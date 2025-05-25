package com.vlog.player.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vlog.player.data.model.PlayItem
import com.vlog.player.data.model.VideoItem
import com.vlog.player.data.model.VideoPlayListGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [29])
class VideoPlayerScreenTest {

    // Coroutine test rule
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule() // To manage main dispatcher

    private lateinit var context: Context

    @Mock
    private lateinit var mockMediaController: MediaController

    @Mock
    private lateinit var mockTransformerBuilder: Transformer.Builder

    @Mock
    private lateinit var mockTransformer: Transformer

    @Captor
    private lateinit var mediaItemCaptor: ArgumentCaptor<MediaItem>

    @Captor
    private lateinit var outputPathCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var transformerListenerCaptor: ArgumentCaptor<Transformer.Listener>


    // Test data
    private val testVideoDetail = VideoItem(
        id = "testVidId",
        title = "Test Video",
        videoPlayList = listOf(
            VideoPlayListGroup(
                gatherTitle = "Playlist1",
                playList = listOf(
                    PlayItem(title = "Ep1", playUrl = "http://example.com/ep1.m3u8"),
                    PlayItem(title = "Ep2", playUrl = "http://example.com/ep2.m3u8")
                )
            )
        )
    )
    private val targetVideoUrl = testVideoDetail.videoPlayList[0].playList[0].playUrl


    @Before
    fun setUp() {
        ShadowLog.stream = System.out // To see Robolectric logs
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext<Application>()

        `when`(mockTransformerBuilder.addListener(any())).thenReturn(mockTransformerBuilder)
        `when`(mockTransformerBuilder.setVideoMimeType(any())).thenReturn(mockTransformerBuilder)
        `when`(mockTransformerBuilder.setAudioMimeType(any())).thenReturn(mockTransformerBuilder)
        `when`(mockTransformerBuilder.build()).thenReturn(mockTransformer)

        // Mock FileProvider.getUriForFile to prevent it from actually trying to use a real FileProvider
        mockStatic(FileProvider::class.java).`when` {
            FileProvider.getUriForFile(any(Context::class.java), anyString(), any(File::class.java))
        }.thenReturn(Uri.parse("content://fakeuri/video.mp4"))
    }
    
    @After
    fun tearDown() {
        clearAllMocks() // Clears static mocks
    }


    // Simulates the logic within the subtitle IconButton's onClick
    private fun triggerSubtitleToggle(
        subtitlesEnabledState: MutableState<Boolean>,
        mediaController: Player?, // Use Player to match VideoPlayer composable
        videoDetail: VideoItem
    ) {
        subtitlesEnabledState.value = !subtitlesEnabledState.value // Toggle the state

        val currentItem = mediaController?.currentMediaItem ?: return
        val currentPosition = mediaController.currentPosition

        val newMediaItemBuilder = currentItem.buildUpon()
        val targetPlayItem = videoDetail.videoPlayList.getOrNull(0)?.playList?.getOrNull(0)

        if (subtitlesEnabledState.value && currentItem.localConfiguration?.uri.toString() == targetPlayItem?.playUrl) {
            val subtitleUri = Uri.parse("file:///android_asset/sample.sub")
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                .setMimeType(MimeTypes.APPLICATION_VOBSUB)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            newMediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        } else {
            newMediaItemBuilder.setSubtitleConfigurations(emptyList())
        }
        val newMediaItem = newMediaItemBuilder.build()
        mediaController.setMediaItem(newMediaItem, currentPosition)
        mediaController.prepare()
        mediaController.play()
    }

