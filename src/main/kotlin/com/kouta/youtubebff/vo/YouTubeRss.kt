package com.kouta.youtubebff.vo

class YouTubeRss {
    data class Response(
        val feeds: List<Feed>
    ) {
        data class Feed(
            val videoId: String,
            val updated: Long
        )
    }
}
