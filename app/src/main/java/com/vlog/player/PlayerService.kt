package com.vlog.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.core.net.toUri
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaLibraryService.MediaLibrarySession.Callback

// Data models (copied from VideoPlayerScreen.kt for service use)
@JsonClass(generateAdapter = true)
data class VideoDetailResponse(
    @Json(name = "data") val data: VideoItem = VideoItem()
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "categoryId") val categoryId: String = "",
    @Json(name = "attachmentId") val attachmentId: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "score") val score: String = "",
    @Json(name = "alias") val alias: String = "",
    @Json(name = "director") val director: String = "",
    @Json(name = "actors") val actors: String = "",
    @Json(name = "region") val region: String = "",
    @Json(name = "language") val language: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "tags") val tags: String = "",
    @Json(name = "author") val author: String = "",
    @Json(name = "coverUrl") val coverUrl: String = "",
    @Json(name = "videoPlayList") val videoPlayList: List<VideoPlayListGroup> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoPlayListGroup(
    @Json(name = "gatherId") val gatherId: String = "",
    @Json(name = "gatherTitle") val gatherTitle: String = "",
    @Json(name = "playerHost") val playerHost: String = "",
    @Json(name = "playerPort") val playerPort: String = "",
    @Json(name = "remarks") val remarks: String = "",
    @Json(name = "playList") val playList: List<PlayItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlayItem(
    @Json(name = "title") val title: String = "",
    @Json(name = "path") val path: String = "",
    @Json(name = "playUrl") val playUrl: String = ""
)


@UnstableApi
class PlayerService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibraryService.MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    internal var videoDetail: VideoItem? = null // Store parsed video data, made internal for test access

    // Hardcoded JSON data (as in VideoPlayerScreen)
    private val videoItemJson = """
        {"data":{"id":"5e3944e0-a017-4f23-892b-71fa8706ed28","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"4f0f9d49-108f-4591-9693-cfcfd5ab9aeb","title":"剑来 第一季","score":"8.9","alias":"剑来 动画版,Sword of Coming","director":"","actors":"","region":"中国","language":"汉语普通话","description":"大千世界，无奇不有。 骊珠洞天中本该有大气运的贫寒少年，因为本命瓷碎裂的缘故，使得机缘临身却难以捉住。基于此，众多大佬纷纷以少年为焦点进行布局，使得少年身边的朋友获得大机缘，而少年却置身风口浪尖之上…","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20240815-1/345228a8f8a8c10084349ffafafaee96.jpg","videoPlayList":[{"gatherId":"f65c8c05-155f-41e4-9e1c-32599bf95ada","gatherTitle":"BD","playerHost":"https://vod6.bdzybf11.com","playerPort":"0","remarks":"更新第26集","playList":[{"title":"第01集","path":"/20241228/efqzeVzv/index.m3u8","playUrl":"https://vod6.bdzybf11.com/20241228/efqzeVzv/index.m3u8"},{"title":"第02集","path":"/videos/202504/04/67d31efc7d3b5136eef7d9d8/d9af6d/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31efc7d3b5136eef7d9d8/d9af6d/index.m3u8"},{"title":"第03集","path":"/videos/202504/04/67d31ec27d3b5136eef7d9c6/177c07/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ec27d3b5136eef7d9c6/177c07/index.m3u8"}]}]}}
    """.trimIndent()

    private inner class MyMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        private val rootMediaId = "root_id"

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(rootMediaId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Vlog Player Library")
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            if (parentId != rootMediaId) {
                return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
            }

            val mediaItems = mutableListOf<MediaItem>()
            videoDetail?.let { detail ->
                if (detail.videoPlayList.isNotEmpty()) {
                    val firstPlaylist = detail.videoPlayList[0] // Using only the first playlist group
                    firstPlaylist.playList.forEach { playItem ->
                        val mediaMetadata = MediaMetadata.Builder()
                            .setTitle(playItem.title)
                            .setArtist(detail.title) // Using main video title as artist
                            .setArtworkUri(detail.coverUrl.toUri())
                            .setIsPlayable(true)
                            .setIsBrowsable(false)
                            .build()
                        
                        val mediaItem = MediaItem.Builder()
                            .setMediaId(playItem.playUrl) // Using playUrl as unique ID for playable item
                            .setUri(playItem.playUrl.toUri())
                            .setMimeType(MimeTypes.APPLICATION_M3U8)
                            .setMediaMetadata(mediaMetadata)
                            .build()
                        mediaItems.add(mediaItem)
                    }
                }
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(mediaItems), params))
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        // Parse JSON and store videoDetail
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(VideoDetailResponse::class.java)
        val videoItemResponse = jsonAdapter.fromJson(videoItemJson) ?: VideoDetailResponse()
        videoDetail = videoItemResponse.data // Store for callback use

        // Set items to player (optional, if service should also play immediately or if client adds items)
        videoDetail?.let { detail ->
             if (detail.videoPlayList.isNotEmpty() && detail.videoPlayList[0].playList.isNotEmpty()) {
                val firstPlaylist = detail.videoPlayList[0]
                val playerMediaItems = firstPlaylist.playList.map { playItem ->
                    MediaItem.Builder()
                        .setUri(playItem.playUrl.toUri())
                        .setMediaId(playItem.playUrl) // Ensure this matches ID in onGetChildren
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                         .setMediaMetadata( // Essential for the player to know what to display
                            MediaMetadata.Builder()
                                .setTitle(playItem.title)
                                .setArtist(detail.title)
                                .setArtworkUri(detail.coverUrl.toUri())
                                .build()
                        )
                        .build()
                }
                player.setMediaItems(playerMediaItems)
                player.prepare()
            }
        }
        
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, MyMediaLibrarySessionCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    // Testability functions
    internal fun getTestableVideoDetail(): VideoItem? = this.videoDetail
    
    internal fun getTestableMediaLibrarySessionCallback(): Callback = MyMediaLibrarySessionCallback()

    internal fun setTestableVideoDetail(detail: VideoItem?) {
        this.videoDetail = detail
    }
}