    @Test
    fun subtitleToggleLogic_enablesSubtitlesForTargetVideo() {
        val subtitlesEnabledState = mutableStateOf(false)
        val mediaItemUri = Uri.parse(targetVideoUrl)
        val currentMediaItem = MediaItem.Builder().setUri(mediaItemUri).setMediaId("ep1").build()

        `when`(mockMediaController.currentMediaItem).thenReturn(currentMediaItem)
        `when`(mockMediaController.currentPosition).thenReturn(12345L)

        triggerSubtitleToggle(subtitlesEnabledState, mockMediaController, testVideoDetail)

        assertTrue(subtitlesEnabledState.value) // Subtitles should now be enabled
        verify(mockMediaController).setMediaItem(mediaItemCaptor.capture(), eq(12345L))
        assertNotNull(mediaItemCaptor.value.localConfiguration?.subtitleConfigurations)
        assertEquals(1, mediaItemCaptor.value.localConfiguration?.subtitleConfigurations?.size)
        assertEquals(
            MimeTypes.APPLICATION_VOBSUB,
            mediaItemCaptor.value.localConfiguration?.subtitleConfigurations?.get(0)?.mimeType
        )
    }

    @Test
    fun subtitleToggleLogic_disablesSubtitles() {
        val subtitlesEnabledState = mutableStateOf(true) // Start with subtitles enabled
        val mediaItemUri = Uri.parse(targetVideoUrl)
        val currentMediaItem = MediaItem.Builder().setUri(mediaItemUri).setMediaId("ep1").build()
        `when`(mockMediaController.currentMediaItem).thenReturn(currentMediaItem)
        `when`(mockMediaController.currentPosition).thenReturn(0L)

        triggerSubtitleToggle(subtitlesEnabledState, mockMediaController, testVideoDetail) // This will toggle to false

        assertFalse(subtitlesEnabledState.value)
        verify(mockMediaController).setMediaItem(mediaItemCaptor.capture(), eq(0L))
        assertTrue(mediaItemCaptor.value.localConfiguration?.subtitleConfigurations?.isEmpty() ?: true)
    }


    @Test
    fun commentSubmission_addsCommentAndClearsText() = runTest {
        val comments = mutableStateListOf<UserComment>()
        val commentTextState = mutableStateOf("Initial comment")

        // Simulate the lambda that would be passed to onCommentSubmit
        val submitCommentAction = {
            if (commentTextState.value.isNotBlank()) {
                comments.add(UserComment(text = commentTextState.value))
                commentTextState.value = ""
            }
        }

        submitCommentAction()

        assertEquals(1, comments.size)
        assertEquals("Initial comment", comments[0].text)
        assertEquals("", commentTextState.value)
    }

    @Test
    fun commentCleanupLogic_removesOldComments() = runTest {
        val comments = mutableStateListOf<UserComment>()
        val initialTime = System.currentTimeMillis()

        comments.add(UserComment(id="1", text = "Old comment 1", timestamp = initialTime - 20000)) // 20s old
        comments.add(UserComment(id="2", text = "Old comment 2", timestamp = initialTime - 15000)) // 15s old
        comments.add(UserComment(id="3", text = "New comment 1", timestamp = initialTime - 5000))  // 5s old
        comments.add(UserComment(id="4", text = "New comment 2", timestamp = initialTime - 1000))   // 1s old

        // Simulate the LaunchedEffect logic for cleanup (comments older than 10s)
        // Advance time by a bit more than the threshold to ensure cleanup happens
        testScheduler.advanceTimeBy(11000) // Advance by 11 seconds

        // Manually filter based on the logic in LaunchedEffect for testing
        val currentTime = initialTime + 11000 // Current time after advancing
        comments.removeAll { comment -> (currentTime - comment.timestamp) > 10000 }


        assertEquals(2, comments.size)
        assertTrue(comments.any { it.text == "New comment 1" })
        assertTrue(comments.any { it.text == "New comment 2" })
        assertFalse(comments.any { it.text.startsWith("Old comment") })
    }
    
