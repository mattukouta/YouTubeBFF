package com.kouta.youtubebff.client

import com.kouta.youtubebff.vo.VideoCache
import com.kouta.youtubebff.vo.YouTubeRss
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.net.URL


@Component
class YouTubeRssClient {
    companion object {
        private const val YOUTUBE_RSS_FEED_URL = "https://www.youtube.com/feeds/videos.xml"
    }

    fun getRss(channelId: String): YouTubeRss.Response? {
        val url = URL("$YOUTUBE_RSS_FEED_URL?channel_id=$channelId")

        return try {
            val response = SyndFeedInput().build(XmlReader(url))

            YouTubeRss.Response(
                response.entries.map {
                    YouTubeRss.Response.Feed(
                        videoId = it.uri.replace("yt:video:", ""),
                        updated = it.updatedDate.time
                    )
                }
            )
        } catch (e: FileNotFoundException) {
            e.stackTrace
            return null
        }
    }

    fun getRssAll(channelIds: List<String>): List<VideoCache> =
        channelIds.flatMap { channelId ->
            getRss(channelId)?.let { response ->
                response.feeds.map {
                    VideoCache(
                        it.videoId,
                        it.updated
                    )
                }
            } ?: emptyList()
        }
}