package com.kouta.youtubebff.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.kouta.youtubebff.extension.setAccessToken
import com.kouta.youtubebff.vo.Subscription
import com.kouta.youtubebff.vo.Video
import org.springframework.http.MediaType
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component
class YouTubeApiClient {

    companion object {
        private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
        val ACCESS_TOKEN = "ya29.a0AfB_byBOOM6yVqMDldXFEWdGOXsWqYOmg4gmEeVj72TVoilBLu1sJlneAHJxugPR1S5EzYhOPBztlCeHVN5NDnSzDYcD0Xsq3_5Itl6wv-w9mH_ZLhgnfpO5Sw02ZZtsosE6STfE8H9g44SdaOxcCKIr-cmtV9y95EBEaCgYKAZYSARESFQHGX2Mi_0XL-Zsvk38NGK5ZvPztZw0171"
    }

    private val webClient = WebClient.builder().baseUrl(YOUTUBE_BASE_URL).build()

    fun getVideos(params: MultiValueMap<String, String>): Mono<Video.Response> = webClient.get().uri {
        params.setAccessToken(ACCESS_TOKEN)
        it.path("videos").queryParams(params).build()
    }
        .httpRequest {
            println(it.uri)
        }
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Video.Response::class.java)
        .doOnSuccess {
            println(it)
        }

    fun getSubscriptions(params: MultiValueMap<String, String>): Mono<Subscription.Response> = webClient.get().uri {
        params.setAccessToken(ACCESS_TOKEN)
        it.path("subscriptions").queryParams(params).build()
    }.httpRequest {
        println(it.uri)
    }
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Subscription.Response::class.java)
        .doOnSuccess {
            println(it)
        }
}