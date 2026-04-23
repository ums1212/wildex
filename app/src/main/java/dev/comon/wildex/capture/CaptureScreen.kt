package dev.comon.wildex.capture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.comon.wildex.component.rememberDebounceClick
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexPalette
import dev.comon.wildex.ui.theme.WildexTheme
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.core.content.edit

private const val TagCapture = "WildexCapture"

/** 뷰파인더 가로:세로 (시안: 세로로 긴 프리뷰, 정사각형 아님) */
private const val ViewfinderAspectWidth = 3f
private const val ViewfinderAspectHeight = 4f

@Composable
fun CaptureScreen(
    onNavigateToBirdInfo: (speciesId: String, recordId: Long?) -> Unit,
    modifier: Modifier = Modifier,
    onAnalyzingChanged: (Boolean) -> Unit = {},
    viewModel: CaptureViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 현재 디스플레이 회전값 — ImageCapture targetRotation 설정에 사용
    val currentView = LocalView.current
    val displayRotation = currentView.display?.rotation ?: Surface.ROTATION_0

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(displayRotation)
            .build()
    }

    // 화면 회전 변경 시 targetRotation 갱신
    LaunchedEffect(displayRotation) {
        imageCapture.targetRotation = displayRotation
    }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    // 카메라 권한이 영구 거절된 경우 — 설정 화면으로 안내
    var cameraPermissionPermanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] == true
            viewModel.onIntent(CaptureIntent.PermissionResult(cameraGranted))
            viewModel.onIntent(
                CaptureIntent.LocationPermissionResult(
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true,
                ),
            )
            if (!cameraGranted) {
                // shouldShowRationale이 false이면 두 번 이상 거절(or "다시 묻지 않음") → 설정 이동 필요
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as android.app.Activity,
                    Manifest.permission.CAMERA,
                )
                if (!showRationale) cameraPermissionPermanentlyDenied = true
            }
        },
    )

    LaunchedEffect(Unit) {
        val cameraGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        viewModel.onIntent(CaptureIntent.PermissionResult(cameraGranted))

        if (!cameraGranted) {
            val prefs = context.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
            val everRequested = prefs.getBoolean("camera_perm_ever_requested", false)
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as android.app.Activity,
                Manifest.permission.CAMERA,
            )
            // 요청 이력이 있고 rationale도 없으면 → 영구 거절 상태
            if (everRequested && !showRationale) cameraPermissionPermanentlyDenied = true
        }

        val locationGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        viewModel.onIntent(CaptureIntent.LocationPermissionResult(locationGranted))
    }

    // 설정 화면에서 돌아왔을 때 권한 상태 재확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
                viewModel.onIntent(CaptureIntent.PermissionResult(granted))
                if (granted) cameraPermissionPermanentlyDenied = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 분석 중 상태 변화를 부모에게 전달 — 하단 바 숨김/복구에 사용
    LaunchedEffect(state.isAnalyzing) {
        onAnalyzingChanged(state.isAnalyzing)
    }

    // 분석 중 뒤로가기 차단
    androidx.activity.compose.BackHandler(enabled = state.isAnalyzing) {}

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CaptureUiEvent.TakePicture -> {
                    // 카메라가 아직 바인딩되지 않은 상태라면 촬영 불가 — 진행 플래그 리셋
                    if (cameraControl == null) {
                        viewModel.onIntent(
                            CaptureIntent.CaptureFailed(
                                IllegalStateException("카메라가 준비되지 않았습니다"),
                            ),
                        )
                        return@collect
                    }
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                // JPEG 바이트 및 회전값 추출 후 즉시 ImageProxy 닫기
                                val buffer = image.planes[0].buffer
                                val imageBytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
                                val rotationDegrees = image.imageInfo.rotationDegrees
                                image.close()
                                viewModel.onIntent(
                                    CaptureIntent.CaptureSucceeded(imageBytes, rotationDegrees),
                                )
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e(TagCapture, "capture", exception)
                                viewModel.onIntent(CaptureIntent.CaptureFailed(exception))
                            }
                        },
                    )
                }
                is CaptureUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CaptureUiEvent.NavigateToBirdInfo -> {
                    onNavigateToBirdInfo(event.speciesId, event.recordId)
                }
            }
        }
    }

    LaunchedEffect(state.flashOn) {
        imageCapture.flashMode =
            if (state.flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    LaunchedEffect(state.hasCameraPermission, lifecycleOwner) {
        if (!state.hasCameraPermission) {
            runCatching {
                context.awaitProcessCameraProvider()?.unbindAll()
            }
            cameraControl = null
            return@LaunchedEffect
        }
        val provider = context.awaitProcessCameraProvider() ?: return@LaunchedEffect
        provider.unbindAll()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        try {
            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
            )
            cameraControl = camera.cameraControl
            camera.cameraInfo.zoomState.value?.let { zoomState ->
                viewModel.onIntent(
                    CaptureIntent.ZoomBoundsUpdated(
                        min = zoomState.minZoomRatio,
                        max = zoomState.maxZoomRatio,
                    ),
                )
            }
        } catch (e: Exception) {
            Log.e(TagCapture, "bind failed", e)
            viewModel.onIntent(CaptureIntent.CameraBindFailed(e))
        }
    }

    LaunchedEffect(state.zoomRatio, state.zoomMin, state.zoomMax, cameraControl) {
        val control = cameraControl ?: return@LaunchedEffect
        runCatching {
            control.setZoomRatio(state.zoomRatio.coerceIn(state.zoomMin, state.zoomMax))
        }
    }

    LaunchedEffect(state.flashOn, cameraControl) {
        val control = cameraControl ?: return@LaunchedEffect
        runCatching { control.enableTorch(state.flashOn) }
    }

    DisposableEffect(context) {
        onDispose {
            val future = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            future.addListener(
                {
                    runCatching { future.get().unbindAll() }
                },
                executor,
            )
        }
    }

    // 촬영 후 정지 프레임 비트맵 — 분석 중에는 라이브 프리뷰 대신 표시
    var frozenImageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(state.frozenFrameBytes, state.frozenFrameRotation) {
        val bytes = state.frozenFrameBytes
        if (bytes == null) {
            frozenImageBitmap = null
            return@LaunchedEffect
        }
        frozenImageBitmap = withContext(Dispatchers.Default) {
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext null
            val rotated = if (state.frozenFrameRotation != 0) {
                val matrix = Matrix().apply { postRotate(state.frozenFrameRotation.toFloat()) }
                android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                    .also { bmp.recycle() }
            } else bmp
            rotated.asImageBitmap()
        }
    }

    val density = LocalDensity.current
    var controlBarHeightPx by remember { mutableIntStateOf(0) }
    val estimatedControlBar = 120.dp

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val controlBarHeight = with(density) {
            if (controlBarHeightPx > 0) controlBarHeightPx.toDp() else estimatedControlBar
        }
        val overlayZoneHeight = (maxHeight - controlBarHeight - WildexDimens.gridMajor).coerceAtLeast(1.dp)

        if (state.hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
            // 분석 중: 촬영된 정지 프레임으로 라이브 프리뷰를 덮음
            frozenImageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        } else {
            CapturePermissionPlaceholder(
                isPermanentlyDenied = cameraPermissionPermanentlyDenied,
                onAction = if (cameraPermissionPermanentlyDenied) {
                    {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            },
                        )
                    }
                } else {
                    {
                        context.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
                            .edit { putBoolean("camera_perm_ever_requested", true) }
                        permissionLauncher.launch(
                            buildList {
                                add(Manifest.permission.CAMERA)
                                add(Manifest.permission.ACCESS_FINE_LOCATION)
                                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }.toTypedArray(),
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (state.hasCameraPermission) {
            CaptureScreenDotGrid(
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (state.hasCameraPermission) Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(overlayZoneHeight)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val aspectWOverH = ViewfinderAspectWidth / ViewfinderAspectHeight
                val maxW = maxWidth
                val maxH = maxHeight
                val wFit = maxW
                val hFit = wFit / aspectWOverH
                val (iw, ih) = if (hFit <= maxH) {
                    wFit to hFit
                } else {
                    val h = maxH
                    val w = h * aspectWOverH
                    w to h
                }
                Box(modifier = Modifier.size(iw, ih)) {
                    CaptureViewfinderOverlays(modifier = Modifier.fillMaxSize())
                }
            }
        }

        AnimatedVisibility(
            visible = state.hasCameraPermission,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            CaptureControlPanel(
                flashOn = state.flashOn,
                onFlashOn = { viewModel.onIntent(CaptureIntent.FlashOn) },
                onFlashOff = { viewModel.onIntent(CaptureIntent.FlashOff) },
                onZoomIn = { viewModel.onIntent(CaptureIntent.ZoomIn) },
                onZoomOut = { viewModel.onIntent(CaptureIntent.ZoomOut) },
                onCapture = { viewModel.onIntent(CaptureIntent.CaptureClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WildexDimens.gridMajor)
                    .padding(bottom = WildexDimens.gridStep * 2)
                    .onGloballyPositioned { coords ->
                        controlBarHeightPx = coords.size.height
                    },
            )
        }

        // 분석 중 로딩 오버레이 — 모든 터치 이벤트 차단
        if (state.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(WildexDimens.gridMajor),
                ) {
                    CircularProgressIndicator(
                        color = WildexPalette.SpecSheetPureRed,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "ANALYZING...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = Color.White,
                    )
                }
            }
        }

        // Snackbar — 오버레이보다 아래에 배치해 항상 표시
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = controlBarHeight + WildexDimens.gridMajor),
        )
    }
}

