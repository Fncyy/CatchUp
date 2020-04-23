package com.example.catchup.shared

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.example.catchup.shared.extensions.album
import com.example.catchup.shared.extensions.flag
import com.example.catchup.shared.extensions.id
import com.example.catchup.shared.extensions.title
import com.example.catchup.shared.library.BrowseTree
import com.example.catchup.shared.library.MEDIA_ROOT_ID
import java.io.File

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 *
 *
 * To implement a MediaBrowserService, you need to:
 *
 *  *  Extend [MediaBrowserServiceCompat], implementing the media browsing
 * related methods [MediaBrowserServiceCompat.onGetRoot] and
 * [MediaBrowserServiceCompat.onLoadChildren];
 *
 *  *  In onCreate, start a new [MediaSessionCompat] and notify its parent
 * with the session's token [MediaBrowserServiceCompat.setSessionToken];
 *
 *  *  Set a callback on the [MediaSessionCompat.setCallback].
 * The callback will receive all the user's actions, like play, pause, etc;
 *
 *  *  Handle all the actual music playing using any method your app prefers (for example,
 * [android.media.MediaPlayer])
 *
 *  *  Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * [MediaSessionCompat.setPlaybackState]
 * [MediaSessionCompat.setMetadata] and
 * [MediaSessionCompat.setQueue])
 *
 *  *  Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 *
 * To make your app compatible with Android Auto, you also need to:
 *
 *  *  Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 *
 */
class SoundService : MediaBrowserServiceCompat() {

    private lateinit var session: MediaSessionCompat
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var browseTree: BrowseTree

    private val callback = SoundCallback()
    private var state: Int = PlaybackStateCompat.STATE_STOPPED

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(this, SESSION_TAG)
        sessionToken = session.sessionToken
        session.setCallback(callback)
        browseTree = BrowseTree(this)
        Log.d(BROWSE_DEBUG, "OnCreateFinished")
    }

    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long {
        var actions = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM)
        actions = when (state) {
            PlaybackStateCompat.STATE_STOPPED ->
                (actions
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE)

            PlaybackStateCompat.STATE_PLAYING ->
                (actions
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE)

            PlaybackStateCompat.STATE_PAUSED ->
                (actions
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP)

            else ->
                (actions
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE)
        }
        return actions
    }

    override fun onDestroy() = session.release()

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.d(BROWSE_DEBUG, "OnGetRootCalled")
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        Log.d(BROWSE_DEBUG, "OnLoadChildren for: $parentId")
        val resultsSent = browseTree.libraryCreator.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val children = browseTree[parentId]?.map { item ->
                    Log.d(BROWSE_DEBUG, "MediaItem: ${item.description}, ${item.flag}")
                    MediaItem(item.description, item.flag)
                }?.toMutableList()
                result.sendResult(children)
            } else {
                session.sendSessionEvent(FAILURE, null)
                result.sendResult(null)
            }
        }

        if (!resultsSent)
            result.detach()
    }

    inner class SoundCallback() :
        MediaSessionCompat.Callback(), MediaPlayer.OnCompletionListener {

        private var mediaPlayer: MediaPlayer? = null
        private var queueIndex = -1
        private var preparedMedia: MediaMetadataCompat? = null
        private val playlist: MutableList<QueueItem> = ArrayList()

        private fun initializeMediaPlayer() {
            mediaPlayer = mediaPlayer ?: MediaPlayer()
            mediaPlayer?.setOnCompletionListener(this)
        }

        private fun updateState() {
            session.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(getAvailableActions())
                    .setState(
                        state,
                        if (mediaPlayer != null) mediaPlayer!!.currentPosition.toLong() else 0L,
                        1.0f
                    ).build()
            )
        }

        override fun onPlay() {
            if (playlist.isEmpty())
                return

            initializeMediaPlayer()

            if (preparedMedia == null)
                onPrepare()

            val filepath = File(applicationContext.filesDir, preparedMedia!!.id)
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(filepath.toString())
            Log.d(PLAY_DEBUG, "Playing this: $filepath")

            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                state = PlaybackStateCompat.STATE_PLAYING
                updateState()
            }
        }

        override fun onPrepare() {
            if (queueIndex < 0 && playlist.isEmpty())
                return

            val mediaId = playlist[queueIndex].description.mediaId
            preparedMedia = browseTree.libraryMap[mediaId]
            session.setMetadata(preparedMedia)

            if (!session.isActive)
                session.isActive = true
        }

        override fun onSkipToQueueItem(queueId: Long) {
            for (i in playlist.indices) {
                if (playlist[i].queueId == queueId) {
                    queueIndex = i
                    preparedMedia = null
                    onPlay()
                    return
                }
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                onPause()
            }
            val mediaItem = browseTree.libraryMap[mediaId]
            playlist.clear()
            browseTree[mediaItem?.album.toString()]?.forEach {
                val description = MediaDescriptionCompat.Builder()
                    .setMediaId(it.id)
                    .setTitle(it.title)
                    .build()

                playlist.add(QueueItem(description, description.hashCode().toLong()))
                Log.d(PLAY_DEBUG, "Playlist item: ${description.mediaId}")
            }
            session.setQueue(playlist)
            session.setQueueTitle(mediaItem?.album.toString())
            for (i in playlist.indices) {
                if (playlist[i].description.mediaId == mediaId) {
                    queueIndex = i
                    preparedMedia = null
                    break
                }
            }
            Log.d(PLAY_DEBUG, "Playing from media id: $mediaId")
            onPlay()
        }

        override fun onPause() {
            mediaPlayer?.pause()
            state = PlaybackStateCompat.STATE_PAUSED
            updateState()
        }

        override fun onStop() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            queueIndex = -1
            playlist.clear()
            session.setMetadata(null)
            session.setQueue(null)
            session.setQueueTitle(null)
            state = PlaybackStateCompat.STATE_STOPPED
            updateState()
        }

        override fun onSkipToNext() {
            if (++queueIndex < playlist.size) {
                preparedMedia = null
                onPlay()
            } else {
                queueIndex = 0
                onPrepare()
                state = PlaybackStateCompat.STATE_PAUSED
                updateState()
            }
        }

        override fun onSkipToPrevious() {
            queueIndex = if (queueIndex > 0) queueIndex - 1 else playlist.size - 1
            preparedMedia = null
            onPlay()
        }

        override fun onCompletion(mp: MediaPlayer?) {
            onSkipToNext()
        }
    }
}


const val PLAY_DEBUG = "PlayDebug"
const val SERVICE_DEBUG = "ServiceDebug"
const val BROWSE_DEBUG = "BrowseDebug"
const val FAILURE = "Couldn't load sounds"
const val SESSION_TAG = "SoundService"
