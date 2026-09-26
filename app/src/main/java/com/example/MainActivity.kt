package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.PermissionsDialog
import com.example.ui.navigation.FloatingBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.BatchOptimizeScreen
import com.example.ui.screens.ExportProgressScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiPlatformScreen
import com.example.ui.screens.OptimizeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.OptimizedTheme
import com.example.ui.util.PermissionHelper
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SubScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming shared video (Android Share Sheet integration)
        handleIncomingIntent(intent)

        setContent {
            val themeStyle by viewModel.themeStyle.collectAsState()
            val keepAwake by viewModel.preferences.keepScreenAwake.collectAsState()

            LaunchedEffect(keepAwake) {
                if (keepAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            OptimizedTheme(themeStyle = themeStyle) {
                MainContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if ((Intent.ACTION_SEND == action || Intent.ACTION_VIEW == action) && type?.startsWith("video/") == true) {
            val videoUri = if (Intent.ACTION_SEND == action) {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            } else {
                intent.data
            }
            videoUri?.let { viewModel.importVideo(it) }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionStatus by remember {
        mutableStateOf(PermissionHelper.getPermissionStatus(context))
    }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Update permission status whenever app resumes from background / system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = PermissionHelper.getPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissionStatus = PermissionHelper.getPermissionStatus(context)
        // If full files access is still missing on Android 11+, show dialog to prompt settings
        if (!permissionStatus.hasFullFilesAccess) {
            showPermissionDialog = true
        }
    }

    val fullFilesSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        permissionStatus = PermissionHelper.getPermissionStatus(context)
    }

    val requestRuntimePermissions = {
        val perms = PermissionHelper.getRuntimePermissionsToRequest()
        if (perms.isNotEmpty()) {
            runtimePermissionsLauncher.launch(perms)
        }
    }

    val requestFullFilesAccess = {
        PermissionHelper.openAllFilesAccessSettings(context)
    }

    // Proactively prompt permissions on first launch if not granted
    LaunchedEffect(Unit) {
        if (!permissionStatus.hasAllGranted) {
            if (!permissionStatus.hasNotifications || !permissionStatus.hasGalleryMedia) {
                requestRuntimePermissions()
            } else if (!permissionStatus.hasFullFilesAccess) {
                showPermissionDialog = true
            }
        }
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    val showBottomBar = currentSubScreen == SubScreen.NONE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = Pair(currentScreen, currentSubScreen),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { (_, sub) ->
                when (sub) {
                    SubScreen.OPTIMIZE -> {
                        OptimizeScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.backToHome() }
                        )
                    }
                    SubScreen.EXPORT_PROGRESS -> {
                        ExportProgressScreen(
                            viewModel = viewModel,
                            onOptimizeAnother = { viewModel.backToHome() }
                        )
                    }
                    SubScreen.MULTI_PLATFORM -> {
                        MultiPlatformScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.backToHome() }
                        )
                    }
                    SubScreen.BATCH_OPTIMIZE -> {
                        BatchOptimizeScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.backToHome() }
                        )
                    }
                    else -> {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(
                                viewModel = viewModel,
                                permissionStatus = permissionStatus,
                                onOpenPermissionsDialog = { showPermissionDialog = true },
                                onRequestRuntimePermissions = requestRuntimePermissions,
                                onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                            Screen.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                            Screen.TOOLS -> ToolsScreen(viewModel = viewModel)
                            Screen.HISTORY -> HistoryScreen(viewModel = viewModel)
                            Screen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                permissionStatus = permissionStatus,
                                onRequestRuntimePermissions = requestRuntimePermissions,
                                onRequestFullFilesAccess = requestFullFilesAccess
                            )
                        }
                    }
                }
            }

            // Permissions Dialog
            if (showPermissionDialog) {
                PermissionsDialog(
                    status = permissionStatus,
                    onRequestRuntimePermissions = requestRuntimePermissions,
                    onRequestFullFilesAccess = requestFullFilesAccess,
                    onDismiss = { showPermissionDialog = false }
                )
            }
        }
    }
}