@Composable
private fun CaptureScreenDotGrid(
    modifier: Modifier = Modifier,
) {
    val dot = WildexTheme.extraColors.cartridgeOutline.copy(alpha = 0.08f)
    val step = WildexDimens.gridMajor
    Canvas(modifier = modifier) {
        val stepPx = step.toPx()
        val r = WildexDimens.gridStep.toPx() * 0.35f
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(color = dot, radius = r, center = Offset(x, y))
                x += stepPx
            }
            y += stepPx
        }
    }
}

@Composable
private fun CapturePermissionPlaceholder(
    onAction: () -> Unit,
    isPermanentlyDenied: Boolean,
    modifier: Modifier = Modifier,
) {
    val debouncedOnAction = rememberDebounceClick(onAction)
    Box(
        modifier = modifier
            .background(WildexPalette.NightVoid, RectangleShape)
            .clickable(onClick = debouncedOnAction),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isPermanentlyDenied) {
                "카메라 권한이 거절되었습니다.\n탭하여 설정에서 허용"
            } else {
                "카메라 권한이 필요합니다.\n탭하여 허용"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = WildexPalette.Neutral,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(WildexDimens.gridMajor),
        )
    }
}

@Composable
private fun CaptureViewfinderOverlays(modifier: Modifier = Modifier) {
    val accent = WildexPalette.SpecSheetPureRed
    val bracket = 20.dp
    val recPadStart = WildexDimens.gridMajor + bracket + WildexDimens.gridStep * 2
    val recPadTop = WildexDimens.gridMajor + WildexDimens.gridStep * 2
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = recPadStart, top = recPadTop),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WildexDimens.gridStep),
        ) {
            Box(
                modifier = Modifier
                    .size(WildexDimens.gridStep * 2)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = "REC",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = accent,
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val len = bracket.toPx()
            val sw = WildexDimens.gridStep.toPx()
            val p = WildexDimens.gridMajor.toPx()
            // 꼭짓점은 안쪽 모서리(뷰 안을 향함). 상단은 아래로, 하단은 위로 팔이 뻗음.
            // 좌상단
            drawLine(accent, Offset(p, p), Offset(p + len, p), sw)
            drawLine(accent, Offset(p, p), Offset(p, p + len), sw)
            // 우상단
            drawLine(accent, Offset(w - p, p), Offset(w - p - len, p), sw)
            drawLine(accent, Offset(w - p, p), Offset(w - p, p + len), sw)
            // 좌하단 (밑변 오른쪽, 왼변 위로)
            drawLine(accent, Offset(p, h - p), Offset(p + len, h - p), sw)
            drawLine(accent, Offset(p, h - p), Offset(p, h - p - len), sw)
            // 우하단 (밑변 왼쪽, 오른변 위로)
            drawLine(accent, Offset(w - p, h - p), Offset(w - p - len, h - p), sw)
            drawLine(accent, Offset(w - p, h - p), Offset(w - p, h - p - len), sw)
            val focus = minOf(w, h) * 0.24f
            drawRect(
                color = accent,
                topLeft = Offset((w - focus) / 2f, (h - focus) / 2f),
                size = Size(focus, focus),
                style = Stroke(width = sw),
            )
        }
    }
}

