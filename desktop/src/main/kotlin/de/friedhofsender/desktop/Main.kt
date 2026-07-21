package de.friedhofsender.desktop

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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

fun main() = application {
    val viewModel = remember { DesktopViewModel() }
    
    var isWindowVisible by remember { mutableStateOf(true) }
    var minimizeToTrayEnabled by remember { mutableStateOf(false) }
    val windowState = rememberWindowState(width = 850.dp, height = 650.dp)

    val iconPainter = remember { loadAppIconPainter() }

    Tray(
        icon = iconPainter,
        tooltip = "Friedhofsender - Cyberpunk Terminal",
        menu = {
            Item("Öffnen", onClick = { isWindowVisible = true })
            Item("Beenden", onClick = {
                viewModel.onCloseApp()
                exitApplication()
            })
        }
    )

    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                if (minimizeToTrayEnabled) {
                    isWindowVisible = false
                } else {
                    viewModel.onCloseApp()
                    exitApplication()
                }
            },
            title = "Friedhofsender - Cyberpunk Desktop Terminal",
            state = windowState,
            icon = iconPainter
        ) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DesktopMainScreen(
                    viewModel = viewModel,
                    minimizeToTrayEnabled = minimizeToTrayEnabled,
                    onMinimizeToTrayChange = { minimizeToTrayEnabled = it },
                    onManualMinimizeToTray = { isWindowVisible = false }
                )
            }
        }
    }
}

private fun loadAppIconPainter(): Painter {
    return try {
        val resourceStream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.ico")
            ?: Thread.currentThread().contextClassLoader.getResourceAsStream("/icon.ico")

        if (resourceStream != null) {
            val bufferedImage = ImageIO.read(resourceStream)
            androidx.compose.ui.graphics.painter.BitmapPainter(bufferedImage.toComposeImageBitmap())
        } else {
            val file = File("desktop/src/jvmMain/resources/icon.ico")
            if (file.exists()) {
                val bufferedImage = ImageIO.read(file)
                androidx.compose.ui.graphics.painter.BitmapPainter(bufferedImage.toComposeImageBitmap())
            } else {
                throw Exception("Icon Datei nicht gefunden")
            }
        }
    } catch (_: Exception) {
        object : Painter() {
            override val intrinsicSize = androidx.compose.ui.geometry.Size(64f, 64f)
            override fun DrawScope.onDraw() {
                drawRect(Color(0xFFFF00FF))
            }
        }
    }
}

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
            modifier = Modifier.offset(x = (glitchOffsetX * 1.5f).dp, y = (glitchOffsetY * 0.5f).dp)
        )
        Text(
            text = text,
            color = Color(0xFF00FFFF).copy(alpha = glitchAlpha * 0.6f),
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            modifier = Modifier.offset(x = (-glitchOffsetX * 1.5f).dp, y = (-glitchOffsetY * 0.5f).dp)
        )
    }
}

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
            elevation = (14f + (shadowAlpha * 4f)).dp,
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
    )
}

