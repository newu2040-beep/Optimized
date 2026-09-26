package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExportPreset
import com.example.data.local.entity.OptimizedProject
import com.example.data.preferences.ThemeStyle
import com.example.data.preferences.UserPreferences
import com.example.data.repository.OptimizedRepository
import com.example.domain.model.ExportSettings
import com.example.domain.model.ExportStage
import com.example.domain.model.OptimizationPlan
import com.example.domain.model.PlatformDestination
import com.example.domain.model.QualityMode
import com.example.domain.model.VideoMetadata
import com.example.engine.ExportValidator
import com.example.engine.PlatformProfileEngine
import com.example.engine.SmartOptimizationEngine
import com.example.engine.StorageManager
import com.example.engine.StorageStats
import com.example.engine.VideoAnalyzer
import com.example.engine.VideoTranscoder
import com.example.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class SubScreen {
    NONE,
    OPTIMIZE,
    EXPORT_PROGRESS,
    MULTI_PLATFORM,
    BATCH_OPTIMIZE,
    TRIM_TOOL,
    CROP_TOOL,
    COMPRESS_TOOL,
    VIDEO_INFO_TOOL,
    PRESETS_TOOL
}

data class MultiPlatformItem(
    val platform: PlatformDestination,
    val stage: String = "Waiting", // Waiting, Optimizing, Complete, Failed
    val progress: Int = 0,
    val outputFile: File? = null
)

