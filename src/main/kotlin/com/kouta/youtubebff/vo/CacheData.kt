package com.kouta.youtubebff.vo

import com.kouta.youtubebff.vo.enums.LiveBroadcastContent

data class CacheData<T>(
    val nextToken: String? = null,
    val data: List<T> = emptyList()
)

data class ChannelCache(
    val id: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails
) {
    data class Thumbnails(
        val default: Thumbnail,
        val medium: Thumbnail,
        val high: Thumbnail
    )
}

data class VideoCache(
    val id: String,
    val update: Long
)

data class VideoDetailCache(
    val id: String,
    val title: String,
    val thumbnails: Thumbnails,
    val liveBroadcastContent: LiveBroadcastContent,
    val liveStreamingDetails: LiveStreamingDetails?,
    val channelId: String,
) {
    data class Thumbnails(
        val default: Thumbnail?,
        val medium: Thumbnail?,
        val high: Thumbnail?,
        val standard: Thumbnail?,
        val maxres: Thumbnail?
    )

    data class LiveStreamingDetails(
        val actualStartTime: String?,
        val actualEndTime: String?,
        val scheduledStartTime: String?,
        val scheduledEndTime: String?,
        val concurrentViewers: Long?
    )
}