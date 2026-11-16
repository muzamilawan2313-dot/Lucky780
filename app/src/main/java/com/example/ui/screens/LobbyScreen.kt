package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccountEntity
import com.example.data.local.AdminControlSettings
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeGoldSoft
import com.example.ui.theme.EmeraldJackpot
import com.example.ui.theme.EmeraldJackpotDark
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
import com.example.viewmodel.LobbyOverlay
import com.example.viewmodel.WalletTab

@Composable
fun LobbyScreen(
    account: AccountEntity,
    adminSettings: AdminControlSettings,
    unreadMailCount: Int,
    onBuyCoin: () -> Unit,
    onOpenWalletTab: (WalletTab) -> Unit,
    onOpenOverlay: (LobbyOverlay) -> Unit,
    onClaimFreeCash: () -> Unit,
    onOpenDiceGame: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    var showSettingsMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Festive Winter Sunset Carnival Background
        Image(
            painter = painterResource(id = R.drawable.img_login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x2AE879F9),
                            Color(0x24F472B6),
                            Color(0x4A581C87)
                        )
                    )
                )
        )

        // Subtle falling snow sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val flakes = listOf(
                0.15f to 0.22f, 0.44f to 0.16f, 0.78f to 0.25f,
                0.28f to 0.56f, 0.64f to 0.62f, 0.89f to 0.48f
            )
            flakes.forEachIndexed { idx, (fx, fy) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = (3 + (idx % 3) * 2).toFloat(),
                    center = Offset(size.width * fx, size.height * fy)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Snow-Capped Top Lobby Bar (Avatar + VIP + ID + Coins + BUY COIN + Quick Icons + Gear)
            CarnivalTopHeaderBar(
                account = account,
                onBuyCoin = onBuyCoin,
                onOpenBind = { onOpenWalletTab(WalletTab.BANK) },
                onClaimFreeCash = onClaimFreeCash,
                onOpenSpinWheel = { onOpenOverlay(LobbyOverlay.SPIN_WHEEL) },
                onOpenGift = { onOpenOverlay(LobbyOverlay.GIFT) },
                onOpenCheckIn = { onOpenOverlay(LobbyOverlay.CHECK_IN) },
                onOpenSupport = { onOpenOverlay(LobbyOverlay.SUPPORT_AI) },
                onOpenVip = { onOpenOverlay(LobbyOverlay.VIP) },
                onOpenSettings = { showSettingsMenu = true }
            )

            // 2. Brand Crest + Announcement Marquee Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_luck780_logo),
                    contentDescription = "Luck780 Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0x992D0E3D),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = adminSettings.marqueeAnnouncement,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. Center Stage: Featured Dice Battle Arena + Live Spin & Rewards Hub
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                FeaturedDiceBattleStage(
                    account = account,
                    onOpenDiceGame = onOpenDiceGame,
                    onOpenSpinWheel = { onOpenOverlay(LobbyOverlay.SPIN_WHEEL) },
                    onOpenCheckIn = { onOpenOverlay(LobbyOverlay.CHECK_IN) },
                    onClaimFreeCash = onClaimFreeCash
                )
            }

            // 4. Festive Wooden & Emerald Bottom Dock (FREE BONUS + Green Pill Bar + DEPOSIT Shop)
            CarnivalBottomDock(
                unreadMailCount = unreadMailCount,
                freeSpinPromoEnabled = adminSettings.freeSpinPromoEnabled,
                onFreeBonus = { onOpenOverlay(LobbyOverlay.SPIN_WHEEL) },
                onWithdraw = { onOpenWalletTab(WalletTab.WITHDRAW) },
                onVip = { onOpenOverlay(LobbyOverlay.VIP) },
                onBank = { onOpenWalletTab(WalletTab.BANK) },
                onRanking = { onOpenWalletTab(WalletTab.RANKING_HISTORY) },
                onEmail = { onOpenOverlay(LobbyOverlay.EMAIL) },
                onEvent = { onOpenOverlay(LobbyOverlay.EVENT) },
                onPromote = { onOpenOverlay(LobbyOverlay.PROMOTE) },
                onDeposit = { onOpenWalletTab(WalletTab.DEPOSIT) }
            )
        }
    }

    if (showSettingsMenu) {
        LobbySettingsDialog(
            account = account,
            onDismiss = { showSettingsMenu = false },
            onOpenAdmin = {
                showSettingsMenu = false
                onOpenAdmin()
            },
            onOpenSupport = {
                showSettingsMenu = false
                onOpenOverlay(LobbyOverlay.SUPPORT_AI)
            },
            onLogout = {
                showSettingsMenu = false
                onLogout()
            }
        )
    }
}

