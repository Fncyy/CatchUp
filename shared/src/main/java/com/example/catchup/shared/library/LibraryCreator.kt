package com.example.catchup.shared.library

import android.content.Context
import android.print.PrintJobInfo.STATE_CREATED
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.MediaMetadataCompat
import android.telecom.Connection.STATE_INITIALIZING
import android.util.Log
import androidx.annotation.IntDef
import com.example.catchup.shared.BROWSE_DEBUG
import com.example.catchup.shared.SERVICE_DEBUG
import com.example.catchup.shared.library.LibraryCreator.Companion.CATEGORY_HARDCODED
import com.example.catchup.shared.model.NewsResponse
import com.example.catchup.shared.network.CurrentsInteractor
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
        val UTTERANCE_IDS = listOf<String>(
            "first",
            "second",
            "third",
            "fourth"
        )
        val CATEGORY_HARDCODED = listOf("general", "auto", "politics")
        const val SAVED_NEWS_COUNT = 3
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

    private lateinit var file: File

    init {
        try {
            val input: FileInputStream = context.openFileInput(SELECTED_SAVE_FILE)
            //selected.addAll(0, input.readBytes().toString(Charsets.UTF_8).split(", "))
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        selected.addAll(CATEGORY_HARDCODED)
        Log.d(SERVICE_DEBUG, "fileDir: ${fileDir.absolutePath}")
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
        library.music.map { jsonMusic ->
            Log.d(BROWSE_DEBUG, jsonMusic.id)
            MediaMetadataCompat.Builder().from(jsonMusic).build()
        }.toList()

    private fun clearFiles() {
        fileDir.listFiles()?.forEach {
            it.delete()
            Log.d(SERVICE_DEBUG, "File deleted: $it")
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
            library.music.add(
                JsonMusic(
                    id = filename,
                    album = category,
                    source = file.absolutePath,
                    title = response.news[i].title
                )
            )
        }
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
    }

    private fun allLoaded(): Boolean {
        fileNames.forEach { name ->
            Log.d(SERVICE_DEBUG, "$name has value: ${utteranceMap[name]}")
            if (!utteranceMap[name]!!) {
                Log.d(SERVICE_DEBUG, "allLoaded returning false -> $name")
                return false
            }
        }
        Log.d(SERVICE_DEBUG, "allLoaded returning true")
        return true
    }

    override fun onDone(utteranceId: String?) {
        Log.d(SERVICE_DEBUG, "TextToSpeech.onDone for: $utteranceId")
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
        Log.d(SERVICE_DEBUG, "TextToSpeech.onError for: $utteranceId")
    }

    override fun onStart(utteranceId: String?) {
        Log.d(SERVICE_DEBUG, "TextToSpeech.onStart for: $utteranceId")
    }

    override fun onInit(status: Int) {
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

