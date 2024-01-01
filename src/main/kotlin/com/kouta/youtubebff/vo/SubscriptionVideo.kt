package com.kouta.youtubebff.vo

data class SubscriptionVideo(
    val videos: List<Video>
) {
    data class Video(
        val id: String,
        val title: String,
        val thumbnails: Thumbnails,
        val liveBroadcastContent: String,
        val liveStreamingDetails: LiveStreamingDetails?,
        val channel: Channel
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

    data class Channel(
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
}