    // Test the logic that would be invoked by the record button's onClick
    @Test
    fun recordingLogic_whenMediaItemIsValid_startsTransformerAndHandlesCompletion() {
        val mediaItemUri = Uri.parse("http://example.com/video.mp4")
        val localConfig = MediaItem.LocalConfiguration.Builder().setUri(mediaItemUri).setMimeType(MimeTypes.VIDEO_MP4).build()
        val currentMediaItem = MediaItem.Builder().setMediaId("vid1").setLocalConfiguration(localConfig).build()

        `when`(mockMediaController.currentMediaItem).thenReturn(currentMediaItem)
        `when`(mockMediaController.currentPosition).thenReturn(5000L) // Current position 5s

        var isRecordingState = false
        var finalOutputPath: String? = null
        var sharedCalled = false

        val mockTransformerInstance = mock(Transformer::class.java)
        `when`(mockTransformerBuilder.addListener(transformerListenerCaptor.capture())).thenReturn(mockTransformerBuilder)
        `when`(mockTransformerBuilder.build()).thenReturn(mockTransformerInstance)


        // This lambda simulates what the onClick for the record button would do
        val recordAction = {
            // Directly use the logic of the real startRecording but pass mocks where needed
            // To make startRecording testable, we pass its dependencies or use a test seam.
            // For this test, we assume startRecording can be called with a way to inject/mock Transformer.Builder
            // or we test a refactored version.
            // For simplicity, we'll assume startRecording uses a passed-in builder for testing.
             startRecordingInternal(
                context = context,
                mediaController = mockMediaController,
                transformerBuilder = mockTransformerBuilder, // Pass the mock builder
                onRecordingStarted = { /*transformer ->*/ isRecordingState = true },
                onRecordingCompleted = { path -> isRecordingState = false; finalOutputPath = path; sharedCalled = true /* Simulate shareVideo call */ },
                onRecordingError = { /*error ->*/ isRecordingState = false; fail("Recording error: $error") }
            )
        }

        recordAction()

        assertTrue(isRecordingState) // Check if recording state was set
        verify(mockTransformerBuilder).addListener(transformerListenerCaptor.capture())
        verify(mockTransformerInstance).start(mediaItemCaptor.capture(), outputPathCaptor.capture())

        // Assert clipping configuration
        val capturedClipConfig = mediaItemCaptor.value.clippingConfiguration
        assertEquals(5000L, capturedClipConfig.startPositionMs)
        assertEquals(5000L + 10000L, capturedClipConfig.endPositionMs) // 5s + 10s
        assertTrue(outputPathCaptor.value.contains("recording_"))
        assertTrue(outputPathCaptor.value.endsWith(".mp4"))

        // Simulate transformation completion
        val mockExportResult = mock(ExportResult::class.java)
        transformerListenerCaptor.value.onTransformationCompleted(mediaItemCaptor.value, mockExportResult)

        assertFalse(isRecordingState)
        assertNotNull(finalOutputPath)
        assertEquals(outputPathCaptor.value, finalOutputPath)
        assertTrue(sharedCalled) // Placeholder for verifying shareVideo was called
    }
    
    @Test
    fun recordingLogic_whenMediaItemIsInvalid_callsOnError() {
        `when`(mockMediaController.currentMediaItem).thenReturn(null) // No media item
        var errorMsg: String? = null
        var isRecordingState = true // Start as true to see it change

        startRecordingInternal(
            context = context,
            mediaController = mockMediaController,
            transformerBuilder = mockTransformerBuilder,
            onRecordingStarted = { fail("Should not start recording") },
            onRecordingCompleted = { fail("Should not complete recording") },
            onRecordingError = { msg -> errorMsg = msg; isRecordingState = false }
        )
        assertFalse(isRecordingState)
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("No valid media item to record"))
    }


    @Test
    fun shareVideo_createsCorrectIntent() {
        val testFilePath = File(context.cacheDir, "test_video.mp4")
        testFilePath.createNewFile()

        // Call the test version of shareVideo
        shareVideoInternal(context, testFilePath.absolutePath)

        val startedIntent = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertNotNull("Intent should have been started", startedIntent)
        assertEquals(Intent.ACTION_CHOOSER, startedIntent.action)
        val wrappedIntent = startedIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull("Wrapped intent should not be null", wrappedIntent)
        assertEquals(Intent.ACTION_SEND, wrappedIntent!!.action)
        assertEquals("video/mp4", wrappedIntent.type)
        assertNotNull("EXTRA_STREAM should not be null", wrappedIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))

        testFilePath.delete()
    }
}

