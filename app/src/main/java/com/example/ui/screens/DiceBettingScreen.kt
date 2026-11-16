package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.DiceSoundEngine
import com.example.data.local.AccountEntity
import com.example.model.DiceBetZone
import com.example.model.DiceHistoryEntry
import com.example.model.DiceRngEngine
import com.example.model.DiceTableSeat
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.EmeraldJackpot
import com.example.ui.theme.FestiveCrimson
import com.example.ui.theme.RoyalPlumBorder
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.RoyalPlumDark
import com.example.ui.theme.RoyalPlumSurface
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.TextOnDarkPrimary
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.WinterFrostCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DiceBettingScreen(
    account: AccountEntity,
    onBack: () -> Unit,
    onOpenDeposit: () -> Unit,
    onPlayDiceRound: suspend (betCoins: Long, grossPayoutCoins: Long, summaryLabel: String) -> Long
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    val soundEngine = remember { DiceSoundEngine() }
    var soundEnabled by remember { mutableStateOf(true) }

    DisposableEffect(soundEngine) {
        onDispose { soundEngine.release() }
    }

    LaunchedEffect(soundEnabled) {
        soundEngine.enabled = soundEnabled
    }

    var history by remember { mutableStateOf(DiceRngEngine.INITIAL_HISTORY) }
    var npcSeats by remember { mutableStateOf(DiceRngEngine.DEFAULT_NPC_SEATS) }
    var selectedChip by remember { mutableLongStateOf(10L) }

    val placedBets = remember { mutableStateMapOf<DiceBetZone, Long>() }
    var lastRoundBets by remember { mutableStateOf<Map<DiceBetZone, Long>>(emptyMap()) }
    val tableZonePool = remember {
        mutableStateMapOf(
            DiceBetZone.LOW_2_6 to 58_990L,
            DiceBetZone.EXACT_7 to 22_040L,
            DiceBetZone.HIGH_8_12 to 41_250L
        )
    }

    var die1 by remember { mutableIntStateOf(4) }
    var die2 by remember { mutableIntStateOf(2) }
    var rollStepIndex by remember { mutableIntStateOf(0) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var phaseLabel by remember { mutableStateOf("Betting") }
    var isRolling by remember { mutableStateOf(false) }
    var winningZones by remember { mutableStateOf<Set<DiceBetZone>>(setOf(DiceBetZone.LOW_2_6)) }
    var lightningZone by remember { mutableStateOf<DiceBetZone?>(null) }
    var roundBannerText by remember { mutableStateOf<String?>(null) }

    var spectatorToggle by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showTrendDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val hasMinimumCoins = account.coins >= DiceRngEngine.MIN_ENTRY_COINS
    val isWatchMode = !hasMinimumCoins || spectatorToggle
    val totalPlacedBet = placedBets.values.sum()
    val displayedAvailableCoins = (account.coins - totalPlacedBet).coerceAtLeast(0L)

    suspend fun executeDiceRollRound() {
        if (isRolling) return
        isRolling = true
        phaseLabel = "Rolling"
        roundBannerText = null
        winningZones = emptySet()

        // Fast yet smooth, readable 6-step dice throw (~0.81s)
        repeat(6) { step ->
            die1 = Random.nextInt(1, 7)
            die2 = Random.nextInt(1, 7)
            rollStepIndex += 1
            soundEngine.playDiceTumbleStep(step)
            delay(135L)
        }

        val activeBets = if (isWatchMode) emptyMap() else placedBets.toMap()
        if (activeBets.isNotEmpty()) {
            lastRoundBets = activeBets
        }

        val outcome = DiceRngEngine.rollRound(activeBets)
        die1 = outcome.die1
        die2 = outcome.die2
        rollStepIndex += 1
        winningZones = outcome.winningZones.toSet()
        lightningZone = outcome.lightningZone
        phaseLabel = "Prizeing"

        history = (listOf(DiceHistoryEntry(outcome.sum, outcome.hitLightning)) + history).take(12)

        // Refresh simulated table pool & NPC balances realistically
        DiceBetZone.entries.forEach { z ->
            tableZonePool[z] = Random.nextLong(9_500L, 65_000L)
        }
        npcSeats = npcSeats.map { seat ->
            val delta = Random.nextLong(-180L, 260L)
            val nextVal = (seat.balanceValue + delta).coerceAtLeast(1_500L)
            val cents = (nextVal % 97).toInt().toString().padStart(2, '0')
            seat.copy(
                balanceValue = nextVal,
                balanceText = "${"%,d".format(nextVal)}.$cents"
            )
        }

        if (!isWatchMode && outcome.totalBet > 0L) {
            val finalPayout = onPlayDiceRound(
                outcome.totalBet,
                outcome.grossPayout,
                "Sum ${outcome.sum}"
            )
            placedBets.clear()
            if (finalPayout > 0L) {
                soundEngine.playWinChime(outcome.hitLightning)
                roundBannerText = "Sum ${outcome.sum} • WON +$finalPayout Coins!"
            } else {
                soundEngine.playRoundSettle()
                roundBannerText = "Sum ${outcome.sum} (${outcome.die1}+${outcome.die2})"
            }
        } else {
            soundEngine.playRoundSettle()
            roundBannerText = "Rolled ${outcome.die1} + ${outcome.die2} = ${outcome.sum}"
        }

        isRolling = false
        delay(900L)
        phaseLabel = "Betting"
        countdownSeconds = 5
    }

    // Fast live auto-cycling round countdown with final-seconds tick sound
    LaunchedEffect(Unit) {
        while (true) {
            delay(850L)
            if (!isRolling) {
                if (countdownSeconds > 1) {
                    countdownSeconds -= 1
                    if (countdownSeconds <= 3) {
                        soundEngine.playTimerTick(urgent = countdownSeconds == 1)
                    }
                } else {
                    countdownSeconds = 0
                    executeDiceRollRound()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0D22), Color(0xFF15183C), Color(0xFF090B1C))
                )
            )
    ) {
        // Subtle diamond lattice texture across midnight backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 42.dp.toPx()
            val strokeColor = Color(0x146366F1)
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(strokeColor, Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 1f)
                drawLine(strokeColor, Offset(x + size.height, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER: Home + Trend | History Bar | Info + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularGoldIconButton(
                    icon = Icons.Default.Home,
                    contentDescription = "Back to Lobby",
                    testTag = "dice_back_button",
                    onClick = onBack
                )
                CircularGoldIconButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Roll Trends",
                    testTag = "dice_trend_button",
                    onClick = { showTrendDialog = true }
                )

                // History Bar Pill (Red=2-6, Blue=7, Teal=8-12 + Yellow Lightning Bolt)
                Surface(
                    color = Color(0xCC0D1028),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        history.forEach { entry ->
                            HistoryTile(entry)
                        }
                    }
                }

                CircularGoldIconButton(
                    icon = Icons.Default.Info,
                    contentDescription = "How to Play",
                    testTag = "dice_rules_button",
                    onClick = { showRulesDialog = true }
                )
                CircularGoldIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Table Settings",
                    testTag = "dice_settings_button",
                    onClick = { showSettingsDialog = true }
                )
            }

            // 2. CENTER ARENA: Left 3 Players | Golden Oval Purple Table | Right 3 Players
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                val isWide = maxWidth >= 560.dp
                val leftSeats = remember(npcSeats) { npcSeats.filter { it.isLeftSide } }
                val rightSeats = remember(npcSeats) { npcSeats.filter { !it.isLeftSide } }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Left 3 Seats
                        Column(
                            modifier = Modifier
                                .width(106.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            leftSeats.forEach { seat ->
                                ReferencePlayerSeatCard(seat)
                            }
                        }

                        // Golden Oval Casino Table
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GoldenOvalDiceTable(
                                die1 = die1,
                                die2 = die2,
                                rollStepIndex = rollStepIndex,
                                countdownSeconds = countdownSeconds,
                                phaseLabel = phaseLabel,
                                isRolling = isRolling,
                                isWatchMode = isWatchMode,
                                winningZones = winningZones,
                                lightningZone = lightningZone,
                                roundBannerText = roundBannerText,
                                placedBets = placedBets,
                                tableZonePool = tableZonePool,
                                onZoneTap = { zone ->
                                    if (!isRolling && !isWatchMode && (account.coins - totalPlacedBet) >= selectedChip) {
                                        placedBets[zone] = (placedBets[zone] ?: 0L) + selectedChip
                                        soundEngine.playChipTap()
                                    }
                                },
                                onTapRoll = {
                                    scope.launch { executeDiceRollRound() }
                                }
                            )
                        }

                        // Right 3 Seats
                        Column(
                            modifier = Modifier
                                .width(106.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            rightSeats.forEach { seat ->
                                ReferencePlayerSeatCard(seat)
                            }
                        }
                    }
                } else {
                    // Compact Portrait fallback (no nested verticalScroll)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            GoldenOvalDiceTable(
                                die1 = die1,
                                die2 = die2,
                                rollStepIndex = rollStepIndex,
                                countdownSeconds = countdownSeconds,
                                phaseLabel = phaseLabel,
                                isRolling = isRolling,
                                isWatchMode = isWatchMode,
                                winningZones = winningZones,
                                lightningZone = lightningZone,
                                roundBannerText = roundBannerText,
                                placedBets = placedBets,
                                tableZonePool = tableZonePool,
                                onZoneTap = { zone ->
                                    if (!isRolling && !isWatchMode && (account.coins - totalPlacedBet) >= selectedChip) {
                                        placedBets[zone] = (placedBets[zone] ?: 0L) + selectedChip
                                        soundEngine.playChipTap()
                                    }
                                },
                                onTapRoll = {
                                    scope.launch { executeDiceRollRound() }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (leftSeats + rightSeats).forEach { seat ->
                                ReferencePlayerSeatCard(seat, modifier = Modifier.width(102.dp))
                            }
                        }
                    }
                }
            }

            // 3. BOTTOM HUD BAR: User Profile | Chips or WATCHING THE BATTLE. | REPEAT / ROLL + 31 Players
            Surface(
                color = Color(0xDD0B0E22),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2E325C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bottom-Left: Current User Avatar + Username + Coins
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .alpha(if (isWatchMode) 0.55f else 1f)
                            .clickable(onClick = onOpenDeposit)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_vip),
                            contentDescription = "Your Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4C1D95))
                        )
                        Column {
                            Text(
                                text = account.username,
                                color = TextOnDarkPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, null, tint = ArcadeGold, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "$displayedAvailableCoins",
                                    color = ArcadeGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Bottom-Center: Casino Chips Row OR WATCHING THE BATTLE. Overlay
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .alpha(if (isWatchMode) 0.25f else 1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiceRngEngine.CHIP_DENOMINATIONS.forEach { chip ->
                                CasinoChipButton(
                                    value = chip,
                                    selected = !isWatchMode && selectedChip == chip,
                                    enabled = !isWatchMode && !isRolling && account.coins >= chip,
                                    onClick = {
                                        selectedChip = chip
                                        soundEngine.playChipTap()
                                    }
                                )
                            }
                        }

                        if (isWatchMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("watching_battle_banner")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "WATCHING THE BATTLE.",
                                        color = ArcadeGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = if (!hasMinimumCoins) {
                                            "You need at least 10 coins to play the game"
                                        } else {
                                            "Spectator mode active • Tap Settings to play"
                                        },
                                        color = Color(0xFFFDE68A),
                                        fontSize = 10.sp
                                    )
                                }
                                if (!hasMinimumCoins) {
                                    Surface(
                                        onClick = onOpenDeposit,
                                        color = EmeraldJackpot,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, ArcadeGold)
                                    ) {
                                        Text(
                                            text = "+ Coins",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom-Right: ROLL / REPEAT + Online Count (31)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isWatchMode && totalPlacedBet > 0L) {
                            Button(
                                onClick = { scope.launch { executeDiceRollRound() } },
                                enabled = !isRolling,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldJackpot, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("dice_roll_button")
                            ) {
                                Text(
                                    text = if (isRolling) "..." else "ROLL ($totalPlacedBet)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Surface(
                                onClick = {
                                    if (!isWatchMode && !isRolling && lastRoundBets.isNotEmpty()) {
                                        val needed = lastRoundBets.values.sum()
                                        if (account.coins >= needed) {
                                            placedBets.clear()
                                            placedBets.putAll(lastRoundBets)
                                            soundEngine.playChipTap()
                                        }
                                    }
                                },
                                color = if (isWatchMode) Color(0xFF151936) else Color(0xFF166534),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isWatchMode) Color(0xFF334155) else ArcadeGold),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("dice_repeat_button")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "REPEAT",
                                        color = if (isWatchMode) Color(0xFF475569) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Online Players Counter Badge (31)
                        Surface(
                            onClick = { showTrendDialog = true },
                            color = Color(0xFF1E224A),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Group, null, tint = WinterFrostCyan, modifier = Modifier.size(16.dp))
                                Text("31", color = ArcadeGold, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Compact Sound Mute/Unmute Toggle
                        Surface(
                            onClick = { soundEnabled = !soundEnabled },
                            shape = CircleShape,
                            color = Color(0xFF141836),
                            border = BorderStroke(1.2.dp, ArcadeGold.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("dice_sound_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = if (soundEnabled) "Mute Game Sound" else "Enable Game Sound",
                                    tint = ArcadeGold,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            containerColor = RoyalPlumSurface,
            title = { Text("Dice Battle • Kaise Khelein", color = ArcadeGold, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiceRngEngine.HOW_TO_PLAY_STEPS.forEachIndexed { idx, step ->
                        Text("${idx + 1}. $step", style = MaterialTheme.typography.bodySmall, color = TextOnDarkPrimary)
                    }
                    Surface(
                        color = RoyalPlumCard,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SunsetOrange)
                    ) {
                        Text(
                            text = DiceRngEngine.RNG_WARNING_NOTICE,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("Close", color = ArcadeGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showTrendDialog) {
        AlertDialog(
            onDismissRequest = { showTrendDialog = false },
            containerColor = RoyalPlumSurface,
            title = { Text("Recent Roll Distribution", color = ArcadeGold) },
            text = {
                val lowCount = history.count { it.sum in 2..6 }
                val sevenCount = history.count { it.sum == 7 }
                val highCount = history.count { it.sum in 8..12 }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Last ${history.size} Rolls:", color = TextOnDarkSecondary, fontSize = 12.sp)
                    Text("• 2 - 6 (Low): $lowCount rounds", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                    Text("• Lucky 7: $sevenCount rounds", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                    Text("• 8 - 12 (High): $highCount rounds", color = EmeraldJackpot, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Note: Every roll is generated independently via RNG.",
                        color = TextOnDarkSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrendDialog = false }) {
                    Text("OK", color = ArcadeGold)
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = RoyalPlumSurface,
            title = { Text("Table Options", color = ArcadeGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { soundEnabled = !soundEnabled },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalPlumCard,
                            contentColor = ArcadeGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (soundEnabled) "Game Sound: ON (Tap to Mute)" else "Game Sound: OFF (Tap to Enable)")
                    }
                    if (hasMinimumCoins) {
                        Button(
                            onClick = {
                                spectatorToggle = !spectatorToggle
                                if (spectatorToggle) placedBets.clear()
                                showSettingsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPlumCard, contentColor = WinterFrostCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isWatchMode) "Switch to Active Betting Mode" else "Switch to Watching Mode")
                        }
                    }
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            onOpenDeposit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buy / Deposit Coins", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            onBack()
                        },
                        border = BorderStroke(1.dp, FestiveCrimson),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Lobby", color = FestiveCrimson)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close", color = TextOnDarkPrimary)
                }
            }
        )
    }
}

@Composable
private fun CircularGoldIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF141836),
        border = BorderStroke(1.5.dp, ArcadeGold),
        modifier = Modifier
            .size(38.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = ArcadeGold, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun HistoryTile(entry: DiceHistoryEntry) {
    val bg = when {
        entry.sum == 7 -> Color(0xFF1D4ED8)
        entry.sum >= 8 -> Color(0xFF0F766E)
        else -> Color(0xFFB91C1C)
    }
    Box(
        modifier = Modifier
            .size(width = 26.dp, height = 24.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${entry.sum}",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (entry.hasLightning2x) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "2X Lightning",
                tint = ArcadeGold,
                modifier = Modifier
                    .size(11.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun ReferencePlayerSeatCard(
    seat: DiceTableSeat,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = if (seat.cosmeticTitle != null) 8.dp else 0.dp)
        ) {
            Surface(
                color = Color(seat.avatarColorHex),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, ArcadeGold),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (seat.id == "seat_5") {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_vip),
                            contentDescription = seat.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        StylizedAnimeSeatPortrait(
                            seatId = seat.id,
                            accentHex = seat.avatarColorHex
                        )
                    }
                }
            }
            Surface(
                color = Color(0xCC0D1028),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.8.dp, Color(0xFF475569)),
                modifier = Modifier.offset(y = (-4).dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = seat.displayName,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = seat.balanceText,
                        color = ArcadeGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }

        if (seat.cosmeticTitle != null) {
            val badgeBg = when (seat.cosmeticTitle) {
                "Richie Rich" -> Brush.horizontalGradient(listOf(Color(0xFFF472B6), Color(0xFFA855F7)))
                "Millionaire" -> Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFF6366F1)))
                else -> Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeBg)
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            ) {
                Text(
                    text = seat.cosmeticTitle,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StylizedAnimeSeatPortrait(
    seatId: String,
    accentHex: Long
) {
    val hairColor = when (seatId) {
        "seat_1" -> Color(0xFF4C1D95)
        "seat_2" -> Color(0xFF1E1B4B)
        "seat_3" -> Color(0xFF475569)
        "seat_4" -> Color(0xFF27272A)
        else -> Color(0xFF6366F1)
    }
    val bgTop = Color(accentHex).copy(alpha = 0.85f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(listOf(bgTop, Color(0xFF111827)))
        )
        // Shoulders / coat
        drawCircle(
            color = Color(0xFF1F2937),
            radius = size.width * 0.44f,
            center = Offset(size.width * 0.5f, size.height * 0.98f)
        )
        // Hair backdrop
        drawCircle(
            color = hairColor,
            radius = size.width * 0.27f,
            center = Offset(size.width * 0.5f, size.height * 0.40f)
        )
        // Face
        drawCircle(
            color = Color(0xFFFDE2C8),
            radius = size.width * 0.20f,
            center = Offset(size.width * 0.5f, size.height * 0.44f)
        )
        // Hair bangs
        drawArc(
            color = hairColor,
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = true,
            topLeft = Offset(size.width * 0.28f, size.height * 0.17f),
            size = Size(size.width * 0.44f, size.height * 0.34f)
        )
    }
}

@Composable
private fun GoldenOvalDiceTable(
    die1: Int,
    die2: Int,
    rollStepIndex: Int,
    countdownSeconds: Int,
    phaseLabel: String,
    isRolling: Boolean,
    isWatchMode: Boolean,
    winningZones: Set<DiceBetZone>,
    lightningZone: DiceBetZone?,
    roundBannerText: String?,
    placedBets: Map<DiceBetZone, Long>,
    tableZonePool: Map<DiceBetZone, Long>,
    onZoneTap: (DiceBetZone) -> Unit,
    onTapRoll: () -> Unit
) {
    // Beveled metallic gold rim around seamless purple-magenta velvet oval (matching screenshot)
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(
            width = 5.dp,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFEF08A), ArcadeGold, Color(0xFFB45309), Color(0xFFFDE047))
            )
        ),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF9333EA), Color(0xFF6B21A8), Color(0xFF3E0F66))
                    )
                )
        ) {
            // Seamless curved felt lines + bright golden neon glow on the winning crescent (no rectangular boxes!)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Subtle top horizontal felt ribbon behind pool counter
                drawRect(
                    color = Color(0x302563EB),
                    topLeft = Offset(w * 0.25f, h * 0.24f),
                    size = Size(w * 0.50f, h * 0.08f)
                )

                val leftWinner = winningZones.contains(DiceBetZone.LOW_2_6)
                val rightWinner = winningZones.contains(DiceBetZone.HIGH_8_12)
                val centerWinner = winningZones.contains(DiceBetZone.EXACT_7)

                if (leftWinner) {
                    val glowArc = Path().apply {
                        moveTo(w * 0.24f, h * 0.08f)
                        quadraticTo(w * 0.02f, h * 0.50f, w * 0.28f, h * 0.94f)
                        lineTo(w * 0.44f, h * 0.94f)
                        quadraticTo(w * 0.30f, h * 0.55f, w * 0.27f, h * 0.08f)
                        close()
                    }
                    drawPath(
                        glowArc,
                        brush = Brush.radialGradient(
                            listOf(Color(0x55FEF08A), Color(0x18F59E0B)),
                            center = Offset(w * 0.24f, h * 0.55f)
                        )
                    )
                }
                if (rightWinner) {
                    val glowArc = Path().apply {
                        moveTo(w * 0.76f, h * 0.08f)
                        quadraticTo(w * 0.98f, h * 0.50f, w * 0.72f, h * 0.94f)
                        lineTo(w * 0.56f, h * 0.94f)
                        quadraticTo(w * 0.70f, h * 0.55f, w * 0.73f, h * 0.08f)
                        close()
                    }
                    drawPath(
                        glowArc,
                        brush = Brush.radialGradient(
                            listOf(Color(0x55FEF08A), Color(0x18F59E0B)),
                            center = Offset(w * 0.76f, h * 0.55f)
                        )
                    )
                }
                if (centerWinner) {
                    drawOval(
                        brush = Brush.radialGradient(
                            listOf(Color(0x55FEF08A), Color.Transparent)
                        ),
                        topLeft = Offset(w * 0.32f, h * 0.26f),
                        size = Size(w * 0.36f, h * 0.52f)
                    )
                }

                // Left crescent boundary (2-6)
                val leftCurve = Path().apply {
                    moveTo(w * 0.26f, h * 0.05f)
                    quadraticTo(w * 0.31f, h * 0.56f, w * 0.44f, h * 0.95f)
                }
                if (leftWinner) {
                    drawPath(
                        leftCurve,
                        color = Color(0x66FEF08A),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        leftCurve,
                        color = Color(0xFFFEF08A),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    drawPath(
                        leftCurve,
                        color = Color(0x45F3E8FF),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Right crescent boundary (8-12)
                val rightCurve = Path().apply {
                    moveTo(w * 0.74f, h * 0.05f)
                    quadraticTo(w * 0.69f, h * 0.56f, w * 0.56f, h * 0.95f)
                }
                if (rightWinner) {
                    drawPath(
                        rightCurve,
                        color = Color(0x66FEF08A),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        rightCurve,
                        color = Color(0xFFFEF08A),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    drawPath(
                        rightCurve,
                        color = Color(0x45F3E8FF),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP STRIP INSIDE OVAL: Golden Stopwatch + Twin Dice Dome + Result Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Authentic 3D Stopwatch Icon with top crown loop
                    Box(contentAlignment = Alignment.TopCenter) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 6.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(ArcadeGold)
                        )
                        Surface(
                            onClick = onTapRoll,
                            shape = CircleShape,
                            color = Color(0xFF6B21A8),
                            border = BorderStroke(2.5.dp, ArcadeGold),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(44.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$countdownSeconds",
                                    color = Color(0xFFFEF08A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    text = phaseLabel,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 8.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Twin Dice Dome (Left: Glass ring with 2 dice | Right: Glossy Black Dome Cover)
                    TwinDiceDomePod(
                        die1 = die1,
                        die2 = die2,
                        rollStepIndex = rollStepIndex,
                        isRolling = isRolling
                    )

                    if (roundBannerText != null) {
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            color = Color(0xCC130826),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ArcadeGold)
                        ) {
                            Text(
                                text = roundBannerText,
                                color = ArcadeGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3-STEP SEAMLESS FELT BETTING ZONES: [2 - 6] | [7 (Top-Center)] | [8 - 12]
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1: Left Crescent (2 - 6)
                    SeamlessFeltZone(
                        zone = DiceBetZone.LOW_2_6,
                        poolAmount = tableZonePool[DiceBetZone.LOW_2_6] ?: 58_990L,
                        userStake = placedBets[DiceBetZone.LOW_2_6] ?: 0L,
                        isWinner = winningZones.contains(DiceBetZone.LOW_2_6),
                        hasLightning = lightningZone == DiceBetZone.LOW_2_6,
                        isWatchMode = isWatchMode,
                        watermark = "2 - 6",
                        counterAtTop = false,
                        scatteredChips = LEFT_ZONE_CHIPS,
                        onClick = { onZoneTap(DiceBetZone.LOW_2_6) },
                        modifier = Modifier
                            .weight(0.34f)
                            .fillMaxHeight()
                    )

                    // Step 2: Upper-Center Lucky 7 (no ODD/EVEN below!)
                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SeamlessFeltZone(
                            zone = DiceBetZone.EXACT_7,
                            poolAmount = tableZonePool[DiceBetZone.EXACT_7] ?: 22_040L,
                            userStake = placedBets[DiceBetZone.EXACT_7] ?: 0L,
                            isWinner = winningZones.contains(DiceBetZone.EXACT_7),
                            hasLightning = lightningZone == DiceBetZone.EXACT_7,
                            isWatchMode = isWatchMode,
                            watermark = "7",
                            counterAtTop = true,
                            scatteredChips = CENTER_ZONE_CHIPS,
                            onClick = { onZoneTap(DiceBetZone.EXACT_7) },
                            modifier = Modifier
                                .weight(0.76f)
                                .fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.24f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2 - 6   •   7   •   8 - 12",
                                color = Color(0x99FEF08A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Step 3: Right Crescent (8 - 12)
                    SeamlessFeltZone(
                        zone = DiceBetZone.HIGH_8_12,
                        poolAmount = tableZonePool[DiceBetZone.HIGH_8_12] ?: 14_530L,
                        userStake = placedBets[DiceBetZone.HIGH_8_12] ?: 0L,
                        isWinner = winningZones.contains(DiceBetZone.HIGH_8_12),
                        hasLightning = lightningZone == DiceBetZone.HIGH_8_12,
                        isWatchMode = isWatchMode,
                        watermark = "8 - 12",
                        counterAtTop = false,
                        scatteredChips = RIGHT_ZONE_CHIPS,
                        onClick = { onZoneTap(DiceBetZone.HIGH_8_12) },
                        modifier = Modifier
                            .weight(0.34f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

private data class ScatteredChipSpec(
    val amount: Long,
    val biasX: Float,
    val biasY: Float
)

private val LEFT_ZONE_CHIPS = listOf(
    ScatteredChipSpec(1000L, 0.36f, 0.22f),
    ScatteredChipSpec(500L, 0.22f, 0.36f),
    ScatteredChipSpec(5000L, 0.44f, 0.44f),
    ScatteredChipSpec(100L, 0.26f, 0.56f),
    ScatteredChipSpec(500L, 0.52f, 0.62f),
    ScatteredChipSpec(1000L, 0.30f, 0.72f),
    ScatteredChipSpec(500L, 0.56f, 0.76f)
)

private val CENTER_ZONE_CHIPS = listOf(
    ScatteredChipSpec(1000L, 0.18f, 0.38f),
    ScatteredChipSpec(5000L, 0.32f, 0.56f),
    ScatteredChipSpec(10L, 0.48f, 0.42f),
    ScatteredChipSpec(100L, 0.28f, 0.72f),
    ScatteredChipSpec(5000L, 0.68f, 0.48f),
    ScatteredChipSpec(100L, 0.62f, 0.68f)
)

private val RIGHT_ZONE_CHIPS = listOf(
    ScatteredChipSpec(5000L, 0.56f, 0.24f),
    ScatteredChipSpec(1000L, 0.74f, 0.34f),
    ScatteredChipSpec(50L, 0.54f, 0.44f),
    ScatteredChipSpec(10L, 0.66f, 0.56f),
    ScatteredChipSpec(500L, 0.48f, 0.60f),
    ScatteredChipSpec(50L, 0.72f, 0.66f),
    ScatteredChipSpec(1000L, 0.68f, 0.78f)
)

@Composable
private fun SeamlessFeltZone(
    zone: DiceBetZone,
    poolAmount: Long,
    userStake: Long,
    isWinner: Boolean,
    hasLightning: Boolean,
    isWatchMode: Boolean,
    watermark: String,
    counterAtTop: Boolean,
    scatteredChips: List<ScatteredChipSpec>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = !isWatchMode, onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("dice_zone_${zone.id}")
    ) {
        val zoneW = maxWidth
        val zoneH = maxHeight

        // 1. Etched purple-felt watermark title & odds
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = watermark,
                color = Color.White.copy(alpha = if (isWinner) 0.55f else 0.24f),
                fontSize = if (watermark.length <= 2) 34.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = zone.oddsLabel,
                color = Color.White.copy(alpha = if (isWinner) 0.45f else 0.22f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Scattered realistic 3D casino chip piles
        scatteredChips.forEach { spec ->
            Box(
                modifier = Modifier.offset(
                    x = zoneW * spec.biasX - 10.dp,
                    y = zoneH * spec.biasY - 10.dp
                )
            ) {
                MiniTableChip(spec.amount, size = 20.dp)
            }
        }

        // Extra bright chip badge when player has staked on this zone
        if (userStake > 0L) {
            Surface(
                color = Color(0xDD14532D),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, ArcadeGold),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "+$userStake",
                    color = Color(0xFFFEF08A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // 3. Lightning 2X badge when struck by lightning
        if (hasLightning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 18.dp, end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "2X Lightning",
                    tint = Color(0xFFFEF08A),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "2X",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 4. Translucent pool & user stake counter bar (top for 7, bottom for 2-6 & 8-12)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(if (counterAtTop) Alignment.TopCenter else Alignment.BottomCenter)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, null, tint = Color(0xFF93C5FD), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "%,d".format(poolAmount),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$userStake",
                    color = if (userStake > 0L) ArcadeGold else Color(0xFFFCA5A5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (userStake > 0L) ArcadeGold else Color(0xFFFCA5A5),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun TwinDiceDomePod(
    die1: Int,
    die2: Int,
    rollStepIndex: Int,
    isRolling: Boolean
) {
    // Stationary when idle; smoothly rotates 45 deg per step in sync with 135ms roll steps
    val smoothRollAngle by animateFloatAsState(
        targetValue = rollStepIndex * 45f,
        animationSpec = tween(durationMillis = 125, easing = LinearEasing),
        label = "slow_dice_roll_angle"
    )
    val domeGlowAlpha by animateFloatAsState(
        targetValue = if (isRolling) 0.9f else 0.35f,
        animationSpec = tween(durationMillis = 180),
        label = "dome_glow_alpha"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Pod: Transparent Glass Ring with 2 Smoothly Tumbling Dice
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF1F2937), Color(0xFF030712))),
                    radius = r
                )
                rotate(smoothRollAngle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF6B7280),
                                ArcadeGold.copy(alpha = domeGlowAlpha),
                                Color(0xFF9CA3AF)
                            )
                        ),
                        radius = r - 2.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                val dSize = size.minDimension * 0.34f
                val leftDieCenter = Offset(size.width * 0.14f + dSize / 2f, size.height * 0.33f + dSize / 2f)
                val rightDieCenter = Offset(size.width * 0.52f + dSize / 2f, size.height * 0.33f + dSize / 2f)
                rotate(degrees = if (isRolling) (smoothRollAngle % 360f) else 0f, pivot = leftDieCenter) {
                    drawDomeDie(die1, Offset(size.width * 0.14f, size.height * 0.33f), dSize, Color(0xFF15803D))
                }
                rotate(degrees = if (isRolling) -(smoothRollAngle % 360f) else 0f, pivot = rightDieCenter) {
                    drawDomeDie(die2, Offset(size.width * 0.52f, size.height * 0.33f), dSize, Color(0xFFF8FAFC))
                }
            }
        }

        // Right Pod: Glossy Black Dome Cover
        Canvas(modifier = Modifier.size(50.dp)) {
            val r = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4B5563), Color(0xFF111827), Color(0xFF030712)),
                    center = Offset(size.width * 0.38f, size.height * 0.34f)
                ),
                radius = r
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = r * 0.28f,
                center = Offset(size.width * 0.36f, size.height * 0.32f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDomeDie(
    value: Int,
    topLeft: Offset,
    dieSize: Float,
    bgColor: Color
) {
    drawRoundRect(
        color = bgColor,
        topLeft = topLeft,
        size = Size(dieSize, dieSize),
        cornerRadius = CornerRadius(dieSize * 0.22f, dieSize * 0.22f)
    )
    val pipPositions = when (value.coerceIn(1, 6)) {
        1 -> listOf(0.5f to 0.5f)
        2 -> listOf(0.28f to 0.28f, 0.72f to 0.72f)
        3 -> listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f)
        4 -> listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f)
        5 -> listOf(0.26f to 0.26f, 0.74f to 0.26f, 0.5f to 0.5f, 0.26f to 0.74f, 0.74f to 0.74f)
        else -> listOf(
            0.28f to 0.24f, 0.72f to 0.24f,
            0.28f to 0.5f, 0.72f to 0.5f,
            0.28f to 0.76f, 0.72f to 0.76f
        )
    }
    val pipColor = if (bgColor == Color(0xFF15803D)) Color.White else if (value == 1 || value == 4) Color(0xFFDC2626) else Color(0xFF111827)
    pipPositions.forEach { (px, py) ->
        drawCircle(
            color = pipColor,
            radius = dieSize * 0.1f,
            center = Offset(topLeft.x + dieSize * px, topLeft.y + dieSize * py)
        )
    }
}

@Composable
private fun CasinoChipButton(
    value: Long,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("dice_chip_$value"),
        contentAlignment = Alignment.Center
    ) {
        MiniTableChip(value = value, size = 38.dp, highlighted = selected)
    }
}

@Composable
private fun MiniTableChip(
    value: Long,
    size: Dp,
    highlighted: Boolean = false
) {
    val chipColor = when (value) {
        10L -> Color(0xFF16A34A)
        50L -> Color(0xFF2563EB)
        100L -> Color(0xFF7C3AED)
        500L -> Color(0xFFDC2626)
        1000L -> Color(0xFFD97706)
        else -> Color(0xFF334155)
    }
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            drawCircle(color = chipColor, radius = r)
            drawCircle(
                color = if (highlighted) ArcadeGold else Color.White,
                radius = r - 2.dp.toPx(),
                style = Stroke(
                    width = if (highlighted) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )
            drawCircle(color = chipColor.copy(alpha = 0.85f), radius = r * 0.68f)
        }
        Text(
            text = DiceRngEngine.formatChipValue(value),
            color = Color.White,
            fontSize = if (size >= 30.dp) 9.sp else 6.5.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}