@Composable
fun AnimatedFancyButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
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
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        GlitchText(
            text = text,
            color = if (enabled) Color.White else Color(0xFF7A7A8A),
            fontSize = 11.sp,
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
            .height(38.dp)
            .background(Color(0xFF080408), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                Color(0xFFFF00FF).copy(alpha = glowAlpha * 0.25f),
                RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 3.dp * glowAlpha,
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
            fontSize = 12.sp,
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
private fun AnimatedKnob(size: Dp, initialValue: Float, onValueChange: (Float) -> Unit) {
    var value by remember(initialValue) { mutableStateOf(initialValue.coerceIn(0f, 1f)) }
    var angle by remember(initialValue) { mutableStateOf(-135f + value * 270f) }

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
                    val delta = -drag.y / 200f
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
fun DesktopMainScreen(
    viewModel: DesktopViewModel,
    minimizeToTrayEnabled: Boolean,
    onMinimizeToTrayChange: (Boolean) -> Unit,
    onManualMinimizeToTray: () -> Unit
) {
    val text by viewModel.text.collectAsState()
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val musicVol by viewModel.musicVolume.collectAsState()
    val ttsVol by viewModel.ttsVolume.collectAsState()
    val topic by viewModel.topicInput.collectAsState()

    val terminalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0012))
            .padding(12.dp)
    ) {
        val hideTerminal = maxWidth < 500.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(if (hideTerminal) 140.dp else 130.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A002E)),
                border = BorderStroke(1.dp, Color(0xFFFF00FF))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F001A), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFFF00FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlitchText(
                                text = "FRIEDHOFSENDER",
                                color = Color(0xFFFF00FF),
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Checkbox(
                                    checked = minimizeToTrayEnabled,
                                    onCheckedChange = onMinimizeToTrayChange,
                                    modifier = Modifier.size(18.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFF00FF),
                                        uncheckedColor = Color(0xFF00FFFF),
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Text(
                                    text = "Tray",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )

                                TextButton(
                                    onClick = onManualMinimizeToTray,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    Text("AUSBLENDEN", color = Color(0xFFFF00FF), fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    AnimatedNowPlayingWindow(nowPlaying)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!hideTerminal) {
                    Card(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight()
                            .panelFrameAnimated()
                            .terminalWindowStyle(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0510)),
                        border = BorderStroke(1.dp, Color(0xFFFF00FF).copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(terminalScrollState)
                        ) {
                            val displayText = if (text.isBlank()) "SUCHE FREQUENZ..." else text
                            Text(
                                text = displayText,
                                color = Color(0xFF00FFFF),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                            NoiseOverlay()
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(if (hideTerminal) 1f else 1.3f)
                        .fillMaxHeight()
                        .panelFrameAnimated()
                        .innerGlow(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0610)),
                    border = BorderStroke(1.dp, Color(0xFFFF00FF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlitchText(
                            text = "TRANS-ÄTHER-DECODER",
                            color = Color(0xFF00FFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        AnimatedFancyButton(
                            text = "NEUE DURCHSAGE EMPFANGEN",
                            onClick = { viewModel.generateBroadcast() }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlitchText("Musik", Color(0xFFB0B0B0), 10.sp, fontWeight = FontWeight.W600)
                                Spacer(modifier = Modifier.height(2.dp))
                                AnimatedKnob(
                                    size = 54.dp,
                                    initialValue = musicVol,
                                    onValueChange = { viewModel.setMusicVolume(it) }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                GlitchText("${(musicVol * 100).roundToInt()}%", Color(0xFF00FFFF), 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlitchText("Durchsage", Color(0xFFB0B0B0), 10.sp, fontWeight = FontWeight.W600)
                                Spacer(modifier = Modifier.height(2.dp))
                                AnimatedKnob(
                                    size = 54.dp,
                                    initialValue = ttsVol,
                                    onValueChange = { viewModel.setTtsVolume(it) }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                GlitchText("${(ttsVol * 100).roundToInt()}%", Color(0xFF00FFFF), 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = topic,
                            onValueChange = { viewModel.topicInput.value = it },
                            label = { Text("ThemenWunsch", color = Color(0xFFFF00FF), fontSize = 11.sp) },
                            placeholder = { Text("z.B. Nebel...", color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (topic.isNotBlank()) {
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

                        if (hideTerminal) {
                            AnimatedFancyButton(
                                text = if (isSpeaking) "VORLESEN STOPPEN" else "VORLESEN",
                                onClick = { viewModel.toggleSpeak() },
                                isActive = isSpeaking
                            )

                            AnimatedFancyButton(
                                text = if (isPlaying) "STOPP" else "START",
                                onClick = { viewModel.toggleMusic() },
                                isActive = isPlaying
                            )

                            AnimatedFancyButton(
                                text = if (isMuted) "UNMUTE" else "MUTE",
                                onClick = { viewModel.toggleMute() },
                                isActive = isMuted
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(Modifier.weight(1f)) {
                                    AnimatedFancyButton(
                                        text = if (isSpeaking) "STOPP" else "VORLESEN",
                                        onClick = { viewModel.toggleSpeak() },
                                        isActive = isSpeaking
                                    )
                                }
                                Box(Modifier.weight(1f)) {
                                    AnimatedFancyButton(
                                        text = if (isMuted) "UNMUTE" else "MUTE",
                                        onClick = { viewModel.toggleMute() },
                                        isActive = isMuted
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(Modifier.weight(1f)) {
                                    AnimatedFancyButton(
                                        text = if (isPlaying) "STOPP" else "START",
                                        onClick = { viewModel.toggleMusic() },
                                        isActive = isPlaying
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}