@Composable
private fun CarnivalTopHeaderBar(
    account: AccountEntity,
    onBuyCoin: () -> Unit,
    onOpenBind: () -> Unit,
    onClaimFreeCash: () -> Unit,
    onOpenSpinWheel: () -> Unit,
    onOpenGift: () -> Unit,
    onOpenCheckIn: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenVip: () -> Unit,
    onOpenSettings: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC6D28D9), Color(0xAAEC4899), Color(0x88F9A8D4))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val isWideHeader = maxWidth >= 660.dp
        val rowModifier = if (isWideHeader) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        }

        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isWideHeader) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp)
        ) {
            // Left: Avatar + VIP Ribbon + Username/ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onOpenVip() }
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.TopCenter) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar_vip),
                        contentDescription = "Player Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, ArcadeGold, CircleShape)
                    )
                    Surface(
                        color = FestiveCrimson,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ArcadeGold),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("vip_badge")
                    ) {
                        Text(
                            text = "VIP ${account.vipLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArcadeGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x887F1D1D))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = account.username,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "ID: ${account.playerUid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArcadeGoldSoft,
                        fontSize = 10.sp
                    )
                }
            }

            // Center: Coin Balance Pill + Glossy Green BUY COIN Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xCC581C87),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, ArcadeGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = ArcadeGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${account.coins}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("coin_balance_text")
                        )
                    }
                }

                Surface(
                    onClick = onBuyCoin,
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, ArcadeGold),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("buy_coin_button")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF4ADE80), EmeraldJackpot, EmeraldJackpotDark))
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BUY COIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Right: Bind (+Rs10), FreeCash, Turntable, Gift, Check-in, Support, Settings Gear
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TopRibbonIconAction(
                    label = "Bind",
                    icon = Icons.Default.Link,
                    iconBg = Color(0xFF2563EB),
                    topBadge = if (!account.isBound) "+Rs10" else null,
                    testTag = "top_action_bind",
                    onClick = onOpenBind
                )
                TopRibbonIconAction(
                    label = "FreeCash",
                    icon = Icons.Default.AttachMoney,
                    iconBg = EmeraldJackpot,
                    testTag = "top_action_free_cash",
                    onClick = onClaimFreeCash
                )
                TopRibbonIconAction(
                    label = "Turntable",
                    icon = Icons.Default.TrackChanges,
                    iconBg = Color(0xFFF59E0B),
                    topBadge = if (account.bonusTickets > 0) "${account.bonusTickets}" else null,
                    testTag = "top_action_turntable",
                    onClick = onOpenSpinWheel
                )
                TopRibbonIconAction(
                    label = "Gift",
                    icon = Icons.Default.CardGiftcard,
                    iconBg = FestiveCrimson,
                    testTag = "top_action_gift",
                    onClick = onOpenGift
                )
                TopRibbonIconAction(
                    label = "Check-in",
                    icon = Icons.Default.CalendarMonth,
                    iconBg = SunsetOrange,
                    testTag = "top_action_checkin",
                    onClick = onOpenCheckIn
                )
                TopRibbonIconAction(
                    label = "Support",
                    icon = Icons.Default.HeadsetMic,
                    iconBg = Color(0xFF22C55E),
                    testTag = "top_action_support",
                    onClick = onOpenSupport
                )

                // Settings Gear (Pink/Gold circular button on far right)
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = Color(0xFFE11D48),
                    border = BorderStroke(1.5.dp, ArcadeGold),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_action_settings")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Admin",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopRibbonIconAction(
    label: String,
    icon: ImageVector,
    iconBg: Color,
    topBadge: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                border = BorderStroke(1.2.dp, ArcadeGold),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (topBadge != null) {
                Surface(
                    color = FestiveCrimson,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, ArcadeGold)
                ) {
                    Text(
                        text = topBadge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private enum class LobbyCategory(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HOT("Hot", Icons.Default.LocalFireDepartment),
    TABLE("Table", Icons.Default.Groups),
    BONUS("777", Icons.Default.Casino)
}

@Composable
private fun FeaturedDiceBattleStage(
    account: AccountEntity,
    onOpenDiceGame: () -> Unit,
    onOpenSpinWheel: () -> Unit,
    onOpenCheckIn: () -> Unit,
    onClaimFreeCash: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(LobbyCategory.HOME) }
    val freeCashFraction = (account.freeCashProgress / 1333f).coerceIn(0f, 1f)
    val canClaimFreeCash = account.freeCashProgress >= 1333L

    val subtitleForTab = when (selectedCategory) {
        LobbyCategory.HOME -> "2-6 • 7 • 8-12 • 2X Lightning • Min 10 Coins (Watch Mode)"
        LobbyCategory.HOT -> "Trending Table • Twin Dice Dome • Up to 6X Instant Payouts"
        LobbyCategory.TABLE -> "31 Players Live • Richie Rich & Millionaire Seats • Watch Mode"
        LobbyCategory.BONUS -> "Play Dice Table or Spin Turntable for Free Cash 1,333!"
    }

    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 860.dp)
            .fillMaxWidth()
            .testTag("dice_lobby_stage")
    ) {
        val isWide = maxWidth >= 480.dp
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Vertical Frosted Maroon Category Rail (matching screenshot)
                LobbyCategoryRail(
                    selected = selectedCategory,
                    compact = false,
                    onSelect = { selectedCategory = it }
                )

                // Open Horizontal Carnival Tile Shelf over the sunset backdrop
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tile 1: 7up7down / Dice Battle Table (Snow-capped Golden Tile + Maroon Jackpot Pill)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(235.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomCenter) {
                            DiceGameLogoTile(
                                tileSize = 136.dp,
                                onClick = onOpenDiceGame,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            JackpotOverlapPill(
                                topLabel = "JACKPOT",
                                bottomLabel = "2X - 6X LIVE"
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = Color(0x9938114D),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Dice Battle Table",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ArcadeGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = subtitleForTab,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Button(
                                    onClick = onOpenDiceGame,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldJackpot,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("open_dice_game_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Enter Dice Table", fontWeight = FontWeight.ExtraBold, maxLines = 1)
                                }
                            }
                        }
                    }

                    // Tile 2: Lucky Spin & Free Cash Treasure Tile (matching screenshot's multi-tile shelf)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(235.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomCenter) {
                            CarnivalBonusTile(
                                tileSize = 136.dp,
                                onClick = onOpenSpinWheel,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            JackpotOverlapPill(
                                topLabel = "FREE CASH",
                                bottomLabel = "${account.freeCashProgress} / 1,333"
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = Color(0x9938114D),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { freeCashFraction },
                                    color = EmeraldJackpot,
                                    trackColor = RoyalPlumDark,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = onOpenSpinWheel,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ArcadeGold,
                                            contentColor = TextOnGold
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("empty_lobby_spin_button")
                                    ) {
                                        Text("Spin Wheel", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = if (canClaimFreeCash) onClaimFreeCash else onOpenCheckIn,
                                        border = BorderStroke(1.dp, WinterFrostCyan),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("empty_lobby_checkin_button")
                                    ) {
                                        Text(
                                            text = if (canClaimFreeCash) "Claim" else "Check-In",
                                            color = WinterFrostCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Compact portrait layout with left vertical category rail + open 7up7down tile card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LobbyCategoryRail(
                    selected = selectedCategory,
                    compact = true,
                    onSelect = { selectedCategory = it }
                )

                Surface(
                    color = Color(0x8C38114D),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, ArcadeGold.copy(alpha = 0.8f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                DiceGameLogoTile(
                                    tileSize = 90.dp,
                                    onClick = onOpenDiceGame,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Surface(
                                    color = Color(0xFF7F1D1D),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, ArcadeGold)
                                ) {
                                    Text(
                                        text = "2X - 6X",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Dice Battle Table",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ArcadeGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = subtitleForTab,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenDiceGame,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldJackpot,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("open_dice_game_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("Enter Dice Table", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
                            }
                            Button(
                                onClick = onOpenSpinWheel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArcadeGold,
                                    contentColor = TextOnGold
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("empty_lobby_spin_button")
                            ) {
                                Text("Spin Wheel", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyCategoryRail(
    selected: LobbyCategory,
    compact: Boolean,
    onSelect: (LobbyCategory) -> Unit
) {
    Surface(
        color = Color(0xA6581C3C),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.25f)),
        shadowElevation = 6.dp,
        modifier = Modifier.testTag("lobby_category_rail")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 5.dp else 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LobbyCategory.entries.forEach { category ->
                val isSelected = category == selected
                Surface(
                    onClick = { onSelect(category) },
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = if (isSelected) BorderStroke(1.5.dp, Color(0xFFFEF08A)) else null,
                    modifier = Modifier
                        .size(if (compact) 38.dp else 48.dp)
                        .testTag("lobby_tab_${category.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(listOf(Color(0xFFFEF08A), ArcadeGold, Color(0xFFD97706)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0x443B0764), Color(0x443B0764)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            tint = if (isSelected) Color.White else Color(0xFFE9D5FF),
                            modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JackpotOverlapPill(
    topLabel: String,
    bottomLabel: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFF701A3C),
        border = BorderStroke(1.5.dp, Color(0xFFFDE047)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = topLabel,
                color = Color(0xFFFDE047),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 10.sp
            )
            Text(
                text = bottomLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun DiceGameLogoTile(
    tileSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF1A0B2E),
        border = BorderStroke(
            width = 3.dp,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFEF08A), ArcadeGold, Color(0xFFB45309))
            )
        ),
        shadowElevation = 10.dp,
        modifier = modifier
            .size(tileSize)
            .testTag("dice_game_logo_tile")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_dice_game_logo),
                contentDescription = "7up7down Dice Battle Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Winter snow cap highlight along the top rim matching the reference dashboard tiles
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp)
                    .width(34.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
private fun CarnivalBonusTile(
    tileSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF2E1065),
        border = BorderStroke(
            width = 3.dp,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFEF08A), ArcadeGold, Color(0xFFB45309))
            )
        ),
        shadowElevation = 10.dp,
        modifier = modifier.size(tileSize)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_jackpot_treasure),
                contentDescription = "Lucky Spin & Free Cash Bonus",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC2E1065))
                        )
                    )
            )
            Surface(
                color = SunsetOrange,
                shape = RoundedCornerShape(bottomEnd = 10.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "Bonus",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 18.dp)
                    .width(32.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
            )
            Text(
                text = "TURNTABLE\nSPIN",
                color = Color(0xFFFEF08A),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

private data class CarnivalDockItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
private fun CarnivalBottomDock(
    unreadMailCount: Int,
    freeSpinPromoEnabled: Boolean,
    onFreeBonus: () -> Unit,
    onWithdraw: () -> Unit,
    onVip: () -> Unit,
    onBank: () -> Unit,
    onRanking: () -> Unit,
    onEmail: () -> Unit,
    onEvent: () -> Unit,
    onPromote: () -> Unit,
    onDeposit: () -> Unit
) {
    val dockItems = listOf(
        CarnivalDockItem("Withdraw", Icons.Default.AccountBalanceWallet, 0, "nav_withdraw", onWithdraw),
        CarnivalDockItem("Vip", Icons.Default.Diamond, 0, "nav_vip", onVip),
        CarnivalDockItem("Bank", Icons.Default.AccountBalance, 0, "nav_bank", onBank),
        CarnivalDockItem("Ranking", Icons.Default.EmojiEvents, 0, "nav_ranking", onRanking),
        CarnivalDockItem("E-Mail", Icons.Default.Mail, unreadMailCount, "nav_email", onEmail),
        CarnivalDockItem("Event", Icons.Default.Celebration, 0, "nav_event", onEvent),
        CarnivalDockItem("Promote", Icons.Default.Share, 0, "nav_promote", onPromote)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC581C87)))
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left: FREE BONUS chest button (like screenshot)
            if (freeSpinPromoEnabled) {
                Surface(
                    onClick = onFreeBonus,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFB91C1C),
                    border = BorderStroke(1.5.dp, ArcadeGold),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("hero_spin_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Free Bonus Spin",
                            tint = ArcadeGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "FREE BONUS",
                            color = ArcadeGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Center: Emerald Green Dock Bar with snow border
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.weight(1f)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp)
                ) {
                    val spreadEvenly = maxWidth >= 430.dp
                    Row(
                        modifier = if (spreadEvenly) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        },
                        horizontalArrangement = if (spreadEvenly) Arrangement.SpaceEvenly else Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dockItems.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(onClick = item.onClick)
                                    .minimumInteractiveComponentSize()
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag(item.testTag),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (item.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = FestiveCrimson) {
                                                Text("${item.badgeCount}", color = Color.White, fontSize = 8.sp)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = Color(0xFFFEF08A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = Color(0xFFFEF08A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = item.label,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Right: Highlighted DEPOSIT Shop Button (like screenshot)
            Surface(
                onClick = onDeposit,
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, ArcadeGold),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("nav_deposit_highlighted")
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(listOf(ArcadeGold, SunsetOrange, FestiveCrimson))
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Deposit",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "DEPOSIT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LobbySettingsDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenSupport: () -> Unit,
    onLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalPlumSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_luck780_logo),
                    contentDescription = "Luck780 Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(34.dp)
                )
                Text("Account & System Menu", color = ArcadeGold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Signed in as ${account.username} (${account.countryCode} ${account.phoneNumber})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDarkPrimary
                )
                Button(
                    onClick = onOpenAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                    modifier = Modifier.fillMaxWidth().testTag("top_action_admin")
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Admin Panel", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenSupport,
                    border = BorderStroke(1.dp, WinterFrostCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.HeadsetMic, null, tint = WinterFrostCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("24/7 AI VIP Support", color = WinterFrostCyan)
                }
                OutlinedButton(
                    onClick = onLogout,
                    border = BorderStroke(1.dp, FestiveCrimson),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out", color = FestiveCrimson)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextOnDarkPrimary)
            }
        }
    )
}
