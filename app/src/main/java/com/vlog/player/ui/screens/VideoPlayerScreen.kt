package com.vlog.player.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Subtitles // For Subtitle Button
import androidx.compose.material.icons.filled.Videocam // For Record Button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.net.Uri // For Subtitle URI
import androidx.media3.common.C // For SELECTION_FLAG_DEFAULT
import android.content.Intent // For sharing
import android.widget.Toast // For user feedback
import androidx.core.content.FileProvider // For sharing
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer // Transformer API
import java.io.File // For file operations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.ComponentName
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.SubtitleConfiguration
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.vlog.player.PlayerService
import com.vlog.player.data.model.VideoItem
import androidx.media3.common.util.UnstableApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.player.data.model.VideoDetailResponse
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.guava.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    onBackClick: () -> Unit
) {
    var isFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentPlayTitle by remember { mutableStateOf("") }
    var mediaController: MediaController? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    var transformer: Transformer? by remember { mutableStateOf(null) }
    var subtitlesEnabled by remember { mutableStateOf(false) }

    val videoItemJson = """
        {"data":{"id":"5e3944e0-a017-4f23-892b-71fa8706ed28","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"4f0f9d49-108f-4591-9693-cfcfd5ab9aeb","title":"剑来 第一季","score":"8.9","alias":"剑来 动画版,Sword of Coming","director":"","actors":"","region":"中国","language":"汉语普通话","description":"大千世界，无奇不有。 骊珠洞天中本该有大气运的贫寒少年，因为本命瓷碎裂的缘故，使得机缘临身却难以捉住。基于此，众多大佬纷纷以少年为焦点进行布局，使得少年身边的朋友获得大机缘，而少年却置身风口浪尖之上…","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20240815-1/345228a8f8a8c10084349ffafafaee96.jpg","videoPlayList":[{"gatherId":"f65c8c05-155f-41e4-9e1c-32599bf95ada","gatherTitle":"BD","playerHost":"https://vod6.bdzybf11.com","playerPort":"0","remarks":"更新第26集","playList":[{"title":"第01集","path":"/20241228/efqzeVzv/index.m3u8","playUrl":"https://vod6.bdzybf11.com/20241228/efqzeVzv/index.m3u8"},{"title":"第02集","path":"/videos/202504/04/67d31efc7d3b5136eef7d9d8/d9af6d/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31efc7d3b5136eef7d9d8/d9af6d/index.m3u8"},{"title":"第03集","path":"/videos/202504/04/67d31ec27d3b5136eef7d9c6/177c07/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ec27d3b5136eef7d9c6/177c07/index.m3u8"},{"title":"第04集","path":"/videos/202504/04/67d31e807d3b5136eef7d9b8/52b9db/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31e807d3b5136eef7d9b8/52b9db/index.m3u8"},{"title":"第05集","path":"/videos/202504/04/67d31e8b7d3b5136eef7d9bb/91afed/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31e8b7d3b5136eef7d9bb/91afed/index.m3u8"},{"title":"第06集","path":"/videos/202504/04/67d31e917d3b5136eef7d9bc/b9b0e6/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31e917d3b5136eef7d9bc/b9b0e6/index.m3u8"},{"title":"第07集","path":"/videos/202504/04/67d31e9d7d3b5136eef7d9bf/f2b680/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31e9d7d3b5136eef7d9bf/f2b680/index.m3u8"},{"title":"第08集","path":"/videos/202504/04/67d31e9d7d3b5136eef7d9be/0e712a/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31e9d7d3b5136eef7d9be/0e712a/index.m3u8"},{"title":"第09集","path":"/videos/202504/04/67d31ea77d3b5136eef7d9c1/cg5f96/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ea77d3b5136eef7d9c1/cg5f96/index.m3u8"},{"title":"第10集","path":"/videos/202504/04/67d31ea97d3b5136eef7d9c2/3a256c/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ea97d3b5136eef7d9c2/3a256c/index.m3u8"},{"title":"第11集","path":"/videos/202504/04/67d31ea47d3b5136eef7d9c0/bda8ee/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ea47d3b5136eef7d9c0/bda8ee/index.m3u8"},{"title":"第12集","path":"/videos/202504/04/67d31eb77d3b5136eef7d9c3/2d788d/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31eb77d3b5136eef7d9c3/2d788d/index.m3u8"},{"title":"第13集","path":"/videos/202504/04/67d31ec07d3b5136eef7d9c5/f2115a/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ec07d3b5136eef7d9c5/f2115a/index.m3u8"},{"title":"第14集","path":"/videos/202504/04/67d31ee07d3b5136eef7d9cc/1agg59/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ee07d3b5136eef7d9cc/1agg59/index.m3u8"},{"title":"第15集","path":"/videos/202504/04/67d31eba7d3b5136eef7d9c4/a6ae8f/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31eba7d3b5136eef7d9c4/a6ae8f/index.m3u8"},{"title":"第16集","path":"/videos/202504/04/67d31edb7d3b5136eef7d9ca/7c33dd/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31edb7d3b5136eef7d9ca/7c33dd/index.m3u8"},{"title":"第17集","path":"/videos/202504/04/67d31ee57d3b5136eef7d9ce/69gffc/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ee57d3b5136eef7d9ce/69gffc/index.m3u8"},{"title":"第18集","path":"/videos/202504/04/67d31ec37d3b5136eef7d9c7/780dad/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ec37d3b5136eef7d9c7/780dad/index.m3u8"},{"title":"第19集","path":"/videos/202504/04/67d31ed77d3b5136eef7d9c9/7905ab/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ed77d3b5136eef7d9c9/7905ab/index.m3u8"},{"title":"第20集","path":"/videos/202504/04/67d31edf7d3b5136eef7d9cb/b03476/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31edf7d3b5136eef7d9cb/b03476/index.m3u8"},{"title":"第21集","path":"/videos/202504/04/67d31ee47d3b5136eef7d9cd/d0c80e/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ee47d3b5136eef7d9cd/d0c80e/index.m3u8"},{"title":"第22集","path":"/videos/202504/04/67d31ee97d3b5136eef7d9d0/96c6ba/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ee97d3b5136eef7d9d0/96c6ba/index.m3u8"},{"title":"第23集","path":"/videos/202504/04/67d31ee87d3b5136eef7d9cf/aef3eb/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31ee87d3b5136eef7d9cf/aef3eb/index.m3u8"},{"title":"第25集","path":"/videos/202504/04/67d31f027d3b5136eef7d9d9/21e527/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31f027d3b5136eef7d9d9/21e527/index.m3u8"},{"title":"第26集","path":"/videos/202504/04/67d31f257d3b5136eef7d9e6/4g633a/index.m3u8","playUrl":"https://b2.bdzybf22.com/videos/202504/04/67d31f257d3b5136eef7d9e6/4g633a/index.m3u8"}]},{"gatherId":"0c42377a-dafb-4a32-a1e8-94f82bf88674","gatherTitle":"KC","playerHost":"https://v3.longshengtea.com","playerPort":"0","remarks":"更新第26集","playList":[{"title":"第01集","path":"/yyv5/202408/15/8uVCszsjeF7/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202408/15/8uVCszsjeF7/video/index.m3u8"},{"title":"第02集","path":"/yyv8/202408/15/mNwb9Lppix14/video/index.m3u8","playUrl":"https://v8.longshengtea.com/yyv8/202408/15/mNwb9Lppix14/video/index.m3u8"},{"title":"第03集","path":"/yyv9/202408/15/cd9MqhUYcM18/video/index.m3u8","playUrl":"https://v9.longshengtea.com/yyv9/202408/15/cd9MqhUYcM18/video/index.m3u8"},{"title":"第04集","path":"/yyv10/202408/21/V45szz2ni915/video/index.m3u8","playUrl":"https://v10.longshengtea.com/yyv10/202408/21/V45szz2ni915/video/index.m3u8"},{"title":"第05集","path":"/yyv12/202408/28/nE2uY6tpDj8/video/index.m3u8","playUrl":"https://v12.longshengtea.com/yyv12/202408/28/nE2uY6tpDj8/video/index.m3u8"},{"title":"第06集","path":"/yyv10/202409/04/ziB2HCM1RJ15/video/index.m3u8","playUrl":"https://v10.longshengtea.com/yyv10/202409/04/ziB2HCM1RJ15/video/index.m3u8"},{"title":"第07集","path":"/yyv12/202409/11/Wq0sQkHqCc8/video/index.m3u8","playUrl":"https://v12.longshengtea.com/yyv12/202409/11/Wq0sQkHqCc8/video/index.m3u8"},{"title":"第08集","path":"/yyv8/202409/18/Na05QqdCuh14/video/index.m3u8","playUrl":"https://v8.longshengtea.com/yyv8/202409/18/Na05QqdCuh14/video/index.m3u8"},{"title":"第09集","path":"/yyv8/202409/25/dgc3w40Deq14/video/index.m3u8","playUrl":"https://v8.longshengtea.com/yyv8/202409/25/dgc3w40Deq14/video/index.m3u8"},{"title":"第10集","path":"/yyv2/202410/02/s4zQFxVSDp10/video/index.m3u8","playUrl":"https://v2.longshengtea.com/yyv2/202410/02/s4zQFxVSDp10/video/index.m3u8"},{"title":"第11集","path":"/yyv3/202410/09/QjsXRwXrDL2/video/index.m3u8","playUrl":"https://v3.longshengtea.com/yyv3/202410/09/QjsXRwXrDL2/video/index.m3u8"},{"title":"第12集","path":"/yyv3/202410/16/UT8xBMc5vB2/video/index.m3u8","playUrl":"https://v3.longshengtea.com/yyv3/202410/16/UT8xBMc5vB2/video/index.m3u8"},{"title":"第13集","path":"/yyv6/202410/23/LxCjDDE49g19/video/index.m3u8","playUrl":"https://v6.longshengtea.com/yyv6/202410/23/LxCjDDE49g19/video/index.m3u8"},{"title":"第14集","path":"/yyv5/202410/30/hQd8Qmikwe21/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202410/30/hQd8Qmikwe21/video/index.m3u8"},{"title":"第15集","path":"/yyv2/202411/06/VLwc0X0C1b21/video/index.m3u8","playUrl":"https://v2.longshengtea.com/yyv2/202411/06/VLwc0X0C1b21/video/index.m3u8"},{"title":"第16集","path":"/yyv3/202411/13/myjkAPaB7r20/video/index.m3u8","playUrl":"https://v3.longshengtea.com/yyv3/202411/13/myjkAPaB7r20/video/index.m3u8"},{"title":"第17集","path":"/yyv2/202411/20/hBbJiTWFLh23/video/index.m3u8","playUrl":"https://v2.longshengtea.com/yyv2/202411/20/hBbJiTWFLh23/video/index.m3u8"},{"title":"第18集","path":"/yyv5/202411/27/1BqJEg33qf24/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202411/27/1BqJEg33qf24/video/index.m3u8"},{"title":"第19集","path":"/yyv2/202412/04/peTMLUDsXs23/video/index.m3u8","playUrl":"https://v2.longshengtea.com/yyv2/202412/04/peTMLUDsXs23/video/index.m3u8"},{"title":"第20集","path":"/yyv5/202412/11/tevqSKTuGK21/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202412/11/tevqSKTuGK21/video/index.m3u8"},{"title":"第21集","path":"/yyv5/202412/14/5Ffm1X3Wcj24/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202412/14/5Ffm1X3Wcj24/video/index.m3u8"},{"title":"第22集","path":"/yyv5/202412/14/8cf0MQdeky20/video/index.m3u8","playUrl":"https://v5.longshengtea.com/yyv5/202412/14/8cf0MQdeky20/video/index.m3u8"},{"title":"第23集","path":"/yyv4/202412/14/YpATfjhiYq20/video/index.m3u8","playUrl":"https://v4.longshengtea.com/yyv4/202412/14/YpATfjhiYq20/video/index.m3u8"},{"title":"第24集","path":"/yyv4/202412/14/wz2MNa1K8w22/video/index.m3u8","playUrl":"https://v4.longshengtea.com/yyv4/202412/14/wz2MNa1K8w22/video/index.m3u8"},{"title":"第25集","path":"/yyv4/202412/14/9UCHXWjcev22/video/index.m3u8","playUrl":"https://v4.longshengtea.com/yyv4/202412/14/9UCHXWjcev22/video/index.m3u8"},{"title":"第26集","path":"/yyv2/202412/14/rnHWaphE2H19/video/index.m3u8","playUrl":"https://v2.longshengtea.com/yyv2/202412/14/rnHWaphE2H19/video/index.m3u8"}]},{"gatherId":"e8e689b2-ccc5-4285-9f7b-723d07c04a22","gatherTitle":"WL","playerHost":"https://cdn.wlcdn88.com","playerPort":"0","remarks":"全26集","playList":[{"title":"第01集","path":"/250d2541/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/250d2541/index.m3u8"},{"title":"第02集","path":"/46a04e3d/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/46a04e3d/index.m3u8"},{"title":"第03集","path":"/c19cce54/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/c19cce54/index.m3u8"},{"title":"第04集","path":"/5d0b7ebb/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/5d0b7ebb/index.m3u8"},{"title":"第05集","path":"/7c7c2b6e/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/7c7c2b6e/index.m3u8"},{"title":"第06集","path":"/b246602b/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/b246602b/index.m3u8"},{"title":"第07集","path":"/595abdfc/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/595abdfc/index.m3u8"},{"title":"第08集","path":"/1302c314/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/1302c314/index.m3u8"},{"title":"第09集","path":"/8fff4840/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/8fff4840/index.m3u8"},{"title":"第10集","path":"/890c3a00/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/890c3a00/index.m3u8"},{"title":"第11集","path":"/02f97b75/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/02f97b75/index.m3u8"},{"title":"第12集","path":"/1b042d77/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/1b042d77/index.m3u8"},{"title":"第13集","path":"/f90eeac3/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/f90eeac3/index.m3u8"},{"title":"第14集","path":"/14bd1a21/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/14bd1a21/index.m3u8"},{"title":"第15集","path":"/5816aed4/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/5816aed4/index.m3u8"},{"title":"第16集","path":"/0f06778e/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/0f06778e/index.m3u8"},{"title":"第17集","path":"/bbb05fcb/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/bbb05fcb/index.m3u8"},{"title":"第18集","path":"/6e4ce698/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/6e4ce698/index.m3u8"},{"title":"第19集","path":"/66ade947/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/66ade947/index.m3u8"},{"title":"第20集","path":"/1716ff4f/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/1716ff4f/index.m3u8"},{"title":"第21集","path":"/309821aa/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/309821aa/index.m3u8"},{"title":"第22集","path":"/cf2afe2b/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/cf2afe2b/index.m3u8"},{"title":"第23集","path":"/c591b644/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/c591b644/index.m3u8"},{"title":"第24集","path":"/a7bd1faa/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/a7bd1faa/index.m3u8"},{"title":"第25集","path":"/107068bb/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/107068bb/index.m3u8"},{"title":"第26集","path":"/fe5cbbd7/index.m3u8","playUrl":"https://cdn.wlcdn88.com:777/fe5cbbd7/index.m3u8"}]},{"gatherId":"1e1d8945-5766-44e8-9e71-f8d6e76244dc","gatherTitle":"HW","playerHost":"https://cos.m3u8hw8.com","playerPort":"0","remarks":"已完结","playList":[{"title":"01","path":"/share/fddc46211b43cae9f4e7d32ae28f5228.m3u8","playUrl":"https://m3u.nikanba.live/share/fddc46211b43cae9f4e7d32ae28f5228.m3u8"},{"title":"02","path":"/share/4d9edf817be0fc4a4a5b32171c39ad1b.m3u8","playUrl":"https://m3u.nikanba.live/share/4d9edf817be0fc4a4a5b32171c39ad1b.m3u8"},{"title":"03","path":"/share/3bc2b6d293cfcf13001e4ea846cbec8d.m3u8","playUrl":"https://m3u.nikanba.live/share/3bc2b6d293cfcf13001e4ea846cbec8d.m3u8"},{"title":"04","path":"/share/1e0048b3cadd174bf5c583005c19742e.m3u8","playUrl":"https://m3u.nikanba.live/share/1e0048b3cadd174bf5c583005c19742e.m3u8"},{"title":"05","path":"/share/2fca2241a44ca615e443e0a800e693f5.m3u8","playUrl":"https://m3u.nikanba.live/share/2fca2241a44ca615e443e0a800e693f5.m3u8"},{"title":"06","path":"/share/49c1903539ef3f3aef0ec22bd92e3719.m3u8","playUrl":"https://m3u.nikanba.live/share/49c1903539ef3f3aef0ec22bd92e3719.m3u8"},{"title":"07","path":"/share/4e4ee7b03b989377f1e1a19cb992c79d.m3u8","playUrl":"https://m3u.nikanba.live/share/4e4ee7b03b989377f1e1a19cb992c79d.m3u8"},{"title":"08","path":"/share/a8c1421d26e9f98a85cfce6efcc26a0e.m3u8","playUrl":"https://m3u.nikanba.live/share/a8c1421d26e9f98a85cfce6efcc26a0e.m3u8"},{"title":"09","path":"/share/4061fc780572ed36225f9598c2a00858.m3u8","playUrl":"https://m3u.nikanba.live/share/4061fc780572ed36225f9598c2a00858.m3u8"},{"title":"10","path":"/share/c5d1c6fcf49aba1d3370649546f21dff.m3u8","playUrl":"https://m3u.nikanba.live/share/c5d1c6fcf49aba1d3370649546f21dff.m3u8"},{"title":"11","path":"/share/cc35464919412e98e17aece6f5c3769d.m3u8","playUrl":"https://m3u.nikanba.live/share/cc35464919412e98e17aece6f5c3769d.m3u8"},{"title":"12","path":"/share/5fb5b97e7768101f4a6a017f7a85d678.m3u8","playUrl":"https://m3u.nikanba.live/share/5fb5b97e7768101f4a6a017f7a85d678.m3u8"},{"title":"13","path":"/share/6fcbb0a1140dc272ea91b66231234c22.m3u8","playUrl":"https://m3u.nikanba.live/share/6fcbb0a1140dc272ea91b66231234c22.m3u8"},{"title":"14","path":"/share/c969788f64bd4c59166a93e37dad292a.m3u8","playUrl":"https://m3u.nikanba.live/share/c969788f64bd4c59166a93e37dad292a.m3u8"},{"title":"15","path":"/share/21496cf70faf5a92bd71512ad95f7f1c.m3u8","playUrl":"https://m3u.nikanba.live/share/21496cf70faf5a92bd71512ad95f7f1c.m3u8"},{"title":"16","path":"/share/5bfe7b359a13aeaaac41e0897e098045.m3u8","playUrl":"https://m3u.nikanba.live/share/5bfe7b359a13aeaaac41e0897e098045.m3u8"},{"title":"17","path":"/share/32822b7c712435f49b9f5d88ffc261fb.m3u8","playUrl":"https://m3u.nikanba.live/share/32822b7c712435f49b9f5d88ffc261fb.m3u8"},{"title":"18","path":"/share/77bec3ad3d26201f39e2fae0cf573ee5.m3u8","playUrl":"https://m3u.nikanba.live/share/77bec3ad3d26201f39e2fae0cf573ee5.m3u8"},{"title":"19","path":"/share/56fd5043c2373e9ef8c6f3611af6c3a2.m3u8","playUrl":"https://m3u.nikanba.live/share/56fd5043c2373e9ef8c6f3611af6c3a2.m3u8"},{"title":"20","path":"/share/b5586605b32828b9a2e58019fdddfe56.m3u8","playUrl":"https://m3u.nikanba.live/share/b5586605b32828b9a2e58019fdddfe56.m3u8"},{"title":"21","path":"/share/0cce5922fb2f1f8360f4f608dc9bd75a.m3u8","playUrl":"https://m3u.nikanba.live/share/0cce5922fb2f1f8360f4f608dc9bd75a.m3u8"},{"title":"22","path":"/share/14e0d04dd50ca0fabb2226b9dae1a674.m3u8","playUrl":"https://m3u.nikanba.live/share/14e0d04dd50ca0fabb2226b9dae1a674.m3u8"},{"title":"23","path":"/share/809120b38beae43c6b70f5c2b599aded.m3u8","playUrl":"https://m3u.nikanba.live/share/809120b38beae43c6b70f5c2b599aded.m3u8"},{"title":"24","path":"/share/e6b8fb79fa16860e395832c60f4293c6.m3u8","playUrl":"https://m3u.nikanba.live/share/e6b8fb79fa16860e395832c60f4293c6.m3u8"},{"title":"25","path":"/share/849efe508c91d4cfcf2e8addb1382af5.m3u8","playUrl":"https://m3u.nikanba.live/share/849efe508c91d4cfcf2e8addb1382af5.m3u8"},{"title":"26","path":"/share/fefd4145a25816ac383a05b4590f94c0.m3u8","playUrl":"https://m3u.nikanba.live/share/fefd4145a25816ac383a05b4590f94c0.m3u8"}]},{"gatherId":"ac6da8a6-7e02-4a70-9d22-a929c268b71a","gatherTitle":"GS","playerHost":"https://v.gsuus.com","playerPort":"0","remarks":"第26集完结","playList":[{"title":"第1集","path":"/play/Rb47xLJa/index.m3u8","playUrl":"https://v.gsuus.com/play/Rb47xLJa/index.m3u8"},{"title":"第2集","path":"/play/YaOrY8Ee/index.m3u8","playUrl":"https://v.gsuus.com/play/YaOrY8Ee/index.m3u8"},{"title":"第3集","path":"/play/RdGj685b/index.m3u8","playUrl":"https://v.gsuus.com/play/RdGj685b/index.m3u8"},{"title":"第4集","path":"/play/qaQwO3qd/index.m3u8","playUrl":"https://v.gsuus.com/play/qaQwO3qd/index.m3u8"},{"title":"第5集","path":"/play/DbD5xZnd/index.m3u8","playUrl":"https://v.gsuus.com/play/DbD5xZnd/index.m3u8"},{"title":"第6集","path":"/play/penpWjYb/index.m3u8","playUrl":"https://v.gsuus.com/play/penpWjYb/index.m3u8"},{"title":"第7集","path":"/play/5eVB6r1e/index.m3u8","playUrl":"https://v.gsuus.com/play/5eVB6r1e/index.m3u8"},{"title":"第8集","path":"/play/penpDlYb/index.m3u8","playUrl":"https://v.gsuus.com/play/penpDlYb/index.m3u8"},{"title":"第9集","path":"/play/YaO4g9Bb/index.m3u8","playUrl":"https://v.gsuus.com/play/YaO4g9Bb/index.m3u8"},{"title":"第10集","path":"/play/9b6ZKXRb/index.m3u8","playUrl":"https://v.gsuus.com/play/9b6ZKXRb/index.m3u8"},{"title":"第11集","path":"/play/mepMqrpa/index.m3u8","playUrl":"https://v.gsuus.com/play/mepMqrpa/index.m3u8"},{"title":"第12集","path":"/play/5eV6MQ9e/index.m3u8","playUrl":"https://v.gsuus.com/play/5eV6MQ9e/index.m3u8"},{"title":"第13集","path":"/play/mepMx4pa/index.m3u8","playUrl":"https://v.gsuus.com/play/mepMx4pa/index.m3u8"},{"title":"第14集","path":"/play/Xe0GRBvd/index.m3u8","playUrl":"https://v.gsuus.com/play/Xe0GRBvd/index.m3u8"},{"title":"第15集","path":"/play/BeX87qge/index.m3u8","playUrl":"https://v.gsuus.com/play/BeX87qge/index.m3u8"},{"title":"第16集","path":"/play/QeZ7XRQa/index.m3u8","playUrl":"https://v.gsuus.com/play/QeZ7XRQa/index.m3u8"},{"title":"第17集","path":"/play/DbD3lrya/index.m3u8","playUrl":"https://v.gsuus.com/play/DbD3lrya/index.m3u8"},{"title":"\t 第18集","path":"/play/QeZ7MGwa/index.m3u8","playUrl":"https://v.gsuus.com/play/QeZ7MGwa/index.m3u8"},{"title":"第19集","path":"/play/mepyJ3Va/index.m3u8","playUrl":"https://v.gsuus.com/play/mepyJ3Va/index.m3u8"},{"title":"第20集","path":"/play/YaOAY9gd/index.m3u8","playUrl":"https://v.gsuus.com/play/YaOAY9gd/index.m3u8"},{"title":"第21集","path":"/play/QeZP8Xvb/index.m3u8","playUrl":"https://v.gsuus.com/play/QeZP8Xvb/index.m3u8"},{"title":"第22集","path":"/play/qaQDkL7e/index.m3u8","playUrl":"https://v.gsuus.com/play/qaQDkL7e/index.m3u8"},{"title":"第23集","path":"/play/lejoYJza/index.m3u8","playUrl":"https://v.gsuus.com/play/lejoYJza/index.m3u8"},{"title":"第24集","path":"/play/lejoYJWa/index.m3u8","playUrl":"https://v.gsuus.com/play/lejoYJWa/index.m3u8"},{"title":"第25集","path":"/play/9b6PWV7e/index.m3u8","playUrl":"https://v.gsuus.com/play/9b6PWV7e/index.m3u8"},{"title":"第26集完结","path":"/play/mepxZV1e/index.m3u8","playUrl":"https://v.gsuus.com/play/mepxZV1e/index.m3u8"}]},{"gatherId":"bb7ed2c4-88d7-4561-b6f8-869ee131333e","gatherTitle":"FF","playerHost":"https://svipsvip.ffzy-online5.com","playerPort":"0","remarks":"已完结","playList":[{"title":"第01集","path":"/20240815/31398_eba62c42/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240815/31398_eba62c42/index.m3u8"},{"title":"第02集","path":"/20240815/31404_16e28998/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240815/31404_16e28998/index.m3u8"},{"title":"第03集","path":"/20240815/31403_c4fa856e/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240815/31403_c4fa856e/index.m3u8"},{"title":"第04集","path":"/20240821/31634_d01d0807/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240821/31634_d01d0807/index.m3u8"},{"title":"第05集","path":"/20240828/31895_4bbc7449/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240828/31895_4bbc7449/index.m3u8"},{"title":"第06集","path":"/20240904/32140_e2d988c7/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240904/32140_e2d988c7/index.m3u8"},{"title":"第07集","path":"/20240911/32363_fb56dbb3/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240911/32363_fb56dbb3/index.m3u8"},{"title":"第08集","path":"/20240918/32578_15a0f9a3/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240918/32578_15a0f9a3/index.m3u8"},{"title":"第09集","path":"/20240925/32840_e7d62ad0/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20240925/32840_e7d62ad0/index.m3u8"},{"title":"第10集","path":"/20241002/33092_2f4933c1/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241002/33092_2f4933c1/index.m3u8"},{"title":"第11集","path":"/20241009/33373_0175d23a/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241009/33373_0175d23a/index.m3u8"},{"title":"第12集","path":"/20241016/33719_cfef4099/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241016/33719_cfef4099/index.m3u8"},{"title":"第13集","path":"/20241023/33989_9c72beb8/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241023/33989_9c72beb8/index.m3u8"},{"title":"第14集","path":"/20241030/34282_e0e5c05d/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241030/34282_e0e5c05d/index.m3u8"},{"title":"第15集","path":"/20241106/34516_92cdc366/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241106/34516_92cdc366/index.m3u8"},{"title":"第16集","path":"/20241113/34760_6301041b/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241113/34760_6301041b/index.m3u8"},{"title":"第17集","path":"/20241120/35094_e090d956/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241120/35094_e090d956/index.m3u8"},{"title":"第18集","path":"/20241127/35369_849c7b9f/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241127/35369_849c7b9f/index.m3u8"},{"title":"第19集","path":"/20241204/35733_e6a07b64/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241204/35733_e6a07b64/index.m3u8"},{"title":"第20集","path":"/20241211/35999_79eb48bb/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241211/35999_79eb48bb/index.m3u8"},{"title":"第21集","path":"/20241214/36112_c6d2a8dc/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36112_c6d2a8dc/index.m3u8"},{"title":"第22集","path":"/20241214/36113_24821016/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36113_24821016/index.m3u8"},{"title":"第23集","path":"/20241214/36114_aef966ac/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36114_aef966ac/index.m3u8"},{"title":"第24集","path":"/20241214/36115_e135d58b/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36115_e135d58b/index.m3u8"},{"title":"第25集","path":"/20241214/36116_ed1b9807/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36116_ed1b9807/index.m3u8"},{"title":"第26集","path":"/20241214/36117_7d933348/index.m3u8","playUrl":"https://svipsvip.ffzy-online5.com/20241214/36117_7d933348/index.m3u8"}]},{"gatherId":"92b174a8-f3de-435b-ae0e-b10558d9f9f6","gatherTitle":"IK","playerHost":"https://bfikuncdn.com","playerPort":"0","remarks":"全26集","playList":[{"title":"第01集","path":"/20240815/F51PgYkC/index.m3u8","playUrl":"https://bfikuncdn.com/20240815/F51PgYkC/index.m3u8"},{"title":"第02集","path":"/20240815/hrVa0loB/index.m3u8","playUrl":"https://bfikuncdn.com/20240815/hrVa0loB/index.m3u8"},{"title":"第03集","path":"/20240815/WcEsaZTo/index.m3u8","playUrl":"https://bfikuncdn.com/20240815/WcEsaZTo/index.m3u8"},{"title":"第04集","path":"/20240821/TUdD0yJb/index.m3u8","playUrl":"https://bfikuncdn.com/20240821/TUdD0yJb/index.m3u8"},{"title":"第05集","path":"/20240828/tCehOKPJ/index.m3u8","playUrl":"https://bfikuncdn.com/20240828/tCehOKPJ/index.m3u8"},{"title":"第06集","path":"/20240904/LrEJ02vM/index.m3u8","playUrl":"https://bfikuncdn.com/20240904/LrEJ02vM/index.m3u8"},{"title":"第07集","path":"/20240911/7Hv5PlZ8/index.m3u8","playUrl":"https://bfikuncdn.com/20240911/7Hv5PlZ8/index.m3u8"},{"title":"第08集","path":"/20240918/f8c4Sw1K/index.m3u8","playUrl":"https://bfikuncdn.com/20240918/f8c4Sw1K/index.m3u8"},{"title":"第09集","path":"/20240926/iX31nd5o/index.m3u8","playUrl":"https://bfikuncdn.com/20240926/iX31nd5o/index.m3u8"},{"title":"第10集","path":"/20241002/OKdKxlFS/index.m3u8","playUrl":"https://bfikuncdn.com/20241002/OKdKxlFS/index.m3u8"},{"title":"第11集","path":"/20241009/huiBy3gO/index.m3u8","playUrl":"https://bfikuncdn.com/20241009/huiBy3gO/index.m3u8"},{"title":"第12集","path":"/20241016/1yQCuvqw/index.m3u8","playUrl":"https://bfikuncdn.com/20241016/1yQCuvqw/index.m3u8"},{"title":"第13集","path":"/20241023/gU2AiOrI/index.m3u8","playUrl":"https://bfikuncdn.com/20241023/gU2AiOrI/index.m3u8"},{"title":"第14集","path":"/20241030/2uwRKZ78/index.m3u8","playUrl":"https://bfikuncdn.com/20241030/2uwRKZ78/index.m3u8"},{"title":"第15集","path":"/20241106/x3fmshxQ/index.m3u8","playUrl":"https://bfikuncdn.com/20241106/x3fmshxQ/index.m3u8"},{"title":"第16集","path":"/20241113/G4tRdEj9/index.m3u8","playUrl":"https://bfikuncdn.com/20241113/G4tRdEj9/index.m3u8"},{"title":"第17集","path":"/20241120/caz1BNe8/index.m3u8","playUrl":"https://bfikuncdn.com/20241120/caz1BNe8/index.m3u8"},{"title":"第18集","path":"/20241127/HeF3enve/index.m3u8","playUrl":"https://bfikuncdn.com/20241127/HeF3enve/index.m3u8"},{"title":"第19集","path":"/20241204/JxXZNKNg/index.m3u8","playUrl":"https://bfikuncdn.com/20241204/JxXZNKNg/index.m3u8"},{"title":"第20集","path":"/20241211/g2i2YzdU/index.m3u8","playUrl":"https://bfikuncdn.com/20241211/g2i2YzdU/index.m3u8"},{"title":"第21集","path":"/20241214/OkOnynDt/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/OkOnynDt/index.m3u8"},{"title":"第22集","path":"/20241214/pjM122NE/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/pjM122NE/index.m3u8"},{"title":"第23集","path":"/20241214/2aU7MBLc/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/2aU7MBLc/index.m3u8"},{"title":"第24集","path":"/20241214/2EWbRVLI/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/2EWbRVLI/index.m3u8"},{"title":"第25集","path":"/20241214/qzrvSmik/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/qzrvSmik/index.m3u8"},{"title":"第26集","path":"/20241214/OSjLB4Wz/index.m3u8","playUrl":"https://bfikuncdn.com/20241214/OSjLB4Wz/index.m3u8"}]},{"gatherId":"1e469266-6b71-487c-9eb0-1daad2681e7c","gatherTitle":"LZ","playerHost":"https://vip1.lz-cdn10.com","playerPort":"0","remarks":"已完结","playList":[{"title":"第01集","path":"/20240815/3725_2916c479/index.m3u8","playUrl":"https://v.cdnlz22.com/20240815/3725_2916c479/index.m3u8"},{"title":"第02集","path":"/20240815/3734_7cd1c271/index.m3u8","playUrl":"https://v.cdnlz22.com/20240815/3734_7cd1c271/index.m3u8"},{"title":"第03集","path":"/20250426/15616_67cb4081/index.m3u8","playUrl":"https://v.lzcdn26.com/20250426/15616_67cb4081/index.m3u8"},{"title":"第04集","path":"/20240821/1670_9fea0ad4/index.m3u8","playUrl":"https://v.cdnlz21.com/20240821/1670_9fea0ad4/index.m3u8"},{"title":"第05集","path":"/20240828/1801_58c3dc4f/index.m3u8","playUrl":"https://v.cdnlz21.com/20240828/1801_58c3dc4f/index.m3u8"},{"title":"第06集","path":"/20240904/1936_17b90191/index.m3u8","playUrl":"https://v.cdnlz21.com/20240904/1936_17b90191/index.m3u8"},{"title":"第07集","path":"/20240911/2057_e11d9e6a/index.m3u8","playUrl":"https://v.cdnlz21.com/20240911/2057_e11d9e6a/index.m3u8"},{"title":"第08集","path":"/20240918/2118_eb4d1c39/index.m3u8","playUrl":"https://v.cdnlz21.com/20240918/2118_eb4d1c39/index.m3u8"},{"title":"第09集","path":"/20240925/2147_eb874480/index.m3u8","playUrl":"https://v.cdnlz21.com/20240925/2147_eb874480/index.m3u8"},{"title":"第10集","path":"/20241002/2191_b747c8cb/index.m3u8","playUrl":"https://v.cdnlz21.com/20241002/2191_b747c8cb/index.m3u8"},{"title":"第11集","path":"/20241009/2214_5a52adbc/index.m3u8","playUrl":"https://v.cdnlz21.com/20241009/2214_5a52adbc/index.m3u8"},{"title":"第12集","path":"/20241016/6505_03ea0252/index.m3u8","playUrl":"https://v.cdnlz22.com/20241016/6505_03ea0252/index.m3u8"},{"title":"第13集","path":"/20241023/6869_fb1fd66b/index.m3u8","playUrl":"https://v.cdnlz22.com/20241023/6869_fb1fd66b/index.m3u8"},{"title":"第14集","path":"/20241030/7221_290894b7/index.m3u8","playUrl":"https://v.cdnlz22.com/20241030/7221_290894b7/index.m3u8"},{"title":"第15集","path":"/20241106/7600_02586f59/index.m3u8","playUrl":"https://v.cdnlz22.com/20241106/7600_02586f59/index.m3u8"},{"title":"第16集","path":"/20241113/7968_cf96e470/index.m3u8","playUrl":"https://v.cdnlz22.com/20241113/7968_cf96e470/index.m3u8"},{"title":"第17集","path":"/20241120/8442_68a53630/index.m3u8","playUrl":"https://v.cdnlz22.com/20241120/8442_68a53630/index.m3u8"},{"title":"第18集","path":"/20241127/8787_b6da2ad4/index.m3u8","playUrl":"https://v.cdnlz22.com/20241127/8787_b6da2ad4/index.m3u8"},{"title":"第19集","path":"/20241204/9170_859796aa/index.m3u8","playUrl":"https://v.cdnlz22.com/20241204/9170_859796aa/index.m3u8"},{"title":"第20集","path":"/20241211/9534_cdde56b7/index.m3u8","playUrl":"https://v.cdnlz22.com/20241211/9534_cdde56b7/index.m3u8"},{"title":"第21集","path":"/20241214/9682_afe277b0/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9682_afe277b0/index.m3u8"},{"title":"第22集","path":"/20241214/9683_9cec2c1b/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9683_9cec2c1b/index.m3u8"},{"title":"第23集","path":"/20241214/9684_f6232a6d/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9684_f6232a6d/index.m3u8"},{"title":"第24集","path":"/20241214/9685_9846cded/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9685_9846cded/index.m3u8"},{"title":"第25集","path":"/20241214/9686_8f1423dc/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9686_8f1423dc/index.m3u8"},{"title":"第26集","path":"/20241214/9687_2ffb4c2c/index.m3u8","playUrl":"https://v.cdnlz22.com/20241214/9687_2ffb4c2c/index.m3u8"}]},{"gatherId":"a4d6a7e8-6fb2-4526-b065-9dade75ef635","gatherTitle":"XL","playerHost":"https://play.xluuss.com","playerPort":"0","remarks":"第26集完结","playList":[{"title":"第1集","path":"/play/7e55yLXe/index.m3u8","playUrl":"https://play.xluuss.com/play/7e55yLXe/index.m3u8"},{"title":"第2集","path":"/play/ZdPvNQ6a/index.m3u8","playUrl":"https://play.xluuss.com/play/ZdPvNQ6a/index.m3u8"},{"title":"第3集","path":"/play/QdJm6Qyb/index.m3u8","playUrl":"https://play.xluuss.com/play/QdJm6Qyb/index.m3u8"},{"title":"第4集","path":"/play/PdRxL40a/index.m3u8","playUrl":"https://play.xluuss.com/play/PdRxL40a/index.m3u8"},{"title":"第5集","path":"/play/oeE5yXke/index.m3u8","playUrl":"https://play.xluuss.com/play/oeE5yXke/index.m3u8"},{"title":"第6集","path":"/play/xboqWkAe/index.m3u8","playUrl":"https://play.xluuss.com/play/xboqWkAe/index.m3u8"},{"title":"第7集","path":"/play/NbWD4vgd/index.m3u8","playUrl":"https://play.xluuss.com/play/NbWD4vgd/index.m3u8"},{"title":"第8集","path":"/play/xboqEmAe/index.m3u8","playUrl":"https://play.xluuss.com/play/xboqEmAe/index.m3u8"},{"title":"第9集","path":"/play/ZdP4jJyd/index.m3u8","playUrl":"https://play.xluuss.com/play/ZdP4jJyd/index.m3u8"},{"title":"第10集","path":"/play/xe74V11a/index.m3u8","playUrl":"https://play.xluuss.com/play/xe74V11a/index.m3u8"},{"title":"第11集","path":"/play/zbqNrw0d/index.m3u8","playUrl":"https://play.xluuss.com/play/zbqNrw0d/index.m3u8"},{"title":"第12集","path":"/play/NbW4NqXd/index.m3u8","playUrl":"https://play.xluuss.com/play/NbW4NqXd/index.m3u8"},{"title":"第13集","path":"/play/zbqNyg0d/index.m3u8","playUrl":"https://play.xluuss.com/play/zbqNyg0d/index.m3u8"},{"title":"第14集","path":"/play/negmJNYb/index.m3u8","playUrl":"https://play.xluuss.com/play/negmJNYb/index.m3u8"},{"title":"第15集","path":"/play/QbY3Qr2e/index.m3u8","playUrl":"https://play.xluuss.com/play/QbY3Qr2e/index.m3u8"},{"title":"第16集","path":"/play/Qe1J7DZa/index.m3u8","playUrl":"https://play.xluuss.com/play/Qe1J7DZa/index.m3u8"},{"title":"第17集","path":"/play/oeE3mvYb/index.m3u8","playUrl":"https://play.xluuss.com/play/oeE3mvYb/index.m3u8"},{"title":"\t 第18集","path":"/play/Qe1JZpVa/index.m3u8","playUrl":"https://play.xluuss.com/play/Qe1JZpVa/index.m3u8"},{"title":"第19集","path":"/play/zbqzK3pd/index.m3u8","playUrl":"https://play.xluuss.com/play/zbqzK3pd/index.m3u8"},{"title":"第20集","path":"/play/ZdPBNJza/index.m3u8","playUrl":"https://play.xluuss.com/play/ZdPBNJza/index.m3u8"},{"title":"第21集","path":"/play/Qe1K673b/index.m3u8","playUrl":"https://play.xluuss.com/play/Qe1K673b/index.m3u8"},{"title":"第22集","path":"/play/PdRElBLb/index.m3u8","playUrl":"https://play.xluuss.com/play/PdRElBLb/index.m3u8"},{"title":"第23集","path":"/play/mbkp2MYd/index.m3u8","playUrl":"https://play.xluuss.com/play/mbkp2MYd/index.m3u8"},{"title":"第24集","path":"/play/mbkp2Mxd/index.m3u8","playUrl":"https://play.xluuss.com/play/mbkp2Mxd/index.m3u8"},{"title":"第25集","path":"/play/xe7Q9Jwb/index.m3u8","playUrl":"https://play.xluuss.com/play/xe7Q9Jwb/index.m3u8"},{"title":"第26集完结","path":"/play/zbqyQ83b/index.m3u8","playUrl":"https://play.xluuss.com/play/zbqyQ83b/index.m3u8"}]},{"gatherId":"5eeb44fc-2fb4-4ec2-b32e-63f50bfff707","gatherTitle":"WJ","playerHost":"https://v10.tlkqc.com","playerPort":"0","remarks":"全26集","playList":[{"title":"第01集","path":"/wjv2/202408/15/Qj0JuMsr0074/video/index.m3u8","playUrl":"https://v2.tlkqc.com/wjv2/202408/15/Qj0JuMsr0074/video/index.m3u8"},{"title":"第02集","path":"/wjv2/202408/15/pbS3yb8PRy74/video/index.m3u8","playUrl":"https://v2.tlkqc.com/wjv2/202408/15/pbS3yb8PRy74/video/index.m3u8"},{"title":"第03集","path":"/wjv9/202408/15/8xwQSEbCWA81/video/index.m3u8","playUrl":"https://v9.tlkqc.com/wjv9/202408/15/8xwQSEbCWA81/video/index.m3u8"},{"title":"第04集","path":"/wjv11/202408/21/PSnUMcCyhk83/video/index.m3u8","playUrl":"https://v11.tlkqc.com/wjv11/202408/21/PSnUMcCyhk83/video/index.m3u8"},{"title":"第05集","path":"/wjv7/202408/28/G6t3faEK4C79/video/index.m3u8","playUrl":"https://v7.tlkqc.com/wjv7/202408/28/G6t3faEK4C79/video/index.m3u8"},{"title":"第06集","path":"/wjv10/202409/04/WiafRSH9dW82/video/index.m3u8","playUrl":"https://v10.tlkqc.com/wjv10/202409/04/WiafRSH9dW82/video/index.m3u8"},{"title":"第07集","path":"/wjv10/202409/11/kKjQCCV0zs82/video/index.m3u8","playUrl":"https://v10.tlkqc.com/wjv10/202409/11/kKjQCCV0zs82/video/index.m3u8"},{"title":"第08集","path":"/wjv9/202409/18/27DQME98V081/video/index.m3u8","playUrl":"https://v9.tlkqc.com/wjv9/202409/18/27DQME98V081/video/index.m3u8"},{"title":"第09集","path":"/wjv11/202409/25/SgkA9NNifU83/video/index.m3u8","playUrl":"https://v11.tlkqc.com/wjv11/202409/25/SgkA9NNifU83/video/index.m3u8"},{"title":"第10集","path":"/wjv4/202410/02/LrcCs7dgmG76/video/index.m3u8","playUrl":"https://v4.tlkqc.com/wjv4/202410/02/LrcCs7dgmG76/video/index.m3u8"},{"title":"第11集","path":"/wjv7/202410/09/HBE05wXrWY79/video/index.m3u8","playUrl":"https://v7.tlkqc.com/wjv7/202410/09/HBE05wXrWY79/video/index.m3u8"},{"title":"第12集","path":"/wjv1/202410/16/QG9Bj9eZgr85/video/index.m3u8","playUrl":"https://v1.tlkqc.com/wjv1/202410/16/QG9Bj9eZgr85/video/index.m3u8"},{"title":"第13集","path":"/wjv12/202410/23/gZubCSAMwA84/video/index.m3u8","playUrl":"https://v12.tlkqc.com/wjv12/202410/23/gZubCSAMwA84/video/index.m3u8"},{"title":"第14集","path":"/wjv10/202410/30/ZzeibgzrMk82/video/index.m3u8","playUrl":"https://v10.tlkqc.com/wjv10/202410/30/ZzeibgzrMk82/video/index.m3u8"},{"title":"第15集","path":"/wjv1/202411/06/qwv3SXTPfz85/video/index.m3u8","playUrl":"https://v1.tlkqc.com/wjv1/202411/06/qwv3SXTPfz85/video/index.m3u8"},{"title":"第16集","path":"/wjv9/202411/13/Lfnd9PYM5681/video/index.m3u8","playUrl":"https://v9.tlkqc.com/wjv9/202411/13/Lfnd9PYM5681/video/index.m3u8"},{"title":"第17集","path":"/wjv3/202411/20/yDjHHS0Wy075/video/index.m3u8","playUrl":"https://v3.tlkqc.com/wjv3/202411/20/yDjHHS0Wy075/video/index.m3u8"},{"title":"第18集","path":"/wjv5/202411/27/MddEcjMjJU77/video/index.m3u8","playUrl":"https://v5.tlkqc.com/wjv5/202411/27/MddEcjMjJU77/video/index.m3u8"},{"title":"第19集","path":"/wjv5/202412/04/xMgeh5rtVq77/video/index.m3u8","playUrl":"https://v5.tlkqc.com/wjv5/202412/04/xMgeh5rtVq77/video/index.m3u8"},{"title":"第20集","path":"/wjv12/202412/11/kwDwNKsFLK84/video/index.m3u8","playUrl":"https://v12.tlkqc.com/wjv12/202412/11/kwDwNKsFLK84/video/index.m3u8"},{"title":"第21集","path":"/wjv3/202412/14/puWhwfNuBX75/video/index.m3u8","playUrl":"https://v3.tlkqc.com/wjv3/202412/14/puWhwfNuBX75/video/index.m3u8"},{"title":"第22集","path":"/wjv9/202412/14/4GsktiJv4481/video/index.m3u8","playUrl":"https://v9.tlkqc.com/wjv9/202412/14/4GsktiJv4481/video/index.m3u8"},{"title":"第23集","path":"/wjv10/202412/14/M5jsU85WM982/video/index.m3u8","playUrl":"https://v10.tlkqc.com/wjv10/202412/14/M5jsU85WM982/video/index.m3u8"},{"title":"第24集","path":"/wjv9/202412/14/8eFbCgpcJx81/video/index.m3u8","playUrl":"https://v9.tlkqc.com/wjv9/202412/14/8eFbCgpcJx81/video/index.m3u8"},{"title":"第25集","path":"/wjv12/202412/14/eU148UKebm84/video/index.m3u8","playUrl":"https://v12.tlkqc.com/wjv12/202412/14/eU148UKebm84/video/index.m3u8"},{"title":"第26集","path":"/wjv3/202412/14/ELUb7cVM2Y75/video/index.m3u8","playUrl":"https://v3.tlkqc.com/wjv3/202412/14/ELUb7cVM2Y75/video/index.m3u8"}]},{"gatherId":"d87ad93c-46cc-4952-a72f-9b90f4383076","gatherTitle":"UK","playerHost":"https://ukzy.ukubf4.com","playerPort":"0","remarks":"全26集","playList":[{"title":"第01集","path":"/20240815/lYg6tzfv/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240815/lYg6tzfv/index.m3u8"},{"title":"第02集","path":"/20240815/piGB21c6/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240815/piGB21c6/index.m3u8"},{"title":"第03集","path":"/20240815/73QS4QrB/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240815/73QS4QrB/index.m3u8"},{"title":"第04集","path":"/20240822/dFLjYvNF/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240822/dFLjYvNF/index.m3u8"},{"title":"第05集","path":"/20240828/XORHVBak/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240828/XORHVBak/index.m3u8"},{"title":"第06集","path":"/20240904/1WIVy1tz/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240904/1WIVy1tz/index.m3u8"},{"title":"第07集","path":"/20240911/qpUhvObu/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240911/qpUhvObu/index.m3u8"},{"title":"第08集","path":"/20240918/L1xX52IH/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240918/L1xX52IH/index.m3u8"},{"title":"第09集","path":"/20240925/ysBVwrCV/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20240925/ysBVwrCV/index.m3u8"},{"title":"第10集","path":"/20241002/4rY5dXSa/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241002/4rY5dXSa/index.m3u8"},{"title":"第11集","path":"/20241009/fymzkNKi/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241009/fymzkNKi/index.m3u8"},{"title":"第12集","path":"/20241016/j0vGdCuz/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241016/j0vGdCuz/index.m3u8"},{"title":"第13集","path":"/20241023/cp1PzpRD/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241023/cp1PzpRD/index.m3u8"},{"title":"第14集","path":"/20241030/yQ93nfys/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241030/yQ93nfys/index.m3u8"},{"title":"第15集","path":"/20241106/HpzybsAN/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241106/HpzybsAN/index.m3u8"},{"title":"第16集","path":"/20241113/hPcRiFVB/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241113/hPcRiFVB/index.m3u8"},{"title":"第17集","path":"/20241120/U2PrVsrA/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241120/U2PrVsrA/index.m3u8"},{"title":"第18集","path":"/20241127/IgU99p0B/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241127/IgU99p0B/index.m3u8"},{"title":"第19集","path":"/20241204/T8xpnUzw/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241204/T8xpnUzw/index.m3u8"},{"title":"第20集","path":"/20241211/QiGFusVf/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241211/QiGFusVf/index.m3u8"},{"title":"第21集","path":"/20241214/uq0bkiXR/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/uq0bkiXR/index.m3u8"},{"title":"第22集","path":"/20241214/YrxKx0Ra/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/YrxKx0Ra/index.m3u8"},{"title":"第23集","path":"/20241214/5oETwsjl/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/5oETwsjl/index.m3u8"},{"title":"第24集","path":"/20241214/sTrzlMRe/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/sTrzlMRe/index.m3u8"},{"title":"第25集","path":"/20241214/dVtuXeLF/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/dVtuXeLF/index.m3u8"},{"title":"第26集","path":"/20241214/eMmWCDH9/index.m3u8","playUrl":"https://ukzy.ukubf4.com/20241214/eMmWCDH9/index.m3u8"}]},{"gatherId":"7eb55bae-a53d-4a02-bd98-f8c36bcdcf5c","gatherTitle":"HN","playerHost":"https://hn.bfvvs.com","playerPort":"0","remarks":"第26集完结","playList":[{"title":"第1集","path":"/play/Le351wpb/index.m3u8","playUrl":"https://hn.bfvvs.com/play/Le351wpb/index.m3u8"},{"title":"第2集","path":"/play/0dNqk08b/index.m3u8","playUrl":"https://hn.bfvvs.com/play/0dNqk08b/index.m3u8"},{"title":"第3集","path":"/play/oeE5Rxle/index.m3u8","playUrl":"https://hn.bfvvs.com/play/oeE5Rxle/index.m3u8"},{"title":"第4集","path":"/play/ZdPvY2la/index.m3u8","playUrl":"https://hn.bfvvs.com/play/ZdPvY2la/index.m3u8"},{"title":"第5集","path":"/play/6dB5vXJb/index.m3u8","playUrl":"https://hn.bfvvs.com/play/6dB5vXJb/index.m3u8"},{"title":"第6集","path":"/play/vbmoWgEe/index.m3u8","playUrl":"https://hn.bfvvs.com/play/vbmoWgEe/index.m3u8"},{"title":"第7集","path":"/play/PdRx9nRa/index.m3u8","playUrl":"https://hn.bfvvs.com/play/PdRx9nRa/index.m3u8"},{"title":"第8集","path":"/play/vbmoBkEe/index.m3u8","playUrl":"https://hn.bfvvs.com/play/vbmoBkEe/index.m3u8"},{"title":"第9集","path":"/play/0dN4O1ma/index.m3u8","playUrl":"https://hn.bfvvs.com/play/0dN4O1ma/index.m3u8"},{"title":"第10集","path":"/play/7e5YKQ8e/index.m3u8","playUrl":"https://hn.bfvvs.com/play/7e5YKQ8e/index.m3u8"},{"title":"第11集","path":"/play/xboL0pBd/index.m3u8","playUrl":"https://hn.bfvvs.com/play/xboL0pBd/index.m3u8"},{"title":"第12集","path":"/play/PdR9G10b/index.m3u8","playUrl":"https://hn.bfvvs.com/play/PdR9G10b/index.m3u8"},{"title":"第13集","path":"/play/xboLw3Bd/index.m3u8","playUrl":"https://hn.bfvvs.com/play/xboLw3Bd/index.m3u8"},{"title":"第14集","path":"/play/Pe90rmxd/index.m3u8","playUrl":"https://hn.bfvvs.com/play/Pe90rmxd/index.m3u8"},{"title":"第15集","path":"/play/NbW3wpxb/index.m3u8","playUrl":"https://hn.bfvvs.com/play/NbW3wpxb/index.m3u8"},{"title":"第16集","path":"/play/QbY3XRAe/index.m3u8","playUrl":"https://hn.bfvvs.com/play/QbY3XRAe/index.m3u8"},{"title":"第17集","path":"/play/6dB3jpke/index.m3u8","playUrl":"https://hn.bfvvs.com/play/6dB3jpke/index.m3u8"},{"title":"\t 第18集","path":"/play/QbY3LYMe/index.m3u8","playUrl":"https://hn.bfvvs.com/play/QbY3LYMe/index.m3u8"},{"title":"第19集","path":"/play/xboxG5Xd/index.m3u8","playUrl":"https://hn.bfvvs.com/play/xboxG5Xd/index.m3u8"},{"title":"第20集","path":"/play/0dNzk1Ld/index.m3u8","playUrl":"https://hn.bfvvs.com/play/0dNzk1Ld/index.m3u8"},{"title":"第21集","path":"/play/QbYO7X0e/index.m3u8","playUrl":"https://hn.bfvvs.com/play/QbYO7X0e/index.m3u8"},{"title":"第22集","path":"/play/ZdPBjJza/index.m3u8","playUrl":"https://hn.bfvvs.com/play/ZdPBjJza/index.m3u8"},{"title":"第23集","path":"/play/neglpXla/index.m3u8","playUrl":"https://hn.bfvvs.com/play/neglpXla/index.m3u8"},{"title":"第24集","path":"/play/neglpX9a/index.m3u8","playUrl":"https://hn.bfvvs.com/play/neglpX9a/index.m3u8"},{"title":"第25集","path":"/play/7e5O70xb/index.m3u8","playUrl":"https://hn.bfvvs.com/play/7e5O70xb/index.m3u8"},{"title":"第26集完结","path":"/play/xbowZJzb/index.m3u8","playUrl":"https://hn.bfvvs.com/play/xbowZJzb/index.m3u8"}]}]}}
    """.trimIndent()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter = moshi.adapter(VideoDetailResponse::class.java)
    val videoItemResponse = remember { jsonAdapter.fromJson(videoItemJson) ?: VideoDetailResponse() }
    val videoDetail = videoItemResponse.data // Still needed for VideoInfo

    // MediaController setup
    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        var listener: Player.Listener? = null

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                mediaController = controller

                // Set initial title from MediaController, if possible and items are ready
                controller.currentMediaItem?.mediaId?.let { mediaId ->
                     val playingInGather = videoDetail.videoPlayList.find { gather ->
                        gather.playList.any { it.title == mediaId }
                    }
                    currentPlayTitle = playingInGather?.let { "${it.gatherTitle} - $mediaId" } ?: mediaId
                }


                listener = object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.mediaId?.let { mediaId ->
                            val playingInGather = videoDetail.videoPlayList.find { gather ->
                                gather.playList.any { it.title == mediaId }
                            }
                            currentPlayTitle = playingInGather?.let { "${it.gatherTitle} - $mediaId" } ?: mediaId
                        }
                    }
                    // Add other listener methods if needed e.g. onIsPlayingChanged
                }
                controller.addListener(listener!!)
            } catch (e: Exception) {
                // Handle error, e.g. log or show a message
            }
        }, MoreExecutors.directExecutor()) // Use directExecutor for simplicity or specify main thread executor

        onDispose {
            listener?.let { mediaController?.removeListener(it) }
            mediaController?.release()
            mediaController = null
        }
    }

    // Screen orientation
    LaunchedEffect(isFullScreen) {
        val activity = context as Activity
        activity.requestedOrientation = if (isFullScreen) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    if (isFullScreen) {
        VideoPlayer(
            player = mediaController,
            isFullScreen = isFullScreen,
            onFullScreenChange = { newValue -> isFullScreen = newValue }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = videoDetail.title) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isFullScreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "全屏")
                        }
                        // Add Record Button
                        IconButton(
                            onClick = {
                                startRecording(
                                    context = context,
                                    mediaController = mediaController,
                                    onRecordingStarted = { t -> transformer = t; isRecording = true },
                                    onRecordingCompleted = { outputPath ->
                                        isRecording = false
                                        transformer = null
                                        shareVideo(context, outputPath)
                                    },
                                    onRecordingError = { error ->
                                        isRecording = false
                                        transformer = null
                                        Toast.makeText(context, "Recording Error: $error", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isRecording && mediaController?.currentMediaItem != null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "Record Video",
                                tint = if (isRecording) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                        // Subtitle Toggle Button
                        IconButton(onClick = {
                            subtitlesEnabled = !subtitlesEnabled
                            val currentItem = mediaController?.currentMediaItem ?: return@IconButton
                            val currentPosition = mediaController?.currentPosition ?: 0L
                            val targetVideoUrl = videoDetail.videoPlayList.getOrNull(0)?.playList?.getOrNull(0)?.playUrl

                            val newMediaItemBuilder = currentItem.buildUpon()

                            if (subtitlesEnabled && currentItem.localConfiguration?.uri?.toString() == targetVideoUrl) {
                                val subtitleUri = Uri.parse("file:///android_asset/sample.sub") // Placeholder
                                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                                    .setMimeType(MimeTypes.APPLICATION_VOBSUB)
                                    .setLanguage("en")
                                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                    .build()
                                newMediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
                                Toast.makeText(context, "Subtitles ON (Placeholder for VobSub)", Toast.LENGTH_SHORT).show()
                            } else {
                                newMediaItemBuilder.setSubtitleConfigurations(emptyList())
                                if (subtitlesEnabled && currentItem.localConfiguration?.uri?.toString() != targetVideoUrl) {
                                    Toast.makeText(context, "Subtitles ON (Not target video)", Toast.LENGTH_SHORT).show()
                                } else if (!subtitlesEnabled) {
                                     Toast.makeText(context, "Subtitles OFF", Toast.LENGTH_SHORT).show()
                                }
                            }
                            val newMediaItem = newMediaItemBuilder.build()
                            mediaController?.setMediaItem(newMediaItem, currentPosition)
                            mediaController?.prepare() // Prepare is needed after setting media item
                            mediaController?.play()

                        }) {
                            Icon(
                                Icons.Filled.Subtitles,
                                contentDescription = "Toggle Subtitles",
                                tint = if (subtitlesEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                VideoPlayer(
                    player = mediaController,
                    isFullScreen = isFullScreen,
                    onFullScreenChange = { newValue -> isFullScreen = newValue }
                )
                VideoInfo(
                    video = videoDetail,
                    currentPlayTitle = currentPlayTitle,
                    onPlayItemClick = { playlistIndex, itemIndex ->
                        val selectedPlayItem = videoDetail.videoPlayList[playlistIndex].playList[itemIndex]
                        // The MediaController's playlist is managed by the service.
                        // We need to find the index based on mediaId if the service built the playlist similarly.
                        // This assumes PlayerService uses playItem.title as mediaId.
                        mediaController?.let { controller ->
                            var foundIndex = -1
                            for (i in 0 until controller.mediaItemCount) {
                                if (controller.getMediaItemAt(i).mediaId == selectedPlayItem.title) {
                                    foundIndex = i
                                    break
                                }
                            }
                            if (foundIndex != -1) {
                                controller.seekToMediaItem(foundIndex)
                                controller.play() // Start playing the selected item
                            }
                        }
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(player: Player?, isFullScreen: Boolean, onFullScreenChange: (Boolean) -> Unit) { // Changed ExoPlayer to Player?
    Box(modifier = Modifier
        .fillMaxWidth()
        .then(if (isFullScreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16f/9f))) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player // Use the passed player (MediaController)
                    useController = true
                    setFullscreenButtonClickListener { onFullScreenChange(!isFullScreen) }
                }
            },
            update = { playerView -> // Add update block to handle player changes
                playerView.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        // 添加全屏返回按钮
        if (isFullScreen) {
            IconButton(
                onClick = { onFullScreenChange(false) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "退出全屏",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
fun VideoInfo(video: VideoItem, currentPlayTitle: String, onPlayItemClick: (playlistIndex: Int, itemIndex: Int) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // 显示当前播放的标题
        if (currentPlayTitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "正在播放: $currentPlayTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                text = "评分: ${video.score}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 添加播放列表
        Text(
            text = "播放源:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // 二级分类列表
        video.videoPlayList.forEachIndexed { playlistIdx, playListGroup ->
            var isExpanded by remember { mutableStateOf(playlistIdx == 0) } // Expand first playlist by default

            Column {
                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = playListGroup.gatherTitle)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }

                if (isExpanded) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        playListGroup.playList.forEachIndexed { itemIdx, playItem ->
                            Button(
                                onClick = {
                                    onPlayItemClick(playlistIdx, itemIdx)
                                    // currentPlayTitle will be updated by the listener in VideoPlayerScreen
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = playItem.title)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoItem(title = "导演", content = video.director)
        InfoItem(title = "主演", content = video.actors)
        InfoItem(title = "地区", content = video.region)
        InfoItem(title = "语言", content = video.language)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "剧情简介",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@UnstableApi
private fun startRecording(
    context: Context,
    mediaController: MediaController?,
    onRecordingStarted: (Transformer) -> Unit,
    onRecordingCompleted: (String) -> Unit,
    onRecordingError: (String) -> Unit
) {
    val currentOriginalMediaItem = mediaController?.currentMediaItem
    if (currentOriginalMediaItem == null || currentOriginalMediaItem.localConfiguration?.uri == null) {
        onRecordingError("No valid media item to record or URI is null.")
        return
    }

    val clipStartPositionMs = mediaController.currentPosition
    val clipDurationMs = 10000L // 10 seconds
    val clipEndPositionMs = clipStartPositionMs + clipDurationMs

    val outputFileName = "recording_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
    val outputDir = File(context.cacheDir, "recordings")
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFilePath = File(outputDir, outputFileName).absolutePath

    val clippedMediaItem = MediaItem.Builder()
        .setUri(currentOriginalMediaItem.localConfiguration!!.uri) // Non-null asserted due to check above
        .setMediaId(currentOriginalMediaItem.mediaId + "_clip_${System.currentTimeMillis()}")
         // Ensure MimeType is set if known, though Transformer might infer it.
        .setMimeType(currentOriginalMediaItem.localConfiguration?.mimeType ?: MimeTypes.VIDEO_MP4) // Fallback or use actual
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clipStartPositionMs)
                .setEndPositionMs(clipEndPositionMs)
                .setStartsAtKeyFrame(true) // Important for accurate clipping
                .build()
        )
        .build()
    
    val transformerListener = object : Transformer.Listener {
        override fun onTransformationCompleted(inputMediaItem: MediaItem, result: ExportResult) {
            onRecordingCompleted(outputFilePath)
        }

        override fun onTransformationError(inputMediaItem: MediaItem, result: ExportResult?, exception: androidx.media3.transformer.ExportException) {
            onRecordingError(exception.message ?: "Unknown transformation error. Error code: ${exception.errorCode}")
        }
    }

    val transformer = Transformer.Builder(context)
        .setVideoMimeType(MimeTypes.VIDEO_H264) // Specify output format
        .setAudioMimeType(MimeTypes.AUDIO_AAC)
        .addListener(transformerListener)
        .build()

    try {
        transformer.start(clippedMediaItem, outputFilePath)
        onRecordingStarted(transformer)
    } catch (e: Exception) {
        onRecordingError("Failed to start transformer: ${e.message}")
    }
}

private fun shareVideo(context: Context, videoPath: String) {
    val videoFile = File(videoPath)
    if (!videoFile.exists()) {
        Toast.makeText(context, "Error: Recorded file not found.", Toast.LENGTH_SHORT).show()
        return
    }

    val authority = "${context.packageName}.fileprovider"
    val videoUri = FileProvider.getUriForFile(context, authority, videoFile)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, videoUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share video"))
}

@Composable
fun InfoItem(title: String, content: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}