package com.vlog.player

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vlog.player.data.model.PlayItem
import com.vlog.player.data.model.VideoItem
import com.vlog.player.data.model.VideoPlayListGroup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [29]) // Configure Robolectric for API 29
class PlayerServiceTest {

    private lateinit var context: Context
    private lateinit var service: PlayerService

    @Mock
    private lateinit var mockPlayer: ExoPlayer // Mock ExoPlayer

    @Mock
    private lateinit var mockMediaLibrarySession: MediaLibraryService.MediaLibrarySession

    @Mock
    private lateinit var mockControllerInfo: MediaSession.ControllerInfo


    @Captor
    private lateinit var mediaItemsCaptor: ArgumentCaptor<List<MediaItem>>

    // Test data similar to what's in PlayerService
    private val testVideoDetail = VideoItem(
        id = "testId",
        title = "Test Video Title",
        coverUrl = "http://example.com/cover.jpg",
        videoPlayList = listOf(
            VideoPlayListGroup(
                gatherTitle = "Test Playlist",
                playList = listOf(
                    PlayItem(title = "Episode 1", playUrl = "http://example.com/ep1.m3u8"),
                    PlayItem(title = "Episode 2", playUrl = "http://example.com/ep2.m3u8")
                )
            )
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()

        // Manually inject the mock player before onCreate is called
        service = object : PlayerService() {
            override fun onCreate() {
                // We need to assign the mockPlayer before super.onCreate() or our custom onCreate logic runs
                // However, ExoPlayer is initialized within PlayerService.onCreate.
                // So, we will let the real onCreate run but then replace the player instance.
                // This is a bit tricky. A better way would be DI or a setter for testing.
                super.onCreate() // This will create a real ExoPlayer and MediaLibrarySession
                
                // For testing specific interactions with player, we'd ideally inject it.
                // Since it's created internally, we'll test the outcome (items passed to it).
                // The 'player' field in the actual service is private, so direct replacement is hard.
                // We will rely on verifying what PlayerService passes to its internal player.
                // Or, for testing the callback, we don't strictly need to mock the service's internal player
                // if the callback logic is self-contained with the videoDetail.
            }
        }
        // Call onCreate to initialize the service components, including parsing JSON.
        // Robolectric handles service lifecycle methods if we were using a ServiceController.
        // For a direct unit test, we call it manually.
        service.onCreate()
        
        // Replace the internally created player for MediaLibrarySession callback tests if needed,
        // or ensure videoDetail is populated for the callback.
        // For now, we'll assume videoDetail is populated by service.onCreate() and test the callback directly.
    }

    @Test
    fun onCreate_parsesJsonAndPreparesPlayer() {
        // PlayerService's onCreate internally creates an ExoPlayer instance and sets media items.
        // To verify this, we would ideally inject a mock ExoPlayer.
        // Since PlayerService creates its own ExoPlayer, we can't directly verify calls on a mock.
        // Instead, we'll test the MediaLibrarySession.Callback which relies on the parsed data.
        // This indirectly confirms that data parsing happened.
        
        // A more direct test would require refactoring PlayerService for DI or a test seam.
        // For this exercise, we'll focus on the callback logic that uses the parsed data.
        assertNotNull(service.getTestableVideoDetail()) // Assuming a getter for testing
        assertEquals(testVideoDetail.title, service.getTestableVideoDetail()?.title)
        assertEquals(2, service.getTestableVideoDetail()?.videoPlayList?.get(0)?.playList?.size)
    }

    @Test
    fun onGetLibraryRoot_returnsCorrectRootMediaItem() {
        val callback = service.getTestableMediaLibrarySessionCallback() // Assuming a getter
        val future = callback.onGetLibraryRoot(mockMediaLibrarySession, mockControllerInfo, null)
        val result = future.get()

        assertNotNull(result)
        assertTrue(result.resultCode == LibraryResult.RESULT_SUCCESS)
        val rootItem = result.value
        assertNotNull(rootItem)
        assertEquals("root_id", rootItem!!.mediaId)
        assertEquals("Vlog Player Library", rootItem.mediaMetadata.title)
        assertFalse(rootItem.mediaMetadata.isPlayable ?: true)
        assertTrue(rootItem.mediaMetadata.isBrowsable ?: false)
    }

    @Test
    fun onGetChildren_forRootId_returnsCorrectChildren() {
        val callback = service.getTestableMediaLibrarySessionCallback()
        // Ensure videoDetail is set in the service for the callback to use
        service.setTestableVideoDetail(testVideoDetail) // Simulate data loading

        val future = callback.onGetChildren(mockMediaLibrarySession, mockControllerInfo, "root_id", 0, 10, null)
        val result = future.get()

        assertNotNull(result)
        assertTrue(result.resultCode == LibraryResult.RESULT_SUCCESS)
        val children = result.value
        assertNotNull(children)
        assertEquals(2, children!!.size)

        val firstChild = children[0]
        assertEquals(testVideoDetail.videoPlayList[0].playList[0].playUrl, firstChild.mediaId)
        assertEquals(testVideoDetail.videoPlayList[0].playList[0].title, firstChild.mediaMetadata.title)
        assertEquals(testVideoDetail.title, firstChild.mediaMetadata.artist) // Main title as artist
        assertEquals(testVideoDetail.coverUrl.toUri(), firstChild.mediaMetadata.artworkUri)
        assertTrue(firstChild.mediaMetadata.isPlayable ?: false)
        assertFalse(firstChild.mediaMetadata.isBrowsable ?: true)

        val secondChild = children[1]
        assertEquals(testVideoDetail.videoPlayList[0].playList[1].playUrl, secondChild.mediaId)
        assertEquals(testVideoDetail.videoPlayList[0].playList[1].title, secondChild.mediaMetadata.title)
    }

    @Test
    fun onGetChildren_forInvalidParentId_returnsError() {
        val callback = service.getTestableMediaLibrarySessionCallback()
        val future = callback.onGetChildren(mockMediaLibrarySession, mockControllerInfo, "invalid_id", 0, 10, null)
        val result = future.get()

        assertNotNull(result)
        assertEquals(LibraryResult.RESULT_ERROR_BAD_VALUE, result.resultCode)
        assertNull(result.value)
    }

    // Helper extension in PlayerService to access videoDetail for test verification
    // This is a common pattern for testing internal state without full DI.
    // Add this to PlayerService.kt:
    // internal fun getTestableVideoDetail(): VideoItem? = this.videoDetail
    // internal fun getTestableMediaLibrarySessionCallback(): MediaLibrarySession.Callback = MyMediaLibrarySessionCallback()
    // internal fun setTestableVideoDetail(videoDetail: VideoItem?) { this.videoDetail = videoDetail }

}

// Add to PlayerService.kt for testability:
/*
@UnstableApi
class PlayerService : MediaLibraryService() {
    // ... existing code ...
    private var videoDetail: VideoItem? = null // Already exists

    // ... existing code ...

    // Make videoDetail accessible for tests
    internal fun getTestableVideoDetail(): VideoItem? = this.videoDetail
    
    // Make callback instance accessible for tests
    internal fun getTestableMediaLibrarySessionCallback(): MediaLibrarySession.Callback = MyMediaLibrarySessionCallback()

    // Allow setting videoDetail for tests, to decouple from JSON parsing in specific tests
    internal fun setTestableVideoDetail(detail: VideoItem?) {
        this.videoDetail = detail
    }
    
    private inner class MyMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        // ... existing callback code ...
    }
    // ... rest of the service code ...
}
*/
