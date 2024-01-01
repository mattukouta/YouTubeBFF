package com.kouta.youtubebff.client

import com.kouta.youtubebff.extension.setAccessToken
import com.kouta.youtubebff.vo.*
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import reactor.core.publisher.Mono


@Component
class YouTubeApiClient {
    companion object {
        private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"

        // TODO ACCESS_TOKENはデバッグ後に削除
        val ACCESS_TOKEN =
            "ya29.a0AfB_byBOOM6yVqMDldXFEWdGOXsWqYOmg4gmEeVj72TVoilBLu1sJlneAHJxugPR1S5EzYhOPBztlCeHVN5NDnSzDYcD0Xsq3_5Itl6wv-w9mH_ZLhgnfpO5Sw02ZZtsosE6STfE8H9g44SdaOxcCKIr-cmtV9y95EBEaCgYKAZYSARESFQHGX2Mi_0XL-Zsvk38NGK5ZvPztZw0171"
    }

    private val webClient = WebClient.builder().baseUrl(YOUTUBE_BASE_URL).build()

    private fun videoRequest(params: MultiValueMap<String, String>, accessToken: String) = webClient.get().uri {
        params.setAccessToken(accessToken)
        it.path("videos").queryParams(params).build()
    }.httpRequest { println(it.uri) }
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()


    fun getVideos(params: MultiValueMap<String, String>, accessToken: String) =
        videoRequest(params, accessToken)
            .bodyToMono(Video.Response::class.java)

    suspend fun fetchVideos(videoIds: List<String>, accessToken: String, limit: Int, nextPageToken: String?): Video.Response? {
        val params = Video.Request(
            parts = listOf(
                Video.Request.Part.ID,
                Video.Request.Part.SNIPPET,
                Video.Request.Part.LIVE_STREAMING_DETAILS
            ),
            filter = Video.Request.Filter.Id(videoIds),
            maxResults = limit,
            pageToken = nextPageToken
        )

        return videoRequest(params.toQueryMap(), accessToken).awaitBodyOrNull<Video.Response>()
    }

//    fun getVideo
//
//    fun getVideoAll(accessToken: String, limit: Int, offset: Int): List<VideoDetailCache>  {
//        var params = Subscription.Request(
//            part = listOf(
//                Subscription.Request.Part.SNIPPET
//            ),
//            filter = Subscription.Request.Filter.Mine,
//            forChannelId = listOf(),
//            maxResults = limit,
//            order = Subscription.Request.Order.RELEVANCE,
//            pageToken = null
//        )
//
//        val results = mutableListOf<VideoDetailCache>()
//        // ループ処理
//        getVideos(params.toQueryMap(), accessToken)
//
//        return emptyList()
//    }

    private fun subscriptionRequest(params: MultiValueMap<String, String>, accessToken: String) = webClient.get().uri {
        params.setAccessToken(accessToken)
        it.path("subscriptions").queryParams(params).build()
    }.httpRequest { println(it.uri) }
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()

    fun getSubscriptions(params: MultiValueMap<String, String>, accessToken: String): Mono<Subscription.Response> =
        subscriptionRequest(params, accessToken)
            .bodyToMono(Subscription.Response::class.java)

    suspend fun fetchSubscriptions(accessToken: String, limit: Int): List<ChannelCache> {
        var params = Subscription.Request(
            parts = listOf(
                Subscription.Request.Part.SNIPPET
            ),
            filter = Subscription.Request.Filter.Mine,
            forChannelId = listOf(),
            maxResults = limit,
            order = Subscription.Request.Order.RELEVANCE,
            pageToken = null
        )

        var isLoop = true
        val results = mutableListOf<ChannelCache>()
        while (isLoop) {
            val result = subscriptionRequest(params.toQueryMap(), accessToken).awaitBodyOrNull<Subscription.Response>()

            when (result) {
                null -> isLoop = false
                else -> {
                    result.items.map {
                        it.snippet?.let { snippet ->
                            results.add(
                                ChannelCache(
                                    id = snippet.resourceId.channelId,
                                    title = snippet.title,
                                    description = snippet.description,
                                    thumbnails = ChannelCache.Thumbnails(
                                        default = snippet.thumbnails.default,
                                        medium = snippet.thumbnails.medium,
                                        high = snippet.thumbnails.high
                                    )
                                )
                            )
                        }
                    }

                    if (result.nextPageToken == null) {
                        isLoop = false
                    } else {
                        params = params.copy(pageToken = result.nextPageToken)
                    }
                }
            }
        }

        return results
    }
}