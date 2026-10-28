package com.example.new_app9.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.new_app9.detector.InferenceInfo
import com.example.new_app9.detector.YoloDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Screen {
    HOME,
    LIVE_CAMERA,
    GALLERY,
    ABOUT,
    CONTACT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Fast App Launch: Initialize Detector lazily in background
    var detector by remember { mutableStateOf<YoloDetector?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            detector = YoloDetector(context, "yolo26n.tflite")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            detector?.close()
        }
    }

    // Handle system back gesture
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.HOME) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.LIVE_CAMERA -> "Live Camera AI"
                                Screen.GALLERY -> "Gallery AI Scan"
                                Screen.ABOUT -> "About Tech3space"
                                Screen.CONTACT -> "Contact Us"
                                else -> "Tech3space AI"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen = Screen.HOME }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A)
                    )
                )
            }
        },
        containerColor = Color(0xFF020617)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> {
                        HomeScreen(
                            onSelectLiveCamera = { currentScreen = Screen.LIVE_CAMERA },
                            onSelectGallery = { currentScreen = Screen.GALLERY },
                            onSelectAbout = { currentScreen = Screen.ABOUT },
                            onSelectContact = { currentScreen = Screen.CONTACT }
                        )
                    }

                    Screen.ABOUT -> {
                        AboutScreen()
                    }

                    Screen.CONTACT -> {
                        ContactScreen()
                    }

                    Screen.LIVE_CAMERA, Screen.GALLERY -> {
                        if (detector == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Loading YOLO AI Model...",
                                        color = Color.LightGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Mode Toggle Sub-Header
                                TabRow(
                                    selectedTabIndex = if (currentScreen == Screen.LIVE_CAMERA) 0 else 1,
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = Color.White,
                                    indicator = { tabPositions ->
                                        val selectedIndex = if (currentScreen == Screen.LIVE_CAMERA) 0 else 1
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = currentScreen == Screen.LIVE_CAMERA,
                                        onClick = { currentScreen = Screen.LIVE_CAMERA },
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Live Camera", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                    Tab(
                                        selected = currentScreen == Screen.GALLERY,
                                        onClick = { currentScreen = Screen.GALLERY },
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Gallery", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    if (currentScreen == Screen.LIVE_CAMERA) {
                                        LiveCameraTab(detector = detector!!)
                                    } else {
                                        GalleryDetectionScreen(detector = detector!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCameraTab(detector: YoloDetector) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Permission Required",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please grant camera permission to use live object detection.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Grant Permission")
            }
        }
    } else {
        var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
        var isFlashEnabled by remember { mutableStateOf(false) }
        var inferenceInfo by remember { mutableStateOf<InferenceInfo?>(null) }

        // FPS Calculation
        var frameCount by remember { mutableIntStateOf(0) }
        var lastFpsTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var currentFps by remember { mutableIntStateOf(0) }

        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                detector = detector,
                lensFacing = lensFacing,
                isFlashEnabled = isFlashEnabled,
                onInferenceResult = { info ->
                    inferenceInfo = info
                    frameCount++
                    val now = System.currentTimeMillis()
                    if (now - lastFpsTime >= 1000) {
                        currentFps = frameCount
                        frameCount = 0
                        lastFpsTime = now
                    }
                }
            )

            // Bounding Box Overlay
            inferenceInfo?.let { info ->
                BoundingBoxOverlay(
                    detections = info.detections,
                    modifier = Modifier.fillMaxSize(),
                    isMirrored = lensFacing == CameraSelector.LENS_FACING_FRONT
                )
            }

            // Real-time Performance HUD
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "YOLO AI Detection",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FPS: $currentFps",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Speed: ${inferenceInfo?.inferenceTimeMs ?: 0} ms",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp
                        )
                    }
                    inferenceInfo?.let { info ->
                        Text(
                            text = "Objects: ${info.detections.size}",
                            color = Color(0xFFFFEA00),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Camera Controls (Switch Front/Back, Flash)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flash Toggle
                AnimatedVisibility(visible = lensFacing == CameraSelector.LENS_FACING_BACK) {
                    FloatingActionButton(
                        onClick = { isFlashEnabled = !isFlashEnabled },
                        containerColor = if (isFlashEnabled) Color(0xFFFFD600) else Color(0x99000000),
                        contentColor = if (isFlashEnabled) Color.Black else Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Flash"
                        )
                    }
                }

                // Camera Switch Toggle
                FloatingActionButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    containerColor = Color(0x99000000),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera"
                    )
                }
            }
        }
    }
}