data class BatchVideoItem(
    val id: String,
    val uri: Uri,
    val metadata: VideoMetadata? = null,
    val status: String = "Queued", // Queued, Processing, Completed, Failed
    val progress: Int = 0,
    val outputFile: File? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = OptimizedRepository(database.projectDao(), database.presetDao())
    val preferences = UserPreferences(application)

    val allProjects: StateFlow<List<OptimizedProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentProjects: StateFlow<List<OptimizedProject>> = repository.getRecentProjects(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<ExportPreset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeStyle: StateFlow<ThemeStyle> = preferences.themeStyle

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    // Active Video Analysis & Plan
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _activeMetadata = MutableStateFlow<VideoMetadata?>(null)
    val activeMetadata: StateFlow<VideoMetadata?> = _activeMetadata.asStateFlow()

    private val _activeSettings = MutableStateFlow(ExportSettings())
    val activeSettings: StateFlow<ExportSettings> = _activeSettings.asStateFlow()

    private val _activePlan = MutableStateFlow<OptimizationPlan?>(null)
    val activePlan: StateFlow<OptimizationPlan?> = _activePlan.asStateFlow()

    // Export progress state
    private val _exportStage = MutableStateFlow<ExportStage>(ExportStage.Idle)
    val exportStage: StateFlow<ExportStage> = _exportStage.asStateFlow()

    private var exportJob: Job? = null
    @Volatile
    private var isCancelled = false

    // Multi-Platform export state
    private val _multiPlatformQueue = MutableStateFlow<List<MultiPlatformItem>>(emptyList())
    val multiPlatformQueue: StateFlow<List<MultiPlatformItem>> = _multiPlatformQueue.asStateFlow()

    // Batch optimization queue
    private val _batchQueue = MutableStateFlow<List<BatchVideoItem>>(emptyList())
    val batchQueue: StateFlow<List<BatchVideoItem>> = _batchQueue.asStateFlow()

    // Storage info
    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    // Error snackbar message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Gallery Save Feedback
    private val _gallerySaveStatus = MutableStateFlow<String?>(null)
    val gallerySaveStatus: StateFlow<String?> = _gallerySaveStatus.asStateFlow()

    fun saveVideoToDeviceGallery(file: File, customTitle: String? = null) {
        viewModelScope.launch {
            _gallerySaveStatus.value = "Saving to Gallery..."
            val result = StorageManager.saveVideoToGallery(getApplication(), file, customTitle)
            result.onSuccess { uri ->
                _gallerySaveStatus.value = "Saved to Gallery (Movies/Optimized) ✓"
                val current = _exportStage.value
                if (current is ExportStage.Completed) {
                    _exportStage.value = current.copy(galleryUri = uri.toString())
                }
            }.onFailure { err ->
                _gallerySaveStatus.value = "Failed to save: ${err.message}"
            }
        }
    }

    fun clearGallerySaveStatus() {
        _gallerySaveStatus.value = null
    }

    init {
        refreshStorageStats()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _currentSubScreen.value = SubScreen.NONE
    }

    fun navigateToSubScreen(subScreen: SubScreen) {
        _currentSubScreen.value = subScreen
    }

    fun backToHome() {
        _currentSubScreen.value = SubScreen.NONE
        _currentScreen.value = Screen.HOME
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val count = repository.getProjectCount()
            _storageStats.value = StorageManager.getStorageStats(getApplication(), count)
        }
    }

    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = VideoAnalyzer.analyze(getApplication(), uri)
            _isAnalyzing.value = false

            result.onSuccess { metadata ->
                _activeMetadata.value = metadata
                val initialSettings = PlatformProfileEngine.createInitialSettings(
                    metadata,
                    PlatformDestination.TIKTOK
                )
                _activeSettings.value = initialSettings
                _activePlan.value = SmartOptimizationEngine.generatePlan(metadata, initialSettings)
                _currentSubScreen.value = SubScreen.OPTIMIZE
            }.onFailure { err ->
                _errorMessage.value = "Could not analyze video. File format may be unsupported."
            }
        }
    }

    fun selectPlatform(platform: PlatformDestination) {
        val metadata = _activeMetadata.value ?: return
        val current = _activeSettings.value
        val profile = PlatformProfileEngine.getProfile(platform)

        val updated = current.copy(
            platform = platform,
            targetWidth = profile.targetWidth,
            targetHeight = profile.targetHeight,
            targetFps = if (metadata.fps > 0 && metadata.fps <= profile.maxFps) Math.round(metadata.fps) else profile.maxFps,
            targetVideoCodec = profile.targetVideoCodec,
            targetBitrateKbps = profile.calculateBitrate(current.qualityMode),
            targetAudioCodec = profile.targetAudioCodec,
            targetAudioBitrateKbps = profile.targetAudioBitrateKbps
        )
        _activeSettings.value = updated
        _activePlan.value = SmartOptimizationEngine.generatePlan(metadata, updated)
    }

    fun updateSettings(newSettings: ExportSettings) {
        val metadata = _activeMetadata.value ?: return
        _activeSettings.value = newSettings
        _activePlan.value = SmartOptimizationEngine.generatePlan(metadata, newSettings)
    }

    fun startExport() {
        val metadata = _activeMetadata.value ?: return
        val plan = _activePlan.value ?: return
        val sourceUri = Uri.parse(metadata.uriString)

        isCancelled = false
        _currentSubScreen.value = SubScreen.EXPORT_PROGRESS

        val autoSave = preferences.autoSaveToGallery.value
        val effectivePlan = plan.copy(exportSettings = plan.exportSettings.copy(saveToGalleryOnCompletion = autoSave))

        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val result = VideoTranscoder.transcode(
                context = getApplication(),
                sourceUri = sourceUri,
                plan = effectivePlan,
                onProgress = { stage -> _exportStage.value = stage },
                isCancelled = { isCancelled }
            )

            result.onSuccess { outputFile ->
                // Record project into Room
                val stage = _exportStage.value
                val validationSize = if (stage is ExportStage.Completed) stage.outputSizeBytes else outputFile.length()
                val valWidth = if (stage is ExportStage.Completed) stage.width else plan.exportSettings.targetWidth
                val valHeight = if (stage is ExportStage.Completed) stage.height else plan.exportSettings.targetHeight
                val valFps = if (stage is ExportStage.Completed) stage.fps else plan.exportSettings.targetFps.toFloat()

                val project = OptimizedProject(
                    originalFilename = metadata.filename,
                    originalUri = metadata.uriString,
                    thumbnailPath = metadata.thumbnailPath,
                    sourceResolution = metadata.resolutionLabel,
                    sourceFps = metadata.fps,
                    sourceBitrateKbps = metadata.videoBitrateBps / 1000,
                    sourceDurationMs = metadata.durationMs,
                    sourceCodec = metadata.videoCodec,
                    sourceAudioCodec = metadata.audioCodec,
                    destinationPlatform = plan.exportSettings.platform.displayName,
                    qualityMode = plan.exportSettings.qualityMode.displayName,
                    exportPath = outputFile.absolutePath,
                    exportSizeBytes = validationSize,
                    exportResolution = "$valWidth × $valHeight",
                    exportFps = valFps,
                    status = "Completed"
                )
                repository.saveProject(project)
                refreshStorageStats()
            }.onFailure { e ->
                if (!isCancelled) {
                    _exportStage.value = ExportStage.Failed(
                        humanReadableError = "Processing failed: ${e.message ?: "Unknown encoding error"}",
                        actionableStep = "Try Balanced quality mode or check free storage."
                    )
                }
            }
        }
    }

    fun cancelExport() {
        isCancelled = true
        exportJob?.cancel()
        _exportStage.value = ExportStage.Failed("Export cancelled by user.", "Select a video to start again.")
    }

    fun startMultiPlatformExport() {
        val metadata = _activeMetadata.value ?: return
        val sourceUri = Uri.parse(metadata.uriString)

        val platforms = listOf(
            PlatformDestination.TIKTOK,
            PlatformDestination.INSTAGRAM_REELS,
            PlatformDestination.YOUTUBE_SHORTS,
            PlatformDestination.SNAPCHAT,
            PlatformDestination.WHATSAPP_STATUS
        )

        _multiPlatformQueue.value = platforms.map { MultiPlatformItem(it, "Waiting", 0) }
        _currentSubScreen.value = SubScreen.MULTI_PLATFORM

        viewModelScope.launch {
            platforms.forEachIndexed { index, platform ->
                _multiPlatformQueue.value = _multiPlatformQueue.value.mapIndexed { idx, item ->
                    if (idx == index) item.copy(stage = "Optimizing", progress = 10) else item
                }

                val initialSettings = PlatformProfileEngine.createInitialSettings(metadata, platform)
                val plan = SmartOptimizationEngine.generatePlan(metadata, initialSettings)

                val result = VideoTranscoder.transcode(
                    context = getApplication(),
                    sourceUri = sourceUri,
                    plan = plan,
                    onProgress = { stage ->
                        if (stage is ExportStage.Encoding) {
                            _multiPlatformQueue.value = _multiPlatformQueue.value.mapIndexed { idx, item ->
                                if (idx == index) item.copy(progress = stage.progressPercent) else item
                            }
                        }
                    },
                    isCancelled = { isCancelled }
                )

                result.onSuccess { file ->
                    _multiPlatformQueue.value = _multiPlatformQueue.value.mapIndexed { idx, item ->
                        if (idx == index) item.copy(stage = "Complete", progress = 100, outputFile = file) else item
                    }
                    val project = OptimizedProject(
                        originalFilename = metadata.filename,
                        originalUri = metadata.uriString,
                        thumbnailPath = metadata.thumbnailPath,
                        sourceResolution = metadata.resolutionLabel,
                        sourceFps = metadata.fps,
                        sourceBitrateKbps = metadata.videoBitrateBps / 1000,
                        sourceDurationMs = metadata.durationMs,
                        sourceCodec = metadata.videoCodec,
                        sourceAudioCodec = metadata.audioCodec,
                        destinationPlatform = platform.displayName,
                        qualityMode = "Balanced",
                        exportPath = file.absolutePath,
                        exportSizeBytes = file.length(),
                        exportResolution = "${initialSettings.targetWidth} × ${initialSettings.targetHeight}",
                        exportFps = initialSettings.targetFps.toFloat(),
                        status = "Completed"
                    )
                    repository.saveProject(project)
                }.onFailure {
                    _multiPlatformQueue.value = _multiPlatformQueue.value.mapIndexed { idx, item ->
                        if (idx == index) item.copy(stage = "Failed") else item
                    }
                }
            }
            refreshStorageStats()
        }
    }

    fun addBatchVideos(uris: List<Uri>) {
        viewModelScope.launch {
            val newItems = uris.map { uri ->
                BatchVideoItem(
                    id = uri.toString(),
                    uri = uri,
                    status = "Analyzing..."
                )
            }
            _batchQueue.value = _batchQueue.value + newItems
            _currentSubScreen.value = SubScreen.BATCH_OPTIMIZE

            // Analyze each video
            for (item in newItems) {
                val res = VideoAnalyzer.analyze(getApplication(), item.uri)
                _batchQueue.value = _batchQueue.value.map {
                    if (it.uri == item.uri) {
                        it.copy(metadata = res.getOrNull(), status = "Queued")
                    } else it
                }
            }
        }
    }

    fun startBatchProcessing(targetPlatform: PlatformDestination) {
        viewModelScope.launch {
            val queue = _batchQueue.value
            queue.forEachIndexed { index, item ->
                if (item.status == "Completed") return@forEachIndexed

                _batchQueue.value = _batchQueue.value.mapIndexed { i, itm ->
                    if (i == index) itm.copy(status = "Processing", progress = 10) else itm
                }

                val meta = item.metadata ?: run {
                    val r = VideoAnalyzer.analyze(getApplication(), item.uri)
                    r.getOrNull()
                }

                if (meta == null) {
                    _batchQueue.value = _batchQueue.value.mapIndexed { i, itm ->
                        if (i == index) itm.copy(status = "Failed") else itm
                    }
                    return@forEachIndexed
                }

                val settings = PlatformProfileEngine.createInitialSettings(meta, targetPlatform)
                val plan = SmartOptimizationEngine.generatePlan(meta, settings)

                val result = VideoTranscoder.transcode(
                    context = getApplication(),
                    sourceUri = item.uri,
                    plan = plan,
                    onProgress = { stage ->
                        if (stage is ExportStage.Encoding) {
                            _batchQueue.value = _batchQueue.value.mapIndexed { i, itm ->
                                if (i == index) itm.copy(progress = stage.progressPercent) else itm
                            }
                        }
                    },
                    isCancelled = { isCancelled }
                )

                result.onSuccess { file ->
                    _batchQueue.value = _batchQueue.value.mapIndexed { i, itm ->
                        if (i == index) itm.copy(status = "Completed", progress = 100, outputFile = file) else itm
                    }
                    repository.saveProject(
                        OptimizedProject(
                            originalFilename = meta.filename,
                            originalUri = meta.uriString,
                            thumbnailPath = meta.thumbnailPath,
                            sourceResolution = meta.resolutionLabel,
                            sourceFps = meta.fps,
                            sourceBitrateKbps = meta.videoBitrateBps / 1000,
                            sourceDurationMs = meta.durationMs,
                            sourceCodec = meta.videoCodec,
                            sourceAudioCodec = meta.audioCodec,
                            destinationPlatform = targetPlatform.displayName,
                            qualityMode = "Balanced",
                            exportPath = file.absolutePath,
                            exportSizeBytes = file.length(),
                            exportResolution = "${settings.targetWidth} × ${settings.targetHeight}",
                            exportFps = settings.targetFps.toFloat(),
                            status = "Completed"
                        )
                    )
                }.onFailure {
                    _batchQueue.value = _batchQueue.value.mapIndexed { i, itm ->
                        if (i == index) itm.copy(status = "Failed") else itm
                    }
                }
            }
            refreshStorageStats()
        }
    }

    fun removeBatchItem(id: String) {
        _batchQueue.value = _batchQueue.value.filter { it.id != id }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            refreshStorageStats()
        }
    }

    fun clearAllProjects() {
        viewModelScope.launch {
            repository.clearProjects()
            refreshStorageStats()
        }
    }

    fun savePreset(preset: ExportPreset) {
        viewModelScope.launch {
            repository.savePreset(preset)
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    fun clearTempFiles() {
        viewModelScope.launch {
            StorageManager.clearTemporaryFiles(getApplication())
            refreshStorageStats()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            StorageManager.clearCache(getApplication())
            refreshStorageStats()
        }
    }
}
