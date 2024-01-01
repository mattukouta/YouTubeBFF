package com.kouta.youtubebff.controller.youtube_api

import com.kouta.youtubebff.client.YouTubeApiClient
import com.kouta.youtubebff.vo.Video
import com.kouta.youtubebff.vo.Video.Request
import com.kouta.youtubebff.vo.Video.Request.Filter
import com.kouta.youtubebff.vo.Video.Request.Part
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class VideoController {
    @Autowired
    private lateinit var youTubeApiClient: YouTubeApiClient

    companion object {
        const val DATABASE_SIZE = 5
    }

    @GetMapping("/videos")
    fun getVideos(
        @RequestParam(name = "accessToken", required = false, defaultValue = "")
        accessToken: String,
        @RequestParam(name = "offset", required = false, defaultValue = "0")
        offsetString: String,
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        limitString: String
    ): Mono<Video.Response> {
        val offset = offsetString.toIntOrNull() ?: 0
        val limit = limitString.toIntOrNull() ?: 10

        val params = Request(
            listOf(
                Part.ID,
                Part.SNIPPET,
                Part.LIVE_STREAMING_DETAILS
            ),
            filter = Filter.Id(listOf("U24BpEAgFfM")),
            maxResults = DATABASE_SIZE,
            pageToken = null
        ).toQueryMap()

        return youTubeApiClient.getVideos(params, accessToken)
    }
}