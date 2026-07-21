@file:OptIn(ExperimentalLayoutApi::class)

package de.friedhofsender.app.ui.main

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.hilt.navigation.compose.hiltViewModel
import de.friedhofsender.app.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class LayoutSize { Compact, Medium, Expanded }

/* ============================================================
    GLITCH TEXT COMPOSABLE
============================================================ */
@Composable
private fun GlitchText(
    text: String,
    color: Color = Color.White,
    fontSize: TextUnit = 12.sp,
    fontFamily: FontFamily = FontFamily.Monospace,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = 0.sp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitchText")

    val glitchOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchOffsetX"
    )

    val glitchOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchOffsetY"
    )

    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing
        )

        Text(
            text = text,
            color = Color(0xFFFF00FF).copy(alpha = glitchAlpha),
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            modifier = Modifier.offset(x = glitchOffsetX * 1.5.dp, y = glitchOffsetY * 0.5.dp)
        )

        Text(
            text = text,
            color = Color(0xFF00FFFF).copy(alpha = glitchAlpha * 0.6f),
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            modifier = Modifier.offset(x = -glitchOffsetX * 1.5.dp, y = -glitchOffsetY * 0.5.dp)
        )
    }
}

@Composable
private fun terminalGlitchStyle(text: String): TextStyle {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchOffset"
    )

    return TextStyle(
        color = Color(0xFF00FFFF),
        fontFamily = FontFamily.Monospace,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        shadow = Shadow(
            color = Color(0xFF00FFFF).copy(alpha = 0.5f),
            offset = Offset(glitchOffset * 1.5f, 0f),
            blurRadius = 6f
        )
    )
}

@Composable
private fun Modifier.panelFrameAnimated(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    this
        .border(
            width = 2.dp,
            brush = Brush.linearGradient(
                listOf(Color(0xFFFF00FF), Color(0xFFCC00FF), Color(0xFFFF00FF))
            ),
            shape = RoundedCornerShape(16.dp)
        )
        .shadow(
            elevation = (14 + (shadowAlpha * 4)).dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = Color(0xFFFF00FF).copy(alpha = shadowAlpha * 0.6f),
            spotColor = Color(0xFFFF33CC).copy(alpha = shadowAlpha * 0.4f)
        )
        .graphicsLayer {
            scaleX = glowScale
            scaleY = glowScale
        }
}

private fun Modifier.terminalWindowStyle(): Modifier = composed {
    this
        .background(Color(0xFF0A0510), RoundedCornerShape(14.dp))
        .border(1.5.dp, Color(0xFFFF00FF).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        .drawBehind {
            val strokeWidth = 1.5f
            val step = 6f
            for (y in 0 until size.height.toInt() step step.toInt()) {
                drawLine(
                    color = Color(0xFFFF00FF).copy(alpha = 0.03f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = strokeWidth
                )
            }
        }
        .padding(14.dp)
}

private fun Modifier.innerGlow(): Modifier =
    this.drawBehind {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF33CC).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                radius = size.maxDimension * 0.85f
            ),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

@Composable
private fun NoiseOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "noise")
    val noiseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noiseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val lineSpacing = 3.5.dp.toPx()
                for (y in 0 until size.height.toInt() step lineSpacing.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = noiseAlpha),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }
            }
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.02f)
                    )
                )
            )
    )
}

@Composable
private fun BackgroundFrame() {
    val infiniteTransition = rememberInfiniteTransition(label = "edgePulse")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = bgScale
                scaleY = bgScale
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.hintergrundfriedhofsender),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    0.6f to Color.Transparent,
                    1.0f to Color(0xFF00FFFF).copy(alpha = glowAlpha * 0.4f),
                    center = center,
                    radius = size.maxDimension * 0.8f
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00FFFF).copy(alpha = glowAlpha * 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFFFF00FF).copy(alpha = glowAlpha * 0.2f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.7f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.4f)
                    )
                )
        )
    }
}

@Composable
private fun StaticOverlay(isActive: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 0.25f else 0f,
        label = "staticAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
    )
}

@Composable
fun rememberMainViewModel(): MainViewModel = hiltViewModel()

