package com.example.catchup.shared.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.catchup.shared.extensions.*

fun MediaMetadataCompat.Builder.from(jsonNews: JsonNews): MediaMetadataCompat.Builder {
    id = jsonNews.id
    title = jsonNews.title
    album = jsonNews.category
    mediaUri = jsonNews.filepath
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    return this
}

data class JsonLibrary (
    var news: MutableList<JsonNews> = mutableListOf()
)

data class JsonNews (
    var id: String = "",
    var title: String = "",
    var category: String = "",
    var filepath: String = ""
)