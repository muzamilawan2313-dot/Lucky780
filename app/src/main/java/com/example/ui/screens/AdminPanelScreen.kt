package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccountEntity
import com.example.data.local.AdminControlSettings
import com.example.data.local.TransactionEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class AdminTab(val label: String) {
    OVERVIEW("Overview"),
    USERS("Users"),
    FINANCE("Finance")
}

private enum class FinanceFilter(val label: String) {
    PENDING("Pending"),
    WITHDRAWALS("Withdrawals"),
    DEPOSITS("Deposits"),
    ALL("All")
}

@Composable
fun AdminPanelScreen(
    allAccounts: List<AccountEntity>,
    allTransactions: List<TransactionEntity>,
    adminSettings: AdminControlSettings,
    onBack: () -> Unit,
    onAdjustCoins: (phoneKey: String, delta: Long, reason: String) -> Unit,
    onGrantTickets: (phoneKey: String, tickets: Int) -> Unit,
    onSetVipLevel: (phoneKey: String, level: Int) -> Unit,
    onApproveWithdrawal: (TransactionEntity) -> Unit,
    onRejectWithdrawal: (TransactionEntity) -> Unit,
    onBroadcastMail: (subject: String, body: String, rewardCoins: Long) -> Unit,
    onUpdateMarquee: (String) -> Unit,
    onTogglePromoBanner: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    Scaffold(
        containerColor = RoyalPlumDark,
        topBar = {
            Surface(color = RoyalPlumSurface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("admin_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextOnDarkPrimary
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.img_luck780_logo),
                                contentDescription = "Luck780 Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Luck780 Admin Panel",
                                style = MaterialTheme.typography.titleLarge,
                                color = ArcadeGold
                            )
                        }

                        Surface(
                            color = FestiveCrimson.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, FestiveCrimson)
                        ) {
                            Text(
                                text = "ADMIN LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFB4C2),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = AdminTab.entries.indexOf(selectedTab),
                        containerColor = RoyalPlumSurface,
                        contentColor = ArcadeGold,
                        indicator = { positions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(
                                    positions[AdminTab.entries.indexOf(selectedTab)]
                                ),
                                color = ArcadeGold
                            )
                        }
                    ) {
                        AdminTab.entries.forEach { tab ->
                            Tab(
                                selected = tab == selectedTab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
            ) {
                when (selectedTab) {
                    AdminTab.OVERVIEW -> AdminOverviewTab(
                        allAccounts = allAccounts,
                        allTransactions = allTransactions,
                        adminSettings = adminSettings,
                        onUpdateMarquee = onUpdateMarquee,
                        onBroadcastMail = onBroadcastMail,
                        onTogglePromoBanner = onTogglePromoBanner
                    )
                    AdminTab.USERS -> AdminUsersTab(
                        allAccounts = allAccounts,
                        onAdjustCoins = onAdjustCoins,
                        onGrantTickets = onGrantTickets,
                        onSetVipLevel = onSetVipLevel
                    )
                    AdminTab.FINANCE -> AdminFinanceTab(
                        allTransactions = allTransactions,
                        onApproveWithdrawal = onApproveWithdrawal,
                        onRejectWithdrawal = onRejectWithdrawal
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    allAccounts: List<AccountEntity>,
    allTransactions: List<TransactionEntity>,
    adminSettings: AdminControlSettings,
    onUpdateMarquee: (String) -> Unit,
    onBroadcastMail: (subject: String, body: String, rewardCoins: Long) -> Unit,
    onTogglePromoBanner: (Boolean) -> Unit
) {
    val totalCoins = remember(allAccounts) { allAccounts.sumOf { it.coins } }
    val pendingWithdrawals = remember(allTransactions) {
        allTransactions.filter { it.type == "WITHDRAW" && it.status == "Processing" }
    }
    val totalDeposits = remember(allTransactions) {
        allTransactions.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
    }

    var marqueeInput by remember(adminSettings.marqueeAnnouncement) {
        mutableStateOf(adminSettings.marqueeAnnouncement)
    }
    var mailSubject by remember { mutableStateOf("Winter Bonus Drop!") }
    var mailBody by remember { mutableStateOf("Enjoy this festive coin gift from the Luck780 Admin Team.") }
    var mailReward by remember { mutableStateOf("100") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminKpiCard(
                title = "Total Players",
                value = "${allAccounts.size}",
                icon = Icons.Default.Groups,
                tint = WinterFrostCyan,
                modifier = Modifier.weight(1f)
            )
            AdminKpiCard(
                title = "Coins Circulation",
                value = "$totalCoins",
                icon = Icons.Default.MonetizationOn,
                tint = ArcadeGold,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminKpiCard(
                title = "Pending Withdrawals",
                value = "${pendingWithdrawals.size} (Rs ${pendingWithdrawals.sumOf { abs(it.amount) }})",
                icon = Icons.Default.Payments,
                tint = FestiveCrimson,
                modifier = Modifier.weight(1f)
            )
            AdminKpiCard(
                title = "Total Deposits",
                value = "Rs $totalDeposits",
                icon = Icons.Default.Tune,
                tint = EmeraldJackpot,
                modifier = Modifier.weight(1f)
            )
        }

        // Live Marquee Ticker Editor
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Campaign, null, tint = ArcadeGold)
                    Text("Live Lobby Ticker Announcement", style = MaterialTheme.typography.titleMedium, color = ArcadeGold)
                }
                OutlinedTextField(
                    value = marqueeInput,
                    onValueChange = { marqueeInput = it },
                    label = { Text("Ticker Message") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onUpdateMarquee(marqueeInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Publish Lobby Announcement", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Global Inbox Broadcast
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, WinterFrostCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Send, null, tint = WinterFrostCyan)
                    Text("Send Global E-Mail & Coin Gift", style = MaterialTheme.typography.titleMedium, color = WinterFrostCyan)
                }
                OutlinedTextField(
                    value = mailSubject,
                    onValueChange = { mailSubject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mailBody,
                    onValueChange = { mailBody = it },
                    label = { Text("Message Body") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mailReward,
                    onValueChange = { mailReward = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Attached Bonus Coins (0 for none)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onBroadcastMail(mailSubject, mailBody, mailReward.toLongOrNull() ?: 0L)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WinterFrostCyan, contentColor = RoyalPlumDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_broadcast_button")
                ) {
                    Text("Broadcast to All Players", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Promo Event Banner Toggle
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, RoyalPlumBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("GET 1,333 FOR FREE Promo Banner", style = MaterialTheme.typography.titleMedium, color = TextOnDarkPrimary)
                    Text("Show featured Spin Wheel banner in Lobby", style = MaterialTheme.typography.bodyMedium, color = TextOnDarkSecondary)
                }
                Switch(
                    checked = adminSettings.freeSpinPromoEnabled,
                    onCheckedChange = onTogglePromoBanner,
                    colors = SwitchDefaults.colors(checkedTrackColor = EmeraldJackpot)
                )
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RoyalPlumCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = TextOnDarkPrimary)
        }
    }
}

@Composable
private fun AdminUsersTab(
    allAccounts: List<AccountEntity>,
    onAdjustCoins: (phoneKey: String, delta: Long, reason: String) -> Unit,
    onGrantTickets: (phoneKey: String, tickets: Int) -> Unit,
    onSetVipLevel: (phoneKey: String, level: Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(allAccounts, search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) allAccounts
        else allAccounts.filter {
            it.username.lowercase().contains(q) ||
                it.phoneNumber.contains(q) ||
                it.playerUid.contains(q)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search by username, phone, or UID...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = ArcadeGold) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.phoneKey }) { user ->
                Surface(
                    color = RoyalPlumCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RoyalPlumBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${user.username} (ID: ${user.playerUid})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextOnDarkPrimary
                                )
                                Text(
                                    text = "${user.countryCode} ${user.phoneNumber} • Bank Bound: ${if (user.isBound) "Yes" else "No"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextOnDarkSecondary
                                )
                            }
                            Surface(color = ArcadeGold, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "VIP ${user.vipLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOnGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Coins: ${user.coins}",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArcadeGold
                            )
                            Text(
                                text = "Spin Tickets: ${user.bonusTickets}",
                                style = MaterialTheme.typography.titleMedium,
                                color = WinterFrostCyan
                            )
                        }

                        // Balance & Ticket Adjustment Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AdminActionPill(
                                label = "+100",
                                color = EmeraldJackpot,
                                modifier = Modifier.weight(1f).testTag("admin_add_100_${user.playerUid}"),
                                onClick = { onAdjustCoins(user.phoneKey, 100L, "Admin +100 Bonus") }
                            )
                            AdminActionPill(
                                label = "+500",
                                color = ArcadeGold,
                                textColor = TextOnGold,
                                modifier = Modifier.weight(1f),
                                onClick = { onAdjustCoins(user.phoneKey, 500L, "Admin +500 VIP Credit") }
                            )
                            AdminActionPill(
                                label = "-100",
                                color = FestiveCrimson,
                                modifier = Modifier.weight(1f),
                                onClick = { onAdjustCoins(user.phoneKey, -100L, "Admin -100 Deduction") }
                            )
                            AdminActionPill(
                                label = "+2 Tkt",
                                color = WinterFrostCyan,
                                textColor = RoyalPlumDark,
                                modifier = Modifier.weight(1f),
                                onClick = { onGrantTickets(user.phoneKey, 2) }
                            )
                        }

                        // VIP Upgrade / Downgrade Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VIP Tier:", style = MaterialTheme.typography.labelLarge, color = TextOnDarkSecondary)
                            (1..5).forEach { lvl ->
                                val active = user.vipLevel == lvl
                                Surface(
                                    onClick = { onSetVipLevel(user.phoneKey, lvl) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) ArcadeGold else RoyalPlumSurface,
                                    border = BorderStroke(1.dp, if (active) ArcadeGold else RoyalPlumBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "V$lvl",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) TextOnGold else TextOnDarkPrimary
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
}

@Composable
private fun AdminActionPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminFinanceTab(
    allTransactions: List<TransactionEntity>,
    onApproveWithdrawal: (TransactionEntity) -> Unit,
    onRejectWithdrawal: (TransactionEntity) -> Unit
) {
    var filter by remember { mutableStateOf(FinanceFilter.PENDING) }
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    val filtered = remember(allTransactions, filter) {
        when (filter) {
            FinanceFilter.PENDING -> allTransactions.filter { it.type == "WITHDRAW" && it.status == "Processing" }
            FinanceFilter.WITHDRAWALS -> allTransactions.filter { it.type == "WITHDRAW" }
            FinanceFilter.DEPOSITS -> allTransactions.filter { it.type == "DEPOSIT" }
            FinanceFilter.ALL -> allTransactions
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinanceFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.label, fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArcadeGold,
                        selectedLabelColor = TextOnGold
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No transactions matching '${filter.label}'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextOnDarkSecondary
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    val isPendingWithdrawal = tx.type == "WITHDRAW" && tx.status == "Processing"
                    Surface(
                        color = RoyalPlumCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isPendingWithdrawal) SunsetOrange else RoyalPlumBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.titleMedium, color = TextOnDarkPrimary)
                                    Text(
                                        text = "${tx.ownerPhoneKey} • ${tx.method} • ${formatter.format(Date(tx.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOnDarkSecondary
                                    )
                                }
                                Text(
                                    text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (tx.amount >= 0) EmeraldJackpot else FestiveCrimson
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when (tx.status) {
                                        "Completed" -> EmeraldJackpot.copy(alpha = 0.2f)
                                        "Processing" -> SunsetOrange.copy(alpha = 0.2f)
                                        else -> FestiveCrimson.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = tx.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (tx.status) {
                                            "Completed" -> EmeraldJackpot
                                            "Processing" -> SunsetOrange
                                            else -> FestiveCrimson
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (isPendingWithdrawal) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { onRejectWithdrawal(tx) },
                                            border = BorderStroke(1.dp, FestiveCrimson),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = FestiveCrimson, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("Refund", color = FestiveCrimson, fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = { onApproveWithdrawal(tx) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldJackpot, contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("admin_approve_tx_${tx.id}")
                                        ) {
                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(4.dp))
                                            Text("Approve", fontSize = 12.sp)
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
}
