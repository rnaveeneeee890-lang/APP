package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.CoolGrey
import com.example.ui.viewmodel.MapPoint
import kotlin.math.sqrt

@OptIn(ExperimentalTextApi::class)
@Composable
fun SimulatedMap(
    modifier: Modifier = Modifier,
    pickupPoint: MapPoint?,
    dropoffPoint: MapPoint?,
    driverPoint: MapPoint?,
    driverRotation: Float,
    isBookingActive: Boolean,
    onMapTap: (MapPoint) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    var prevDriverPoint by remember { mutableStateOf<MapPoint?>(null) }
    if (prevDriverPoint == null && driverPoint != null) {
        prevDriverPoint = driverPoint
    } else if (driverPoint == null && prevDriverPoint != null) {
        prevDriverPoint = null
    }

    val targetX = driverPoint?.x ?: prevDriverPoint?.x ?: 0f
    val targetY = driverPoint?.y ?: prevDriverPoint?.y ?: 0f

    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "animated_x"
    )
    val animatedY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "animated_y"
    )
    val animatedRot by animateFloatAsState(
        targetValue = driverRotation,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "animated_rot"
    )
    
    // Pulse animation for pins
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulses")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Convert pixel coordinates (0..width, 0..height) to relative MapPoint (0..100)
                        val relativeX = (offset.x / size.width) * 100f
                        val relativeY = (offset.y / size.height) * 100f
                        onMapTap(MapPoint(relativeX, relativeY, "Custom Map Pin"))
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Map Background grid
            // Grid spacing
            val gridCount = 10
            val cellW = width / gridCount
            val cellH = height / gridCount

            // Draw dark grid cells background
            drawRect(
                color = Color(0xFF10131A),
                size = size
            )

            // Draw grid lines
            for (i in 0..gridCount) {
                // Vertical lines
                drawLine(
                    color = Color(0xFF1D222F),
                    start = Offset(i * cellW, 0f),
                    end = Offset(i * cellW, height),
                    strokeWidth = 1f
                )
                // Horizontal lines
                drawLine(
                    color = Color(0xFF1D222F),
                    start = Offset(0f, i * cellH),
                    end = Offset(width, i * cellH),
                    strokeWidth = 1f
                )
            }

            // 2. Draw Scenic Landscape features (River and Park)
            // Draw Aether River
            val riverPath = Path().apply {
                moveTo(0f, height * 0.45f)
                cubicTo(
                    width * 0.3f, height * 0.35f,
                    width * 0.6f, height * 0.6f,
                    width, height * 0.5f
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF1A365D), // Midnight deep blue
                style = Stroke(width = 32f, pathEffect = PathEffect.cornerPathEffect(50f))
            )

            // Draw Future Park
            drawRoundRect(
                color = Color(0xFF1B4D3E), // Holographic dark green
                topLeft = Offset(width * 0.15f, height * 0.65f),
                size = Size(width * 0.25f, height * 0.2f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Label the Park
            drawText(
                textMeasurer = textMeasurer,
                text = "NEO FOREST",
                topLeft = Offset(width * 0.18f, height * 0.73f),
                style = TextStyle(
                    color = Color(0xFF39FF14).copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // Label the River
            drawText(
                textMeasurer = textMeasurer,
                text = "AETHER RIVER",
                topLeft = Offset(width * 0.45f, height * 0.41f),
                style = TextStyle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // 3. Draw City Streets (Highlighting major roads)
            // Cyber Broadway (Diagonal Main Rd)
            drawLine(
                color = Color(0xFF283044),
                start = Offset(0f, 0f),
                end = Offset(width, height),
                strokeWidth = 12f
            )

            // Tech Avenue (Horizontal)
            drawLine(
                color = Color(0xFF283044),
                start = Offset(0f, height * 0.3f),
                end = Offset(width, height * 0.3f),
                strokeWidth = 10f
            )

            // Quantum Boulevard (Vertical)
            drawLine(
                color = Color(0xFF283044),
                start = Offset(width * 0.75f, 0f),
                end = Offset(width * 0.75f, height),
                strokeWidth = 10f
            )

            // Label major roads
            drawText(
                textMeasurer = textMeasurer,
                text = "CYBER BROADWAY",
                topLeft = Offset(width * 0.3f, height * 0.32f),
                style = TextStyle(
                    color = CoolGrey.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "TECH AVENUE",
                topLeft = Offset(width * 0.05f, height * 0.27f),
                style = TextStyle(
                    color = CoolGrey.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            // 4. Draw Route / Active Booking path if both pins are present
            if (pickupPoint != null && dropoffPoint != null) {
                val startX = (pickupPoint.x / 100f) * width
                val startY = (pickupPoint.y / 100f) * height
                val endX = (dropoffPoint.x / 100f) * width
                val endY = (dropoffPoint.y / 100f) * height

                // Simulated grid-based route with turn: goes horizontal then vertical
                val routePath = Path().apply {
                    moveTo(startX, startY)
                    // Mid-point 1
                    val midX = startX + (endX - startX) * 0.5f
                    lineTo(midX, startY)
                    // Mid-point 2
                    lineTo(midX, endY)
                    lineTo(endX, endY)
                }

                // Draw glowing background route shadow
                drawPath(
                    path = routePath,
                    color = ElectricBlue.copy(alpha = 0.3f),
                    style = Stroke(width = 16f, pathEffect = PathEffect.cornerPathEffect(30f))
                )

                // Draw active dashed navigation path
                drawPath(
                    path = routePath,
                    color = ElectricBlue,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )
            }

            // 5. Draw Pickup Pin (Green/Yellow)
            if (pickupPoint != null) {
                val pX = (pickupPoint.x / 100f) * width
                val pY = (pickupPoint.y / 100f) * height

                // Pulse ring
                drawCircle(
                    color = ElectricYellow.copy(alpha = pulseAlpha),
                    radius = pulseRadius * 2f,
                    center = Offset(pX, pY)
                )

                // Core pin
                drawCircle(
                    color = ElectricYellow,
                    radius = 8f,
                    center = Offset(pX, pY)
                )
                drawCircle(
                    color = Color(0xFF0C0E12),
                    radius = 4f,
                    center = Offset(pX, pY)
                )

                // Tooltip Label
                drawRoundRect(
                    color = Color(0xFF1B1F26).copy(alpha = 0.85f),
                    topLeft = Offset(pX - 45f, pY - 45f),
                    size = Size(90f, 25f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "PICKUP",
                    topLeft = Offset(pX - 25f, pY - 40f),
                    style = TextStyle(
                        color = ElectricYellow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // 6. Draw Dropoff Pin (Electric Blue)
            if (dropoffPoint != null) {
                val dX = (dropoffPoint.x / 100f) * width
                val dY = (dropoffPoint.y / 100f) * height

                // Glow ring
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.3f),
                    radius = 16f,
                    center = Offset(dX, dY)
                )

                // Core pin
                drawCircle(
                    color = ElectricBlue,
                    radius = 8f,
                    center = Offset(dX, dY)
                )
                drawCircle(
                    color = Color(0xFF0C0E12),
                    radius = 4f,
                    center = Offset(dX, dY)
                )

                // Tooltip Label
                drawRoundRect(
                    color = Color(0xFF1B1F26).copy(alpha = 0.85f),
                    topLeft = Offset(dX - 45f, dY - 45f),
                    size = Size(90f, 25f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "DROP OFF",
                    topLeft = Offset(dX - 32f, dY - 40f),
                    style = TextStyle(
                        color = ElectricBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // 7. Draw Driver Icon (Moving Tesla Cab)
            if (driverPoint != null) {
                val drX = (animatedX / 100f) * width
                val drY = (animatedY / 100f) * height

                // Rotate canvas around driver position to draw the car facing its trajectory
                rotate(degrees = animatedRot, pivot = Offset(drX, drY)) {
                    // Glow behind car
                    drawCircle(
                        color = ElectricYellow.copy(alpha = 0.4f),
                        radius = 20f,
                        center = Offset(drX, drY)
                    )

                    // Draw Car body
                    drawRoundRect(
                        color = ElectricYellow,
                        topLeft = Offset(drX - 10f, drY - 18f),
                        size = Size(20f, 36f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Draw windshield (black rectangle)
                    drawRect(
                        color = Color(0xFF0C0E12),
                        topLeft = Offset(drX - 7f, drY - 10f),
                        size = Size(14f, 8f)
                    )

                    // Draw rear windshield
                    drawRect(
                        color = Color(0xFF0C0E12),
                        topLeft = Offset(drX - 7f, drY + 8f),
                        size = Size(14f, 4f)
                    )

                    // Draw Headlights (two bright cyan lights at front)
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 2.5f,
                        center = Offset(drX - 6f, drY - 16f)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 2.5f,
                        center = Offset(drX + 6f, drY - 16f)
                    )
                }
            }
        }

        // Overlay Map Instruction if state is empty
        if (pickupPoint == null && dropoffPoint == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color(0xFF1B1F26).copy(alpha = 0.85f), RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
            ) {
                Text(
                    text = "Tap on map to select pickup and drop off points",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        }
    }
}
