package com.kouta.youtubebff.vo

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class Subscription {
    data class Response(
        val nextPageToken: String?,
        val prevPageToken: String?,
        val pageInfo: PageInfo,
        val items: List<Subscription>
    ) {
        data class Subscription(
            val id: String?,
            val snippet: Snippet?,
            val contentDetails: ContentDetails?,
            val subscriberSnippet: SubscriberSnippet?
        ) {
            data class Snippet(
                val publishedAt: String,
                val channelTitle: String?,
                val title: String,
                val description: String,
                val resourceId: ResourceId,
                val channelId: String,
                val thumbnails: Thumbnails,

                ) {
                data class ResourceId(
                    val channelId: String
                )
            }

            data class ContentDetails(
                val totalItemCount: Int,
                val newItemCount: Int,
                val activityType: Type
            ) {
                enum class Type {
                    @JsonProperty("all") ALL,
                    @JsonProperty("uploads") UPLOADS,
                    UNKNOWN;
                }
            }

            data class SubscriberSnippet(
                val title: String,
                val description: String,
                val channelId: String,
                val thumbnails: Thumbnails
            )

            data class Thumbnails(
                val default: Thumbnail,
                val medium: Thumbnail,
                val high: Thumbnail
            )
        }
    }

    data class Request(
        val parts: List<Part>,
        val filter: Filter,
        val forChannelId: List<String> = listOf(),
        val maxResults: Int = 5,
        val order: Order = Order.RELEVANCE,
        val pageToken: String? = null
    ) {
        enum class Part(val value: String) {
            CONTENT_DETAILS("contentDetails"),
            ID("id"),
            SNIPPET("snippet"),
            SUBSCRIBER_SNIPPET("subscriberSnippet");
        }

        sealed class Filter {
            data class ChannelId(val channelId: String): Filter()
            data class Id(val id: String): Filter()
            data object Mine: Filter()
            data object MyRecentSubscribers: Filter()
            data object MySubscribers: Filter()
        }

        enum class Order(val value: String) {
            ALPHABETICAL("alphabetical"),
            RELEVANCE("relevance"),
            UNREAD("unread");
        }

        fun toQueryMap(): MultiValueMap<String, String> {
            val params = LinkedMultiValueMap<String, String>()

            params["part"] = parts.joinToString(",") { it.value }

            when (filter) {
                is Filter.ChannelId -> params["channelId"] = filter.channelId
                is Filter.Id -> params["id"] = filter.id
                Filter.Mine -> params["mine"] = true.toString()
                Filter.MyRecentSubscribers -> params["myRecentSubscribers"] = true.toString()
                Filter.MySubscribers -> params["mySubscribers"] = true.toString()
            }

            if (forChannelId.isNotEmpty()) {
                params["forChannelId"] = forChannelId.joinToString(",")
            }

            params["maxResults"] = maxResults.toString()

            params["order"] = order.value

            pageToken?.let { params["pageToken"] = it }

            return params
        }
    }
}