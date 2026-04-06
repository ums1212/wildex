package dev.comon.wildex.ui

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.comon.wildex.ui.capture.CaptureIntent
import dev.comon.wildex.ui.capture.CaptureUiEvent
import dev.comon.wildex.ui.capture.CaptureViewModel
import dev.comon.wildex.ui.theme.WildexDimens
import dev.comon.wildex.ui.theme.WildexPalette
import dev.comon.wildex.ui.theme.WildexTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TagCapture = "WildexCapture"

/** 뷰파인더 가로:세로 (시안: 세로로 긴 프리뷰, 정사각형 아님) */
private const val ViewfinderAspectWidth = 3f
private const val ViewfinderAspectHeight = 4f

@Composable
fun CaptureScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.onIntent(CaptureIntent.PermissionResult(granted))
        },
    )

    LaunchedEffect(Unit) {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.onIntent(CaptureIntent.PermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CaptureUiEvent.TakePicture -> {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                image.close()
                                viewModel.onIntent(CaptureIntent.CaptureSucceeded)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e(TagCapture, "capture", exception)
                                viewModel.onIntent(CaptureIntent.CaptureFailed(exception))
                            }
                        },
                    )
                }
                is CaptureUiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
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
        } else {
            CapturePermissionPlaceholder(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        CaptureScreenDotGrid(
            modifier = Modifier.fillMaxSize(),
        )

        Box(
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

        CaptureControlPanel(
            flashOn = state.flashOn,
            onFlashOn = { viewModel.onIntent(CaptureIntent.FlashOn) },
            onFlashOff = { viewModel.onIntent(CaptureIntent.FlashOff) },
            onZoomIn = { viewModel.onIntent(CaptureIntent.ZoomIn) },
            onZoomOut = { viewModel.onIntent(CaptureIntent.ZoomOut) },
            onCapture = { viewModel.onIntent(CaptureIntent.CaptureClicked) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = WildexDimens.gridMajor)
                .padding(bottom = WildexDimens.gridStep * 2)
                .onGloballyPositioned { coords ->
                    controlBarHeightPx = coords.size.height
                },
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
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(WildexPalette.NightVoid, RectangleShape)
            .clickable(onClick = onRequestPermission),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "카메라 권한이 필요합니다.\n탭하여 허용",
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

@Composable
private fun CaptureDpad(
    dpadFace: androidx.compose.ui.graphics.Color,
    outline: androidx.compose.ui.graphics.Color,
    onFace: androidx.compose.ui.graphics.Color,
    flashOn: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFlashOn: () -> Unit,
    onFlashOff: () -> Unit,
) {
    val rowH = 24.dp
    val sep = 2.dp
    val accentOn = WildexPalette.SpecSheetPureRed
    Column(
        modifier = Modifier
            .width(120.dp)
            .border(WildexDimens.borderStrokeChunky, outline, RectangleShape)
            .background(dpadFace, RectangleShape),
    ) {
        DpadCell(
            onClick = onZoomIn,
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
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RectangleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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
    val depth = WildexDimens.shadowOffsetHard
    val ring = WildexTheme.extraColors.cartridgeOutline
    val shadow = WildexTheme.extraColors.cartridgeHardShadow
    val red = WildexPalette.SpecSheetPureRed
    val iconTint = WildexPalette.OnPrimary
    val outer = 44.dp
    val ringInset = WildexDimens.gridStep
    Box(modifier = Modifier.padding(end = depth, bottom = depth)) {
        Box(
            modifier = Modifier
                .size(outer)
                .offset(depth, depth)
                .clip(CircleShape)
                .background(shadow, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(outer)
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCapture,
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
    suspendCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    Log.e(TagCapture, "ProcessCameraProvider", e)
                    continuation.resume(null)
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }
