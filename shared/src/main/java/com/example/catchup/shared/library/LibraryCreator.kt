package com.example.catchup.shared.library

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.IntDef
import com.example.catchup.shared.R
import com.example.catchup.shared.model.NewsResponse
import com.example.catchup.shared.network.CurrentsInteractor
import com.example.catchup.shared.network.isConnected
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class LibraryCreator(private val context: Context) : UtteranceProgressListener(),
    TextToSpeech.OnInitListener {

    companion object {
        const val SELECTED_SAVE_FILE = "selected.txt"
        const val METADATA_FILE = "metadata.json"
        var SAVED_NEWS_COUNT = 3
        const val MAX_FAILED_API_CALLS = 4
    }

    @State
    var state: Int = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    private val library: JsonLibrary = JsonLibrary()
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    private val utteranceMap: ConcurrentMap<String, Boolean> = ConcurrentHashMap()
    private val currentsInteractor = CurrentsInteractor()
    private val fileNames: MutableList<String> = mutableListOf()
    private var selected: MutableList<String> = mutableListOf()
    private var fileDir: File = context.applicationContext.filesDir
    private var textToSpeech: TextToSpeech
    private var errorCount = 0
    private var ttsStatus: Int = -1

    private lateinit var file: File

    init {
        SAVED_NEWS_COUNT = context.getSharedPreferences(
            context.getString(R.string.settings_file_key),
            Context.MODE_PRIVATE
        ).getInt(context.getString(R.string.KEY_NEWS), SAVED_NEWS_COUNT)

        try {
            val input: FileInputStream = context.openFileInput(SELECTED_SAVE_FILE)
            selected.addAll(0, input.readBytes().toString(Charsets.UTF_8).split(", "))
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        clearFiles()

        selected.forEach { category ->
            var filename: String
            for (i in 0 until SAVED_NEWS_COUNT) {
                filename = "${category}_$i.wav"
                utteranceMap[filename] = false
                fileNames.add(filename)
            }
        }

        textToSpeech = TextToSpeech(context, this)
    }

    fun whenReady(performAction: (Boolean) -> Unit): Boolean =
        when (state) {
            STATE_CREATED, STATE_INITIALIZING -> {
                onReadyListeners += performAction
                false
            }
            else -> {
                performAction(state != STATE_ERROR)
                true
            }
        }

    fun buildMetadata(): List<MediaMetadataCompat> =
        library.news.map { jsonMusic ->
            MediaMetadataCompat.Builder().from(jsonMusic).build()
        }.toList()


    /**
     * Clears the data folder
     * Recreates [SELECTED_SAVE_FILE]
     */
    private fun clearFiles() {
        fileDir.listFiles()?.forEach {
            it.delete()
        }
        var content = ""
        var first = true
        selected.forEach {
            if (first) {
                content = it
                first = false
            } else {
                content += ", $it"
            }
        }
        val out: FileOutputStream = context.openFileOutput(SELECTED_SAVE_FILE, Context.MODE_PRIVATE)
        out.write(content.toByteArray())
        out.close()
    }

    private fun saveNewsByCategory(response: NewsResponse, category: String) {
        var filename: String
        for (i in 0 until SAVED_NEWS_COUNT) {
            filename = "${category}_$i.wav"
            file = File(fileDir, filename)
            textToSpeech.synthesizeToFile(response.news[i].description, null, file, filename)
            library.news.add(
                JsonNews(
                    id = filename,
                    category = category,
                    filepath = file.absolutePath,
                    title = response.news[i].title
                )
            )
        }
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
        if (!isConnected())
            state = STATE_ERROR
        if (++errorCount > MAX_FAILED_API_CALLS)
            state = STATE_ERROR
        else
            onInit(ttsStatus)
    }

    private fun allLoaded(): Boolean {
        fileNames.forEach { name ->
            if (!utteranceMap[name]!!) {
                return false
            }
        }
        return true
    }

    override fun onDone(utteranceId: String?) {
        if (utteranceId == null)
            return
        else
            utteranceMap[utteranceId] = true
        if (allLoaded()) {
            val file = File(fileDir, METADATA_FILE)
            file.writeText(Gson().toJson(library))
            state = STATE_INITIALIZED
        }
    }

    override fun onError(utteranceId: String?) {
    }

    override fun onStart(utteranceId: String?) {
    }

    override fun onInit(status: Int) {
        ttsStatus = status
        if (status != TextToSpeech.ERROR) {
            textToSpeech.language = Locale.UK
            textToSpeech.setOnUtteranceProgressListener(this)

            selected.forEach {
                currentsInteractor.getCategory(
                    onSuccessWithString = this::saveNewsByCategory,
                    onError = this::showError,
                    category = it
                )
            }
        }
    }
}

@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
@Retention(AnnotationRetention.SOURCE)
annotation class State

const val STATE_CREATED = 1
const val STATE_INITIALIZING = 2
const val STATE_INITIALIZED = 3
const val STATE_ERROR = 4

