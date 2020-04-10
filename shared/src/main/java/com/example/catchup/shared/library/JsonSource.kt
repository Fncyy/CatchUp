package com.example.catchup.shared.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.example.catchup.shared.BROWSE_DEBUG
import com.example.catchup.shared.extensions.*

fun MediaMetadataCompat.Builder.from(jsonMusic: JsonMusic): MediaMetadataCompat.Builder {
    id = jsonMusic.id
    title = jsonMusic.title
    album = jsonMusic.album
    mediaUri = jsonMusic.source
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    displayTitle = jsonMusic.title
    displayDescription = jsonMusic.album

    return this
}

data class JsonLibrary (
    var music: MutableList<JsonMusic> = mutableListOf()
)

data class JsonMusic (
    var id: String = "",
    var title: String = "",
    var album: String = "",
    var source: String = ""
)