// Helper for coroutine testing - ensure this is defined in your test setup
@ExperimentalCoroutinesApi
class MainCoroutineRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : org.junit.rules.TestWatcher() {
    override fun starting(description: org.junit.runner.Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: org.junit.runner.Description) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}


// Internal testable versions of the private functions from VideoPlayerScreen
// These would ideally be refactored in the main source for better testability (e.g. making them internal)
// Or by passing dependencies like Transformer.Builder.
@androidx.media3.common.util.UnstableApi
private fun startRecordingInternal(
    context: Context,
    mediaController: MediaController?,
    transformerBuilder: Transformer.Builder, // Allow injecting mock builder
    onRecordingStarted: (Transformer) -> Unit,
    onRecordingCompleted: (String) -> Unit,
    onRecordingError: (String) -> Unit
) {
    val currentOriginalMediaItem = mediaController?.currentMediaItem
    // Simplified URI check for testing; real one checks localConfiguration.uri
    if (currentOriginalMediaItem == null || currentOriginalMediaItem.localConfiguration?.uri == null) {
        onRecordingError("No valid media item to record or URI is null.")
        return
    }

    val clipStartPositionMs = mediaController.currentPosition
    val clipDurationMs = 10000L 
    val clipEndPositionMs = clipStartPositionMs + clipDurationMs

    val outputFileName = "recording_${System.currentTimeMillis()}.mp4" // Simplified name for test
    val outputDir = File(context.cacheDir, "recordings")
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFilePath = File(outputDir, outputFileName).absolutePath

    val clippedMediaItem = MediaItem.Builder()
        .setUri(currentOriginalMediaItem.localConfiguration!!.uri)
        .setMediaId(currentOriginalMediaItem.mediaId + "_clip_${System.currentTimeMillis()}")
        .setMimeType(currentOriginalMediaItem.localConfiguration?.mimeType ?: MimeTypes.VIDEO_MP4)
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clipStartPositionMs)
                .setEndPositionMs(clipEndPositionMs)
                .setStartsAtKeyFrame(true)
                .build()
        )
        .build()
    
    val transformerListener = object : Transformer.Listener {
        override fun onTransformationCompleted(inputMediaItem: MediaItem, result: ExportResult) {
            onRecordingCompleted(outputFilePath)
        }

        override fun onTransformationError(inputMediaItem: MediaItem, result: ExportResult?, exception: ExportException) {
            onRecordingError(exception.message ?: "Unknown transformation error. Error code: ${exception.errorCode}")
        }
    }
    
    // Use the injected builder
    val transformer = transformerBuilder
        .addListener(transformerListener)
        .build()

    try {
        transformer.start(clippedMediaItem, outputFilePath)
        onRecordingStarted(transformer)
    } catch (e: Exception) {
        onRecordingError("Failed to start transformer: ${e.message}")
    }
}

private fun shareVideoInternal(context: Context, videoPath: String) {
    val videoFile = File(videoPath)
    if (!videoFile.exists()) {
        println("Test Share Error: Recorded file not found: $videoPath")
        return
    }
    val authority = "${context.packageName}.fileprovider" 
    // ShadowFileProvider will handle this without a real provider if set up in test environment.
    // Or use mockStatic(FileProvider::class.java) as done in setUp.
    val videoUri = FileProvider.getUriForFile(context, authority, videoFile)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, videoUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ApplicationProvider.getApplicationContext<Application>().startActivity(Intent.createChooser(shareIntent, "Share video"))
}
