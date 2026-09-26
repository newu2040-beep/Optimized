package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class OptimizedProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalFilename: String,
    val originalUri: String,
    val thumbnailPath: String?,
    val sourceResolution: String,
    val sourceFps: Float,
    val sourceBitrateKbps: Long,
    val sourceDurationMs: Long,
    val sourceCodec: String,
    val sourceAudioCodec: String?,
    val destinationPlatform: String,
    val qualityMode: String,
    val exportPath: String?,
    val exportSizeBytes: Long?,
    val exportResolution: String?,
    val exportFps: Float?,
    val timestamp: Long = System.currentTimeMillis(),
    val presetName: String? = null,
    val status: String = "Completed"
)

@Entity(tableName = "presets")
data class ExportPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val platform: String,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val fps: Int,
    val videoCodec: String,
    val bitrateKbps: Int,
    val audioCodec: String,
    val audioBitrateKbps: Int,
    val isDefault: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
