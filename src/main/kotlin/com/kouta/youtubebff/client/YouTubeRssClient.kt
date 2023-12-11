package com.kouta.youtubebff.client

import com.kouta.youtubebff.vo.YouTubeRss
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.springframework.stereotype.Component
import java.net.URL


@Component
class YouTubeRssClient {
    companion object {
        private const val YOUTUBE_RSS_FEED_URL = "https://www.youtube.com/feeds/videos.xml"
    }

    fun getRss(channelId: String): YouTubeRss.Response {
        val url = URL("$YOUTUBE_RSS_FEED_URL?channel_id=$channelId")
        val response = SyndFeedInput().build(XmlReader(url))
        return YouTubeRss.Response(
            response.entries.map {
                YouTubeRss.Response.Feed(it.uri.replace("yt:video:", ""))
            }
        )
    }
}