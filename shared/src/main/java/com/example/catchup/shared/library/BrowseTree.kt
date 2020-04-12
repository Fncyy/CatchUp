package com.example.catchup.shared.library

import android.content.Context
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.example.catchup.shared.BROWSE_DEBUG
import com.example.catchup.shared.extensions.album
import com.example.catchup.shared.extensions.flag
import com.example.catchup.shared.extensions.id
import com.example.catchup.shared.extensions.title

class BrowseTree(context: Context) {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()
    val libraryCreator = LibraryCreator(context)
    val libraryMap = mutableMapOf<String, MediaMetadataCompat>()

    init {
        val rootList = mediaIdToChildren[MEDIA_ROOT_ID] ?: mutableListOf()
        mediaIdToChildren[MEDIA_ROOT_ID] = rootList
        libraryCreator.whenReady { successfullyInitialized ->
                if (successfullyInitialized) {
                    val metadata = libraryCreator.buildMetadata()
                    metadata.sortedBy { it.album }.forEach { mediaItem ->
                        libraryMap[mediaItem.id] = mediaItem
                        val albumMediaId = mediaItem.album
                        val albumChildren = mediaIdToChildren[albumMediaId] ?: buildAlbumRoot(mediaItem)
                        albumChildren += mediaItem
                    }
                }
            }
    }

    private fun buildAlbumRoot(mediaItem: MediaMetadataCompat): MutableList<MediaMetadataCompat> {
        val albumMetadata = MediaMetadataCompat.Builder().apply {
            id = mediaItem.album.toString()
            title = mediaItem.album
            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        }.build()

        val rootList = mediaIdToChildren[MEDIA_ROOT_ID] ?: mutableListOf()
        rootList += albumMetadata
        mediaIdToChildren[MEDIA_ROOT_ID] = rootList

        return mutableListOf<MediaMetadataCompat>().also {
            mediaIdToChildren[albumMetadata.id] = it
        }
    }

    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]
}

const val MEDIA_ROOT_ID = "/"