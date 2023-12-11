package com.kouta.youtubebff.vo

import com.kouta.youtubebff.vo.enums.LiveBroadcastContent
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class Video {
    data class Request(
        val parts: List<Part>,
        val filter: Filter,
        val maxResults: Int? = null,
        val pageToken: String? = null,
        val videoCategoryId: String? = null
    ) {
        enum class Part(val value: String) {
            CONTENT_DETAILS("contentDetails"),
            ID("id"),
            LIVE_STREAMING_DETAILS("liveStreamingDetails"),
            SNIPPET("snippet"),
            STATUS("status"),
            SUGGESTIONS("suggestions");
        }

        sealed class Filter(open val value: String) {
            sealed class Chart(override val value: String) : Filter(value) {
                data object MostPopular : Chart("mostPopular")
            }
            data class Id(val videoId: List<String>) : Filter(videoId.joinToString(","))
            sealed class MyRating(override val value: String) : Filter(value) {
                data object Dislike : MyRating("dislike")
                data object Like : MyRating("like")
            }
        }

        fun toQueryMap() : MultiValueMap<String, String> {
            val params = LinkedMultiValueMap<String, String>()

            params["part"] = parts.joinToString(",") { it.value }

            when (filter) {
                Filter.Chart.MostPopular -> params["chart"] = filter.value
                is Filter.Id -> params["id"] = filter.value
                Filter.MyRating.Dislike -> params["myRating"] = filter.value
                Filter.MyRating.Like -> params["myRating"] = filter.value
            }

            maxResults?.let {
                params["maxResults"] = it.toString()
            }

            pageToken?.let {
                params["pageToken"] = it
            }

            videoCategoryId?.let {
                params["videoCategoryId"] = it
            }

            return params
        }
    }

    data class Response(
        val nextPageToken: String?,
        val prevPageToken: String?,
        val pageInfo: PageInfo,
        val items: List<Video>
    ) {
        data class Video(
            val id: String?,
            val snippet: Snippet?,
            val contentDetails: ContentDetails?,
            val status: Status?,
            val liveStreamingDetails: LiveStreamingDetails?
        ) {
            data class Snippet(
                val publishedAt: String,
                val channelId: String,
                val title: String,
                val description: String,
                val thumbnails: Thumbnails,
                val channelTitle: String,
                val tags: List<String>?,
                val categoryId: String,
                val liveBroadcastContent: LiveBroadcastContent
            ) {
                data class Thumbnails(
                    val default: Thumbnail?,
                    val medium: Thumbnail?,
                    val high: Thumbnail?,
                    val standard: Thumbnail?,
                    val maxres: Thumbnail?
                ){
                    fun getHighThumbnail() = when {
                        maxres != null -> maxres
                        standard != null -> standard
                        high != null -> high
                        medium != null -> medium
                        default != null -> default
                        else -> null
                    }
                }
            }

            data class ContentDetails(
                val duration: String,
                val dimension: String,
                val definition: Definition,
                val caption: Boolean,
                val licensedContent: Boolean
            ) {
                enum class Definition(val value: String) {
                    HD("hd"),
                    SD("sd");
                }
            }

            data class Status(
                val publishAt: String?,
            )
            data class LiveStreamingDetails(
                val actualStartTime: String?,
                val actualEndTime: String?,
                val scheduledStartTime: String?,
                val scheduledEndTime: String?,
                val concurrentViewers: Long?
            )
        }
    }
}
