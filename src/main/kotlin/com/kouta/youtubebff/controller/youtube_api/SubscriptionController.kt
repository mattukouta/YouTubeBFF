package com.kouta.youtubebff.controller.youtube_api

import com.kouta.youtubebff.client.YouTubeApiClient
import com.kouta.youtubebff.vo.Subscription
import com.kouta.youtubebff.vo.Subscription.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SubscriptionController {
    @Autowired
    private lateinit var youTubeApiClient: YouTubeApiClient

    companion object {
        const val DATABASE_SIZE = 5
    }
    @GetMapping("/subscriptions")
    fun getSubscriptions(
        @RequestParam(name = "accessToken", required = false, defaultValue = "")
        accessToken: String,
    ): Mono<Subscription.Response> {
        val params = Request(
            part = listOf(
                Request.Part.SNIPPET
            ),
            filter = Request.Filter.Mine,
            forChannelId = listOf(),
            maxResults = DATABASE_SIZE,
            order = Request.Order.RELEVANCE,
            pageToken = null
        ).toQueryMap()

        return youTubeApiClient.getSubscriptions(params)
    }
}