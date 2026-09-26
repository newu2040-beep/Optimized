package com.example

import com.example.domain.model.CanvasMode
import com.example.domain.model.ExportSettings
import com.example.domain.model.PlatformDestination
import com.example.domain.model.QualityMode
import com.example.domain.model.VideoMetadata
import com.example.engine.PlatformProfileEngine
import com.example.engine.SmartOptimizationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPlatformProfileDefaults() {
        val tiktok = PlatformProfileEngine.getProfile(PlatformDestination.TIKTOK)
        assertEquals(1080, tiktok.targetWidth)
        assertEquals(1920, tiktok.targetHeight)
        assertEquals("H.264", tiktok.targetVideoCodec)
        assertEquals("AAC", tiktok.targetAudioCodec)

        val reels = PlatformProfileEngine.getProfile(PlatformDestination.INSTAGRAM_REELS)
        assertEquals(1080, reels.targetWidth)
        assertEquals(1920, reels.targetHeight)
        assertEquals(30, reels.maxFps)

        val whatsapp = PlatformProfileEngine.getProfile(PlatformDestination.WHATSAPP_STATUS)
        assertEquals(720, whatsapp.targetWidth)
        assertEquals(1280, whatsapp.targetHeight)
    }

    @Test
    fun testSmartOptimizationDirectExport() {
        val compatibleSource = VideoMetadata(
            filename = "perfect_tiktok.mp4",
            uriString = "content://media/1",
            sizeBytes = 25 * 1024 * 1024L,
            durationMs = 15000L,
            width = 1080,
            height = 1920,
            fps = 30.0f,
            videoCodec = "H.264 (AVC)",
            videoBitrateBps = 12_000_000L,
            isHdr = false,
            colorSpace = "BT.709",
            rotationDegrees = 0,
            container = "MP4",
            audioCodec = "AAC",
            audioBitrateBps = 192_000L,
            audioSampleRate = 44100,
            audioChannels = 2
        )

        val settings = ExportSettings(
            platform = PlatformDestination.TIKTOK,
            qualityMode = QualityMode.BALANCED,
            canvasMode = CanvasMode.FIT,
            targetWidth = 1080,
            targetHeight = 1920,
            targetFps = 30,
            targetVideoCodec = "H.264",
            targetBitrateKbps = 12000,
            targetAudioCodec = "AAC",
            targetAudioBitrateKbps = 192,
            removeAudio = false,
            normalizeAudio = false,
            hdrToSdrConversion = false
        )

        val plan = SmartOptimizationEngine.generatePlan(compatibleSource, settings)

        assertTrue("Plan should be ready for direct export without re-encoding", plan.isReadyToExportDirectly)
        assertFalse("Resolution change should not be required", plan.requiresResolutionChange)
        assertFalse("Codec change should not be required", plan.requiresCodecChange)
    }

    @Test
    fun testSmartOptimizationRequiresReEncode() {
        val landscapeHevcSource = VideoMetadata(
            filename = "landscape_4k.mov",
            uriString = "content://media/2",
            sizeBytes = 200 * 1024 * 1024L,
            durationMs = 30000L,
            width = 3840,
            height = 2160,
            fps = 60.0f,
            videoCodec = "H.265 (HEVC)",
            videoBitrateBps = 50_000_000L,
            isHdr = true,
            colorSpace = "BT.2020",
            rotationDegrees = 0,
            container = "MOV",
            audioCodec = "PCM",
            audioBitrateBps = 1500_000L,
            audioSampleRate = 48000,
            audioChannels = 2
        )

        val settings = PlatformProfileEngine.createInitialSettings(landscapeHevcSource, PlatformDestination.INSTAGRAM_REELS)
        val plan = SmartOptimizationEngine.generatePlan(landscapeHevcSource, settings)

        assertFalse("Direct export should NOT be allowed for 4k HEVC MOV to Instagram", plan.isReadyToExportDirectly)
        assertTrue("Resolution change should be required", plan.requiresResolutionChange)
        assertTrue("Codec change should be required", plan.requiresCodecChange)
        assertTrue("Estimated output size should be smaller than 4k source", plan.estimatedOutputSizeBytes < landscapeHevcSource.sizeBytes)
        assertTrue("Estimated reduction percent should be positive", plan.estimatedReductionPercent > 0)
    }

    @Test
    fun testPermissionStatusStateCalculations() {
        val noneGranted = com.example.ui.util.PermissionStatusState(
            hasNotifications = false,
            hasGalleryMedia = false,
            hasFullFilesAccess = false
        )
        assertFalse(noneGranted.hasAllGranted)
        assertEquals(0, noneGranted.grantedCount)
        assertEquals(3, noneGranted.totalCount)

        val partialGranted = com.example.ui.util.PermissionStatusState(
            hasNotifications = true,
            hasGalleryMedia = true,
            hasFullFilesAccess = false
        )
        assertFalse(partialGranted.hasAllGranted)
        assertEquals(2, partialGranted.grantedCount)

        val allGranted = com.example.ui.util.PermissionStatusState(
            hasNotifications = true,
            hasGalleryMedia = true,
            hasFullFilesAccess = true
        )
        assertTrue(allGranted.hasAllGranted)
        assertEquals(3, allGranted.grantedCount)
    }

    @Test
    fun testQualityModeMultipliers() {
        assertTrue(QualityMode.ULTRA_COMPRESSION.bitrateMultiplier < QualityMode.BALANCED.bitrateMultiplier)
        assertTrue(QualityMode.DISCORD_LIMIT.bitrateMultiplier < QualityMode.BALANCED.bitrateMultiplier)
        assertTrue(QualityMode.WHATSAPP_LIMIT.bitrateMultiplier < QualityMode.BALANCED.bitrateMultiplier)
        assertTrue(QualityMode.PRO_CINEMA.bitrateMultiplier > QualityMode.MAXIMUM_QUALITY.bitrateMultiplier)
    }

    @Test
    fun testExpandedThemes() {
        val themes = com.example.data.preferences.ThemeStyle.values()
        assertTrue(themes.size >= 12)
        assertTrue(themes.contains(com.example.data.preferences.ThemeStyle.CYBERPUNK))
        assertTrue(themes.contains(com.example.data.preferences.ThemeStyle.SUNSET))
        assertTrue(themes.contains(com.example.data.preferences.ThemeStyle.EMERALD))
        assertTrue(themes.contains(com.example.data.preferences.ThemeStyle.OCEANIC))
        assertTrue(themes.contains(com.example.data.preferences.ThemeStyle.AMOLED))
    }
}
