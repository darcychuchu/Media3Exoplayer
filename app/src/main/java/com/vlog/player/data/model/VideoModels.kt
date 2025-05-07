package com.vlog.player.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class VideoListResponse(
    val data: List<VideoItem> = emptyList()
)

@Immutable
data class VideoItem(
    val id: String = "",
    val categoryId: String = "",
    val attachmentId: String = "",
    val title: String = "",
    val score: String = "",
    val alias: String = "",
    val director: String = "",
    val actors: String = "",
    val region: String = "",
    val language: String = "",
    val description: String = "",
    val tags: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val videoPlayList: List<VideoPlayList> = emptyList()
)

@Immutable
data class VideoDetailResponse(
    val data: VideoItem = VideoItem()
)

@Immutable
data class VideoPlayList(
    val gatherId: String = "",
    val gatherTitle: String = "",
    val playerHost: String = "",
    val playerPort: String = "",
    val remarks: String = "",
    val playList: List<PlayItem> = emptyList()
)

@Immutable
data class PlayItem(
    val title: String = "",
    val path: String = "",
    val playUrl: String = ""
)