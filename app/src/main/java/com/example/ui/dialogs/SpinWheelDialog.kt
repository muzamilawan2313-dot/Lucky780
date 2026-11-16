package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountEntity
import com.example.model.SPIN_WHEEL_SEGMENTS
import com.example.model.SpinReward
import com.example.model.WheelSegment
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeGoldDeep
import com.example.ui.theme.EmeraldJackpot
import com.example.ui.theme.FestiveCrimson
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.RoyalPlumDark
import com.example.ui.theme.RoyalPlumSurface
import com.example.ui.theme.TextOnDarkPrimary
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.WinterFrostCyan
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SpinWheelDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSpinCompleted: (reward: SpinReward, usedCoinFallback: Boolean) -> Unit,
    onClaimMilestone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var lastWonSegment by remember { mutableStateOf<WheelSegment?>(null) }

    val pulseTransition = rememberInfiniteTransition(label = "wheel_lights")
    val bulbPhase by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulb_phase"
    )

    val canUseTicket = account.bonusTickets > 0
    val canSpinWithCoins = account.coins >= 20L
    val canSpin = !isSpinning && (canUseTicket || canSpinWithCoins)

    Dialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(ArcadeGold, FestiveCrimson, WinterFrostCyan))
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF3B125E), RoyalPlumSurface, RoyalPlumDark)
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                val isWide = maxWidth >= 520.dp
                val wheelSize = if (isWide) 236.dp else 286.dp

                val promoBanner: @Composable () -> Unit = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(FestiveCrimson, Color(0xFFFF6D3B), ArcadeGoldDeep)
                                )
                            )
                            .border(1.5.dp, ArcadeGold, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GET 1,333 FOR FREE",
                                style = if (isWide) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Free Spins and Win Real Money",
                                style = MaterialTheme.typography.labelLarge,
                                color = ArcadeGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (account.freeCashProgress / 1333f).coerceIn(0f, 1f) },
                                color = ArcadeGold,
                                trackColor = Color.Black.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${account.freeCashProgress} / 1,333 Progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                                if (account.freeCashProgress >= 1333L) {
                                    Surface(
                                        onClick = onClaimMilestone,
                                        color = EmeraldJackpot,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "CLAIM 1,333!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Multiplier: ${account.activeMultiplier}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinterFrostCyan
                                    )
                                }
                            }
                        }
                    }
                }

                val wheelWidget: @Composable () -> Unit = {
                    Box(
                        modifier = Modifier.size(wheelSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val outerRadius = size.minDimension / 2f - 6f
                            val wheelRadius = outerRadius - 18f

                            // Outer golden marquee ring
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(ArcadeGold, ArcadeGoldDeep, Color(0xFF78350F)),
                                    center = center,
                                    radius = outerRadius
                                ),
                                radius = outerRadius,
                                center = center
                            )

                            // Twinkling perimeter bulbs
                            val bulbCount = 16
                            for (i in 0 until bulbCount) {
                                val angleRad = Math.toRadians(i * (360.0 / bulbCount))
                                val bx = center.x + (outerRadius - 9f) * cos(angleRad).toFloat()
                                val by = center.y + (outerRadius - 9f) * sin(angleRad).toFloat()
                                val isLit = (i % 2 == 0) == (bulbPhase > 0.5f)
                                drawCircle(
                                    color = if (isLit) Color.White else Color(0xFFFFE082).copy(alpha = 0.4f),
                                    radius = if (isLit) 5.5f else 4f,
                                    center = Offset(bx, by)
                                )
                            }

                            // Rotating segmented wheel
                            val segCount = SPIN_WHEEL_SEGMENTS.size
                            val sweep = 360f / segCount
                            rotate(degrees = rotation.value, pivot = center) {
                                SPIN_WHEEL_SEGMENTS.forEachIndexed { index, segment ->
                                    val startAngle = -90f - (sweep / 2f) + (index * sweep)
                                    drawArc(
                                        color = Color(segment.colorHex),
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                                        size = Size(wheelRadius * 2f, wheelRadius * 2f)
                                    )
                                    drawArc(
                                        color = ArcadeGold.copy(alpha = 0.85f),
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                                        size = Size(wheelRadius * 2f, wheelRadius * 2f),
                                        style = Stroke(width = 3f)
                                    )

                                    val midAngle = Math.toRadians((startAngle + sweep / 2f).toDouble())
                                    val labelRadius = wheelRadius * 0.66f
                                    val lx = center.x + labelRadius * cos(midAngle).toFloat()
                                    val ly = center.y + labelRadius * sin(midAngle).toFloat()

                                    drawContext.canvas.nativeCanvas.apply {
                                        save()
                                        translate(lx, ly)
                                        rotate(startAngle + sweep / 2f + 90f)
                                        val titlePaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.WHITE
                                            textSize = if (isWide) 26f else 32f
                                            isFakeBoldText = true
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val subPaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.argb(235, 255, 243, 196)
                                            textSize = if (isWide) 17f else 20f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        drawText(segment.label, 0f, -2f, titlePaint)
                                        drawText(segment.subLabel, 0f, 18f, subPaint)
                                        restore()
                                    }
                                }
                            }
                        }

                        // Top golden pointer triangle
                        Canvas(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(28.dp)
                        ) {
                            val pointer = Path().apply {
                                moveTo(size.width * 0.5f, size.height * 0.95f)
                                lineTo(size.width * 0.12f, 0f)
                                lineTo(size.width * 0.88f, 0f)
                                close()
                            }
                            drawPath(pointer, color = ArcadeGold)
                            drawPath(pointer, color = Color.White, style = Stroke(width = 2.5f))
                        }

                        // Central interactive 'Spin x1' button
                        Box(
                            modifier = Modifier
                                .size(if (isWide) 74.dp else 86.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        if (canSpin) listOf(Color(0xFFFFF59D), ArcadeGold, FestiveCrimson)
                                        else listOf(Color.Gray, Color.DarkGray)
                                    )
                                )
                                .border(3.dp, Color.White, CircleShape)
                                .clickable(enabled = canSpin) {
                                    scope.launch {
                                        isSpinning = true
                                        lastWonSegment = null
                                        val usedCoins = !canUseTicket
                                        val chosenIndex = Random.nextInt(SPIN_WHEEL_SEGMENTS.size)
                                        val sweep = 360f / SPIN_WHEEL_SEGMENTS.size
                                        val targetRemainder = ((360f - chosenIndex * sweep) % 360f + 360f) % 360f
                                        val currentMod = ((rotation.value % 360f) + 360f) % 360f
                                        val deltaMod = ((targetRemainder - currentMod) % 360f + 360f) % 360f
                                        val jitter = Random.nextFloat() * 14f - 7f
                                        val targetAngle = rotation.value + 1800f + deltaMod + jitter

                                        rotation.animateTo(
                                            targetValue = targetAngle,
                                            animationSpec = tween(
                                                durationMillis = 3400,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        val won = SPIN_WHEEL_SEGMENTS[chosenIndex]
                                        lastWonSegment = won
                                        onSpinCompleted(won.reward, usedCoins)
                                        isSpinning = false
                                    }
                                }
                                .testTag("spin_wheel_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isSpinning) "..." else "Spin x1",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (canSpin) TextOnGold else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (isWide) 13.sp else 16.sp
                                )
                                Text(
                                    text = if (canUseTicket) "FREE" else "20 Coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (canSpin) TextOnGold else Color.LightGray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                val statusAndClose: @Composable () -> Unit = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(visible = lastWonSegment != null) {
                            lastWonSegment?.let { seg ->
                                Surface(
                                    color = EmeraldJackpot.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, EmeraldJackpot)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = ArcadeGold
                                        )
                                        Text(
                                            text = "Won ${seg.label} ${seg.subLabel}!",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ArcadeGold
                                        )
                                    }
                                }
                            }
                        }

                        if (lastWonSegment == null) {
                            Text(
                                text = if (canUseTicket) {
                                    "Tap Spin x1 to use 1 Free Bonus Ticket!"
                                } else {
                                    "Out of free tickets — spin for 20 Coins or earn tickets via Deposit & Check-in."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnDarkSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { if (!isSpinning) onDismiss() },
                            enabled = !isSpinning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPlumCard,
                                contentColor = TextOnDarkPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text("Back to Lobby")
                        }
                    }
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(0.46f),
                            contentAlignment = Alignment.Center
                        ) {
                            wheelWidget()
                        }
                        Column(
                            modifier = Modifier.weight(0.54f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = RoyalPlumCard,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = null,
                                            tint = WinterFrostCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Tickets: ${account.bonusTickets}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextOnDarkPrimary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { if (!isSpinning) onDismiss() },
                                    modifier = Modifier.testTag("close_spin_dialog")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Lucky Wheel",
                                        tint = TextOnDarkPrimary
                                    )
                                }
                            }
                            promoBanner()
                            statusAndClose()
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = RoyalPlumCard,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = WinterFrostCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Tickets: ${account.bonusTickets}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextOnDarkPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { if (!isSpinning) onDismiss() },
                                modifier = Modifier.testTag("close_spin_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Lucky Wheel",
                                    tint = TextOnDarkPrimary
                                )
                            }
                        }
                        promoBanner()
                        wheelWidget()
                        statusAndClose()
                    }
                }
            }
        }
    }
}
