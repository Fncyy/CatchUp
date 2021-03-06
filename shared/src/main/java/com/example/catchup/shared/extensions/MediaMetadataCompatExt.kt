package com.example.catchup.shared.extensions

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

inline val MediaMetadataCompat.id: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline val MediaMetadataCompat.album: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

@MediaBrowserCompat.MediaItem.Flags
inline val MediaMetadataCompat.flag
    get() = this.getLong(METADATA_MEDIA_FLAGS).toInt()

const val NO_GET = "Property does not have a 'get'"
const val GETTER_ERROR = "Cannot get from MediaMetadataCompat.Builder"

inline var MediaMetadataCompat.Builder.id: String
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException(GETTER_ERROR)
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, value)
    }

inline var MediaMetadataCompat.Builder.title: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException(GETTER_ERROR)
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_TITLE, value)
    }

inline var MediaMetadataCompat.Builder.album: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException(GETTER_ERROR)
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_ALBUM, value)
    }

inline var MediaMetadataCompat.Builder.mediaUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException(GETTER_ERROR)
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, value)
    }

@MediaBrowserCompat.MediaItem.Flags
inline var MediaMetadataCompat.Builder.flag: Int
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(METADATA_MEDIA_FLAGS, value.toLong())
    }

const val METADATA_MEDIA_FLAGS = "mediaItemFlag"