@Composable
fun MainScreen(viewModel: MainViewModel = rememberMainViewModel()) {
    val showIntro by viewModel.showIntro.collectAsState()
    val showTextOverlay by viewModel.showTextOverlay.collectAsState()
    val isNoise by viewModel.isNoise.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!showIntro) {
            BackgroundFrame()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val layoutSize = when {
                maxWidth < 600.dp -> LayoutSize.Compact
                maxWidth < 840.dp -> LayoutSize.Medium
                else -> LayoutSize.Expanded
            }

            Crossfade(
                targetState = isLandscape,
                animationSpec = tween(450),
                label = "orientationSwitch"
            ) { landscape: Boolean ->
                if (landscape) {
                    LandscapeContentPanel(viewModel, layoutSize)
                } else {
                    ContentPanel(viewModel, layoutSize)
                }
            }
        }

        StaticOverlay(isActive = isNoise)

        AnimatedVisibility(
            visible = showIntro,
            enter = fadeIn(tween(600)),
            exit = fadeOut(tween(500))
        ) {
            IntroScreen { viewModel.dismissIntro() }
        }

        if (showTextOverlay) {
            val text by viewModel.text.collectAsState()
            TextOverlay(text) { viewModel.closeTextOverlay() }
        }
    }
}

@Composable
private fun IntroScreen(onDismiss: () -> Unit) {
    val bootScale = remember { Animatable(0.02f) }
    LaunchedEffect(Unit) {
        bootScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    var glitchActive by remember { mutableStateOf(true) }

    val glitch = rememberInfiniteTransition(label = "glitchTransition")
    val glitchOffsetX by glitch.animateFloat(
        -2f, 2f,
        animationSpec = infiniteRepeatable(
            tween(120, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "glitchOffset"
    ).takeIf { glitchActive } ?: remember { mutableStateOf(0f) }

    val glitchAlpha by glitch.animateFloat(
        0.15f, 0.35f,
        animationSpec = infiniteRepeatable(
            tween(180, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    ).takeIf { glitchActive } ?: remember { mutableStateOf(0f) }

    val bootLines = listOf(
        "",
        "[BOOT] Initialisiere",
        "[BOOT] Scanne Grabreihen …",
        "[BOOT] Lade Friedhofsmodul: OK",
        "[BOOT] Starte Friedhofsender‑Daemon …",
        "[BOOT] Synchronisiere Verstorbenenarchiv …",
        "[ OK ] Durchsagespeicher montiert.",
        "[BOOT] Verbinde mit jenseitigen Frequenzen …",
        "[WARN] Präsenz im Kanal erkannt.",
        "[ OK ] Präsenz ignoriert.",
        "[ OK ] Ätherverbindung stabil.",
        "[ OK ] Empfangsmodul bereit.",
        "[ OK ] Durchsagespeicher montiert.",
        "[DONE] Friedhofsender bereit für Übertragungen."
    )

    var visibleLines by remember { mutableStateOf(1) }
    var bootFinished by remember { mutableStateOf(false) }
    var crtPhase by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (visibleLines < bootLines.size) {
            delay(220)
            visibleLines++
        }
        bootFinished = true
    }

    val crtScale = remember { Animatable(1f) }
    val crtAlpha = remember { Animatable(1f) }

    LaunchedEffect(crtPhase) {
        if (crtPhase) {
            glitchActive = false
            crtScale.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
            crtAlpha.animateTo(0f, tween(180, easing = LinearEasing))
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (bootFinished && !crtPhase) crtPhase = true
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.schaltkasten_handy),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleY = bootScale.value },
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        List(40) { if (it % 2 == 0) Color.Transparent else Color.Black.copy(alpha = 0.12f) }
                    )
                )
                .alpha(0.6f)
        )

        if (glitchActive) {
            Image(
                painter = painterResource(id = R.drawable.schaltkasten_handy),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = glitchOffsetX.dp)
                    .graphicsLayer { alpha = glitchAlpha },
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color(0xFFFF00FF).copy(alpha = 0.4f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                bootLines.take(visibleLines).forEach {
                    Text(
                        it,
                        color = Color(0xFFFF00FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(Modifier.height(3.dp))
                }

                if (!bootFinished) {
                    Text(
                        "_",
                        color = Color(0xFFFF00FF),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (bootFinished) {
            GlitchText(
                text = "Tippen zum Start …",
                color = Color(0xFFFF00FF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = crtScale.value
                    alpha = crtAlpha.value
                }
        )
    }
}

@Composable
private fun ContentPanel(
    viewModel: MainViewModel,
    layoutSize: LayoutSize
) {
    val text by viewModel.text.collectAsState()
    val status by viewModel.status.collectAsState()
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val musicVolume by viewModel.musicVolume.collectAsState()
    val ttsVolume by viewModel.ttsVolume.collectAsState()
    val topicInput by viewModel.topicInput.collectAsState() // 💡 Prompt-Thema State

    val titleSize = when (layoutSize) {
        LayoutSize.Compact -> 24.sp
        LayoutSize.Medium -> 28.sp
        LayoutSize.Expanded -> 36.sp
    }
    val knobSize = when (layoutSize) {
        LayoutSize.Compact -> 62.dp
        LayoutSize.Medium -> 76.dp
        LayoutSize.Expanded -> 96.dp
    }
    val panelPadding = when (layoutSize) {
        LayoutSize.Compact -> 12.dp
        LayoutSize.Medium -> 16.dp
        LayoutSize.Expanded -> 24.dp
    }

    val textScrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "titlePulse")
    val titleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(panelPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color(0xFFFF00FF).copy(alpha = 0.3f)
                )
        ) {
            GlitchText(
                text = "Friedhofsender",
                color = Color.White,
                fontSize = titleSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .panelFrameAnimated()
                .terminalWindowStyle()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlitchText(
                        text = "TRANS-ÄTHER-DECODER // $status",
                        color = Color(0xFF00FFFF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedStatusIndicator()

                    Spacer(Modifier.width(8.dp))
                }

                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF00FFFF).copy(alpha = 0.25f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(textScrollState)
                    ) {
                        val displayInfoText = if (text.isBlank()) "SUCHE FREQUENZ..." else text
                        Crossfade(
                            targetState = displayInfoText,
                            animationSpec = tween(400),
                            label = "displayText"
                        ) { targetText ->
                            Text(text = targetText, style = terminalGlitchStyle(targetText))
                        }
                    }
                    NoiseOverlay()
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .panelFrameAnimated()
                .innerGlow()
                .background(Color(0xFF0D0610), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedFancyButton(
                text = "NEUE DURCHSAGE EMPFANGEN",
                onClick = { viewModel.generate() }
            )

            // 💡 Prompt-Thema Eingabefeld im Cyberpunk-Stil
            OutlinedTextField(
                value = topicInput,
                onValueChange = { viewModel.updateTopic(it) },
                label = { Text("Wunschthema", color = Color(0xFFFF00FF), fontSize = 11.sp) },
                placeholder = { Text("z.B. Nebel...", color = Color.Gray, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (topicInput.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.clearTopic() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Prompt löschen",
                                tint = Color(0xFFFF00FF)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF00FF),
                    unfocusedBorderColor = Color(0xFF00FFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            AnimatedNowPlayingWindow(nowPlaying)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        AnimatedFancyButton(
                            if (isPlaying) "STOP" else "START",
                            onClick = { viewModel.toggleMusic() },
                            isActive = isPlaying
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        AnimatedFancyButton(
                            if (isMuted) "UNMUTE" else "MUTE",
                            onClick = { viewModel.toggleMute() },
                            isActive = isMuted
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        AnimatedFancyButton("ZOOM", onClick = { viewModel.openTextOverlay() })
                    }
                    Box(Modifier.weight(1f)) {
                        AnimatedFancyButton(
                            text = if (isSpeaking) "VORLESEN STOPPEN" else "VORLESEN",
                            onClick = { viewModel.handleSpeakAction() },
                            isActive = isSpeaking
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                AnimatedBottomControls(
                    musicVolume = musicVolume,
                    ttsVolume = ttsVolume,
                    onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                    onTtsVolumeChange = { viewModel.setTtsVolume(it) },
                    knobSize = knobSize
                )
            }
        }
    }
}

@Composable
private fun LandscapeContentPanel(
    viewModel: MainViewModel,
    layoutSize: LayoutSize
) {
    val text by viewModel.text.collectAsState()
    val status by viewModel.status.collectAsState()
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val musicVolume by viewModel.musicVolume.collectAsState()
    val ttsVolume by viewModel.ttsVolume.collectAsState()
    val topicInput by viewModel.topicInput.collectAsState() // 💡 Prompt-Thema State

    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .panelFrameAnimated()
                .terminalWindowStyle()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlitchText(
                    text = "> TRANS-ÄTHER-DECODER // $status",
                    color = Color(0xFF00FFFF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                AnimatedStatusIndicator()

                Spacer(Modifier.width(6.dp))
            }

            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF00FFFF).copy(alpha = 0.25f))
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(leftScrollState)
                ) {
                    Crossfade(
                        targetState = text,
                        animationSpec = tween(400),
                        label = "landscapeText"
                    ) { targetText ->
                        Text(
                            text = targetText,
                            style = terminalGlitchStyle(targetText).copy(fontSize = 13.sp)
                        )
                    }
                }
                NoiseOverlay()
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .panelFrameAnimated()
                .innerGlow()
                .background(Color(0xFF0D0610), RoundedCornerShape(16.dp))
                .padding(12.dp)
                .verticalScroll(rightScrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedFancyButton("NEUE DURCHSAGE", onClick = { viewModel.generate() })

            // 💡 Prompt-Thema Eingabefeld auch im Querformat
            OutlinedTextField(
                value = topicInput,
                onValueChange = { viewModel.updateTopic(it) },
                label = { Text("Prompt-Thema", color = Color(0xFFFF00FF), fontSize = 10.sp) },
                placeholder = { Text("z.B. Nebel...", color = Color.Gray, fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (topicInput.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.clearTopic() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Prompt löschen",
                                tint = Color(0xFFFF00FF)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF00FF),
                    unfocusedBorderColor = Color(0xFF00FFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            AnimatedNowPlayingWindow(nowPlaying)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    AnimatedFancyButton(
                        if (isPlaying) "STOP" else "START",
                        onClick = { viewModel.toggleMusic() },
                        isActive = isPlaying
                    )
                }
                Box(Modifier.weight(1f)) {
                    AnimatedFancyButton(
                        if (isMuted) "UNMUTE" else "MUTE",
                        onClick = { viewModel.toggleMute() },
                        isActive = isMuted
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    AnimatedFancyButton("ZOOM", onClick = { viewModel.openTextOverlay() })
                }
                Box(Modifier.weight(1f)) {
                    AnimatedFancyButton(
                        text = if (isSpeaking) "VORLESEN STOPPEN" else "VORLESEN",
                        onClick = { viewModel.handleSpeakAction() },
                        isActive = isSpeaking
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                AnimatedBottomControls(
                    musicVolume = musicVolume,
                    ttsVolume = ttsVolume,
                    onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                    onTtsVolumeChange = { viewModel.setTtsVolume(it) },
                    knobSize = 64.dp
                )
            }
        }
    }
}

@Composable
private fun AnimatedStatusIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .size(6.dp)
            .background(Color(0xFF00FF88), CircleShape)
            .shadow(
                elevation = 4.dp * pulseScale,
                shape = CircleShape,
                ambientColor = Color(0xFF00FF88).copy(alpha = 0.4f)
            )
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
    )
}

@Composable
fun AnimatedFancyButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "buttonGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderRotation"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "pressScale"
    )

    val activeColor = if (isActive) Color(0xFF00FFFF) else Color(0xFFFF00FF)

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFF4A0052).copy(alpha = 0.8f) else Color(0xFF2A1A35),
            disabledContainerColor = Color(0xFF1A0A20)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .border(
                1.8.dp,
                if (enabled) {
                    Brush.linearGradient(
                        listOf(
                            activeColor,
                            Color(0xFFFF33CC),
                            Color(0xFFCC00FF),
                            activeColor
                        )
                    )
                } else {
                    SolidColor(Color(0xFF3A3A4A))
                },
                RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = if (enabled) 10.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = activeColor.copy(alpha = if (enabled) glowAlpha * 0.4f else 0.2f),
                spotColor = Color(0xFFFF33CC).copy(alpha = if (enabled) glowAlpha * 0.2f else 0.1f)
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .animateContentSize()
            .drawBehind {
                val scanLineHeight = 2f
                for (y in 0 until size.height.toInt() step 4) {
                    drawLine(
                        color = if (isActive) Color(0xFF00FFFF).copy(alpha = 0.08f) else Color(0xFFFF00FF).copy(alpha = 0.06f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = scanLineHeight
                    )
                }

                if (isActive && enabled) {
                    for (i in 0..2) {
                        val progress = ((borderRotation + (i * 120)) % 360) / 360f
                        val glowRadius = size.minDimension * (0.5f + progress * 0.8f)

                        drawCircle(
                            color = activeColor.copy(alpha = (1f - progress) * 0.15f),
                            radius = glowRadius,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentPadding = PaddingValues(vertical = 10.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        GlitchText(
            text = text,
            color = if (enabled) Color.White else Color(0xFF7A7A8A),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(200)
            isPressed = false
        }
    }
}

@Composable
private fun AnimatedNowPlayingWindow(nowPlaying: String) {
    val displayText = if (nowPlaying.isBlank()) "–" else " >> $nowPlaying "

    var textWidth by remember(nowPlaying) { mutableStateOf(0f) }
    var boxWidth by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = boxWidth,
        targetValue = -textWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (textWidth > 0) ((textWidth + boxWidth) * 7).toInt().coerceIn(3500, 25000) else 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "marqueeOffset"
    )

    val infiniteGlow = rememberInfiniteTransition(label = "marqueeGlow")
    val glowAlpha by infiniteGlow.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFF080408), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                Color(0xFFFF00FF).copy(alpha = glowAlpha * 0.25f),
                RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = glowAlpha * 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF00FFFF).copy(alpha = glowAlpha * 0.15f)
            )
            .padding(horizontal = 10.dp)
            .onGloballyPositioned { boxWidth = it.size.width.toFloat() }
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        GlitchText(
            text = displayText,
            color = Color(0xFF00FFFF),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.W500,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .onGloballyPositioned { coords ->
                    if (coords.size.width > 0) {
                        textWidth = coords.size.width.toFloat()
                    }
                }
                .graphicsLayer {
                    this.translationX = offsetX
                }
                .align(Alignment.CenterStart)
        )
    }
}

@Composable
private fun AnimatedBottomControls(
    musicVolume: Float,
    ttsVolume: Float,
    onMusicVolumeChange: (Float) -> Unit,
    onTtsVolumeChange: (Float) -> Unit,
    knobSize: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlitchText(
                text = "Musik",
                color = Color(0xFFB0B0B0),
                fontSize = 11.sp,
                fontWeight = FontWeight.W600
            )

            AnimatedKnob(
                size = knobSize,
                initialValue = musicVolume,
                onValueChange = onMusicVolumeChange
            )

            AnimatedVolumeText(value = musicVolume)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlitchText(
                text = "Durchsage",
                color = Color(0xFFB0B0B0),
                fontSize = 11.sp,
                fontWeight = FontWeight.W600
            )

            AnimatedKnob(
                size = knobSize,
                initialValue = ttsVolume,
                onValueChange = onTtsVolumeChange
            )

            AnimatedVolumeText(value = ttsVolume)
        }
    }
}

@Composable
private fun AnimatedVolumeText(value: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "volumeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    GlitchText(
        text = "${(value * 100).roundToInt()}%",
        color = Color(0xFF00FFFF).copy(alpha = glowAlpha),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AnimatedKnob(size: Dp, initialValue: Float, onValueChange: (Float) -> Unit) {
    var value by remember { mutableStateOf(initialValue.coerceIn(0f, 1f)) }
    var angle by remember { mutableStateOf(-135f + value * 270f) }

    val infiniteTransition = rememberInfiniteTransition(label = "knobPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "knobGlow"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF3A0052),
                        Color(0xFF1A0035)
                    )
                )
            )
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFF00FF),
                        Color(0xFFFF33CC),
                        Color(0xFFCC00FF)
                    )
                ),
                CircleShape
            )
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Color(0xFFFF00FF).copy(alpha = glowAlpha * 0.5f),
                spotColor = Color(0xFFFF33CC).copy(alpha = glowAlpha * 0.3f)
            )
            .pointerInput(Unit) {
                detectDragGestures { _, drag ->
                    val delta = -drag.y / 400f
                    val newValue = (value + delta).coerceIn(0f, 1f)

                    if (newValue != value) {
                        value = newValue
                        angle = -135f + value * 270f
                        onValueChange(value)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(size / 3)
                .width(3.dp)
                .background(Color(0xFF00FFFF), RoundedCornerShape(1.5.dp))
                .rotate(angle)
        )
    }
}

@Composable
private fun TextOverlay(text: String, onClose: () -> Unit) {
    val scaleAnimation = remember { Animatable(0.8f) }
    val alphaAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        alphaAnimation.animateTo(1f, tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.75f))
            .pointerInput(Unit) {
                detectTapGestures {
                    onClose()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .panelFrameAnimated()
                .innerGlow()
                .background(Color(0xFF0A0510), RoundedCornerShape(16.dp))
                .padding(22.dp)
                .graphicsLayer {
                    scaleX = scaleAnimation.value
                    scaleY = scaleAnimation.value
                    alpha = alphaAnimation.value
                }
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlitchText(
                text = "ÜBERTRAGUNG",
                color = Color(0xFF00FFFF),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFFF00FF).copy(alpha = 0.2f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            AnimatedFancyButton(
                text = "SCHLIESSEN",
                onClick = onClose
            )
        }
    }
}