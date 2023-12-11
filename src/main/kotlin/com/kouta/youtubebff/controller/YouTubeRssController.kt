package com.kouta.youtubebff.controller

import com.kouta.youtubebff.client.YouTubeApiClient
import com.kouta.youtubebff.client.YouTubeRssClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class YouTubeRssController {
    @Autowired
    private lateinit var youTubeRssClient: YouTubeRssClient

    @GetMapping("/rss")
    fun getRss(
        // test channelId: UCuTAXTexrhetbOe3zgskJBQ
        @RequestParam(name = "channelId", required = true) channelId: String
    ) = youTubeRssClient.getRss(channelId)
}