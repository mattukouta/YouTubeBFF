package com.kouta.youtubebff.controller.youtube_api

import com.kouta.youtubebff.client.YouTubeApiClient
import com.kouta.youtubebff.client.YouTubeRssClient
import com.kouta.youtubebff.vo.*
import com.kouta.youtubebff.vo.enums.LiveBroadcastContent
import com.kouta.youtubebff.vo.enums.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SubscriptionVideosController {
    companion object {
        const val DEFAULT_RESULT_COUNT = "5"
    }

    @Autowired
    private lateinit var youTubeApiClient: YouTubeApiClient

    @Autowired
    private lateinit var youtubeRssClient: YouTubeRssClient

    private val channelCached: MutableMap<String, CacheData<ChannelCache>> = mutableMapOf()
    private val videoCached: MutableMap<String, CacheData<VideoCache>> = mutableMapOf()
    private val videoDetailCached: MutableMap<String, CacheData<VideoDetailCache>> = mutableMapOf()

    // FEからPaging用にフォロー済みの最新動画をマッシュアップするエンドポイント
    @GetMapping("/subscription/videos")
    suspend fun getSubscriptionVideos(
        @RequestParam(name = "sessionId", required = true)
        sessionId: String,
        @RequestParam(name = "accessToken", required = true)
        accessToken: String,
        @RequestParam(name = "offset", required = false, defaultValue = "0")
        offset: String,
        @RequestParam(name = "limit", required = false, defaultValue = DEFAULT_RESULT_COUNT)
        limit: String,
        @RequestParam(name = "sort", required = false, defaultValue = "")
        sort: String,
        @RequestParam(name = "filter", required = false, defaultValue = "")
        filter: String,
    ): Mono<SubscriptionVideo> {
        /*
            TODO 全体方針
             - sessionIDごとに各情報を管理
             - 同sessionIdであれば、基本管理している情報から取得
        */
        val requestOffset = offset.toIntOrNull() ?: 0
        val requestLimit = limit.toIntOrNull() ?: 0

        // 全チャンネル取得
        println("全チャンネル取得")
        val channelCaches = channelCached[sessionId]?.data ?: getSubscriptionAll(accessToken, sessionId)
        val channelIds = channelCaches.map { it.id }

        // 全チャンネルのRSS取得
        println("全チャンネルのRSS取得")
        val videoCaches = videoCached[sessionId]?.data ?: getVideoAll(sessionId, channelIds)

        // RSS情報をsortを元に順番入れ替え
        println("RSS情報をsortを元に順番入れ替え")
        val sortedVideos = when (Sort.findByValue(sort) ?: Sort.UPDATED_AT_DESC) {
            Sort.UPDATED_AT_ASC -> videoCaches.sortedBy { it.update }
            Sort.UPDATED_AT_DESC -> videoCaches.sortedBy { it.update }.reversed()
        }

        // TODO filter処理
        println("filter処理")
        println()

        // offsetとlimitを元にFEに動画情報を返す
        println("offsetとlimitを元にFEに動画情報を返す")
        val requestVideos = sortedVideos.subList(requestOffset, requestOffset + requestLimit)
        val videoDetailCaches =
            getVideoDetails(accessToken, sessionId, requestLimit, requestVideos)
        println()

        // チャンネル情報、動画情報を詰め込んで返却する
        val subscriptionVideoResponse = SubscriptionVideo(
            videos = videoDetailCaches.mapNotNull { videoDetail ->
                channelCaches.firstOrNull { it.id == videoDetail.channelId }?.let { channel ->
                    SubscriptionVideo.Video(
                        id = videoDetail.id,
                        title = videoDetail.title,
                        thumbnails = SubscriptionVideo.Video.Thumbnails(
                            default = videoDetail.thumbnails.default,
                            medium = videoDetail.thumbnails.medium,
                            high = videoDetail.thumbnails.high,
                            standard = videoDetail.thumbnails.standard,
                            maxres = videoDetail.thumbnails.maxres

                        ),
                        liveBroadcastContent = videoDetail.liveBroadcastContent.value,
                        liveStreamingDetails = videoDetail.liveStreamingDetails?.let {
                            SubscriptionVideo.Video.LiveStreamingDetails(
                                actualStartTime = videoDetail.liveStreamingDetails.actualStartTime,
                                actualEndTime = videoDetail.liveStreamingDetails.actualEndTime,
                                scheduledStartTime = videoDetail.liveStreamingDetails.scheduledStartTime,
                                scheduledEndTime = videoDetail.liveStreamingDetails.scheduledEndTime,
                                concurrentViewers = videoDetail.liveStreamingDetails.concurrentViewers
                            )
                        },
                        channel = SubscriptionVideo.Channel(
                            id = channel.id,
                            title = channel.title,
                            description = channel.description,
                            thumbnails = SubscriptionVideo.Channel.Thumbnails(
                                default = channel.thumbnails.default,
                                medium = channel.thumbnails.medium,
                                high = channel.thumbnails.high,
                            )
                        ),
                    )
                }
            }
        )


        return Mono.just(subscriptionVideoResponse)
    }

    private suspend fun getSubscriptionAll(accessToken: String, sessionId: String): List<ChannelCache> {
        val channelResponse = youTubeApiClient.fetchSubscriptions(
            accessToken = accessToken,
            limit = 10
        )
        channelCached[sessionId] = CacheData(null, channelResponse)
        return channelResponse
    }

    private suspend fun getVideoAll(
        sessionId: String,
        channelIds: List<String>
    ): List<VideoCache> {
        val videoResponse = youtubeRssClient.getRssAll(channelIds)

        videoCached[sessionId] = CacheData(null, videoResponse)
        return videoResponse
    }

    private suspend fun getVideoDetails(
        accessToken: String,
        sessionId: String,
        limit: Int,
        videos: List<VideoCache>
    ): List<VideoDetailCache> {
        val cacheData = videoDetailCached[sessionId]

        val resultData = if (cacheData == null) {
            val response = youTubeApiClient.fetchVideos(
                videoIds = videos.map { it.id },
                accessToken = accessToken,
                limit = limit,
                nextPageToken = null
            )

            response?.convertToVideoDetailCache()
        } else {
            val notCachedVideos = videos.filterNot { cacheData.data.map { cache -> cache.id }.contains(it.id) }
            val cachedVideos = cacheData.data.filter { videos.map { video -> video.id }.contains(it.id) }

            // APIリクエスト
            val results = if (notCachedVideos.isNotEmpty()) {
                youTubeApiClient.fetchVideos(
                    videoIds = notCachedVideos.map { it.id },
                    accessToken = accessToken,
                    limit = limit,
                    nextPageToken = cacheData.nextToken
                )
            } else null

            // キャッシュに保存する
            results?.let { result ->
                CacheData(
                    nextToken = result.nextPageToken,
                    data = videos.map { it.id }.mapNotNull { id ->
                        val cache = cachedVideos.firstOrNull { it.id == id }
                        val firstItem = results.items.firstOrNull { it.id == id }
                        when {
                            cache != null -> {
                                cache
                            }

                            firstItem?.id != null && firstItem.snippet != null -> {
                                VideoDetailCache(
                                    id = firstItem.id,
                                    title = firstItem.snippet.title,
                                    thumbnails = VideoDetailCache.Thumbnails(
                                        default = firstItem.snippet.thumbnails.default,
                                        medium = firstItem.snippet.thumbnails.medium,
                                        high = firstItem.snippet.thumbnails.high,
                                        standard = firstItem.snippet.thumbnails.standard,
                                        maxres = firstItem.snippet.thumbnails.maxres
                                    ),
                                    liveBroadcastContent = LiveBroadcastContent.findByValue(firstItem.snippet.liveBroadcastContent)
                                        ?: LiveBroadcastContent.NONE,
                                    liveStreamingDetails = firstItem.liveStreamingDetails?.let {
                                        VideoDetailCache.LiveStreamingDetails(
                                            actualStartTime = it.actualStartTime,
                                            actualEndTime = it.actualEndTime,
                                            scheduledStartTime = it.scheduledStartTime,
                                            scheduledEndTime = it.scheduledEndTime,
                                            concurrentViewers = it.concurrentViewers
                                        )
                                    },
                                    channelId = firstItem.snippet.channelId
                                )
                            }

                            else -> {
                                null
                            }
                        }
                    }
                )
            } ?: CacheData(
                cacheData.nextToken,
                data = cachedVideos
            )
        }

        resultData?.let { result ->
            videoDetailCached.remove(sessionId)
            videoDetailCached[sessionId] = result.copy(data = (result.data + (cacheData?.data ?: emptyList())).distinctBy { it.id })
        }

        return resultData?.data ?: emptyList()
    }

    private fun Video.Response.convertToVideoDetailCache() =
        CacheData(
            nextToken = this.nextPageToken,
            data = this.items.mapNotNull { video ->
                video.id ?: return@mapNotNull null
                video.snippet?.let { snippet ->
                    VideoDetailCache(
                        id = video.id,
                        title = snippet.title,
                        thumbnails = VideoDetailCache.Thumbnails(
                            default = snippet.thumbnails.default,
                            medium = snippet.thumbnails.medium,
                            high = snippet.thumbnails.high,
                            standard = snippet.thumbnails.standard,
                            maxres = snippet.thumbnails.maxres
                        ),
                        liveBroadcastContent = LiveBroadcastContent.findByValue(snippet.liveBroadcastContent)
                            ?: LiveBroadcastContent.NONE,
                        liveStreamingDetails = video.liveStreamingDetails?.let {
                            VideoDetailCache.LiveStreamingDetails(
                                actualStartTime = it.actualStartTime,
                                actualEndTime = it.actualEndTime,
                                scheduledStartTime = it.scheduledStartTime,
                                scheduledEndTime = it.scheduledEndTime,
                                concurrentViewers = it.concurrentViewers,
                            )
                        },
                        channelId = snippet.channelId
                    )
                }
            }
        )
}