@Composable
private fun CaptureControlPanel(
    flashOn: Boolean,
    onFlashOn: () -> Unit,
    onFlashOff: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val depth = WildexDimens.shadowOffsetHard
    val outline = WildexTheme.extraColors.cartridgeOutline
    val shadow = WildexTheme.extraColors.cartridgeHardShadow
    val panelBg = MaterialTheme.colorScheme.surfaceContainerLowest
    val dpadFace = MaterialTheme.colorScheme.surfaceContainerHigh
    val onFace = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.padding(end = depth, bottom = depth)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(shadow, RectangleShape),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
                .background(panelBg, RectangleShape)
                .padding(horizontal = WildexDimens.gridStep * 2, vertical = WildexDimens.gridStep * 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CaptureDpad(
                dpadFace = dpadFace,
                outline = outline,
                onFace = onFace,
                flashOn = flashOn,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onFlashOn = onFlashOn,
                onFlashOff = onFlashOff,
            )
            CaptureShutterCluster(
                onCapture = onCapture,
            )
        }
    }
}

private enum class DpadDirection { Up, Down, Left, Right }

@Composable
private fun CaptureDpad(
    dpadFace: Color,
    outline: Color,
    onFace: Color,
    flashOn: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFlashOn: () -> Unit,
    onFlashOff: () -> Unit,
) {
    // 각 방향 셀의 InteractionSource를 CaptureDpad가 소유 — 전체 위젯 기울기 계산에 사용
    val zoomInSource = remember { MutableInteractionSource() }
    val zoomOutSource = remember { MutableInteractionSource() }
    val flashOnSource = remember { MutableInteractionSource() }
    val flashOffSource = remember { MutableInteractionSource() }

    val zoomInPressed by zoomInSource.collectIsPressedAsState()
    val zoomOutPressed by zoomOutSource.collectIsPressedAsState()
    val flashOnPressed by flashOnSource.collectIsPressedAsState()
    val flashOffPressed by flashOffSource.collectIsPressedAsState()

    val pressedDirection = when {
        zoomInPressed -> DpadDirection.Up
        zoomOutPressed -> DpadDirection.Down
        flashOnPressed -> DpadDirection.Left
        flashOffPressed -> DpadDirection.Right
        else -> null
    }

    val tiltAngle = 18f
    val targetRotX = when (pressedDirection) {
        DpadDirection.Up -> tiltAngle
        DpadDirection.Down -> -tiltAngle
        else -> 0f
    }
    val targetRotY = when (pressedDirection) {
        DpadDirection.Left -> -tiltAngle
        DpadDirection.Right -> tiltAngle
        else -> 0f
    }
    val rotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "dpadRotX",
    )
    val rotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "dpadRotY",
    )

    val rowH = 24.dp
    val sep = 2.dp
    val accentOn = WildexPalette.SpecSheetPureRed

    // graphicsLayer를 Column 전체에 적용 — border/background까지 포함해 위젯 전체가 기울어짐
    Column(
        modifier = Modifier
            .width(120.dp)
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                cameraDistance = 8.dp.toPx()
            }
            .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
            .background(dpadFace, RectangleShape),
    ) {
        DpadCell(
            onClick = onZoomIn,
            interactionSource = zoomInSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(rowH),
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = onFace,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sep)
                .background(outline.copy(alpha = 0.35f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowH),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DpadCell(
                onClick = onFlashOn,
                interactionSource = flashOnSource,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Icon(
                    imageVector = Icons.Filled.FlashOn,
                    contentDescription = "플래시 켜기",
                    tint = if (flashOn) accentOn else onFace,
                    modifier = Modifier.size(18.dp),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            DpadCell(
                onClick = onFlashOff,
                interactionSource = flashOffSource,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Icon(
                    imageVector = Icons.Filled.FlashOff,
                    contentDescription = "플래시 끄기",
                    tint = onFace,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sep)
                .background(outline.copy(alpha = 0.35f)),
        )
        DpadCell(
            onClick = onZoomOut,
            interactionSource = zoomOutSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(rowH),
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = onFace,
            )
        }
    }
}

@Composable
private fun DpadCell(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun CaptureShutterCluster(
    onCapture: () -> Unit,
) {
    val debouncedOnCapture = rememberDebounceClick(onCapture)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val depth = WildexDimens.shadowOffsetHard
    val depthPressed = 2.dp
    val shadowOffset = if (pressed) depthPressed else depth
    val contentInset = if (pressed) depth - depthPressed else 0.dp

    val ring = WildexTheme.extraColors.cartridgeOutline
    val shadow = WildexTheme.extraColors.cartridgeHardShadow
    val red = WildexPalette.SpecSheetPureRed
    val iconTint = WildexPalette.OnPrimary
    val outer = 64.dp
    val ringInset = WildexDimens.gridStep

    Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
        // 그림자 — pressed 시 offset 축소
        Box(
            modifier = Modifier
                .size(outer)
                .offset(shadowOffset, shadowOffset)
                .clip(CircleShape)
                .background(shadow, CircleShape),
        )
        // 링 + 버튼 페이스 — pressed 시 그림자 방향으로 이동
        Box(
            modifier = Modifier
                .size(outer)
                .offset(contentInset, contentInset)
                .border(WildexDimens.borderStrokeChunky, ring, CircleShape)
                .padding(ringInset),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(red, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = debouncedOnCapture,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "촬영",
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private suspend fun Context.awaitProcessCameraProvider(): ProcessCameraProvider? =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                if (continuation.isActive) {
                    try {
                        // addListener 콜백 내부에서 호출하므로 Future는 이미 완료 상태 — blocking 없음
                        continuation.resume(future.get())
                    } catch (e: Exception) {
                        Log.e(TagCapture, "ProcessCameraProvider", e)
                        continuation.resume(null)
                    }
                }
                // continuation이 비활성(취소)이면 결과를 무시 — Future 자체는 취소하지 않음
            },
            ContextCompat.getMainExecutor(this),
        )
        // ProcessCameraProvider Future는 동일 Context에 대한 싱글턴이므로
        // 코루틴 취소 시 future.cancel()을 호출하면 이후 getInstance() 호출도 영구적으로 실패함
        // → invokeOnCancellation에서 Future를 취소하지 않고 결과만 무시
    }
