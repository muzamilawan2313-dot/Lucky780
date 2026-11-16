package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountEntity
import com.example.data.local.MailMessageEntity
import com.example.data.local.ReferralRecordEntity
import com.example.model.VIP_TIERS
import com.example.referral.ReferralLinkHelper
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.EmeraldJackpot
import com.example.ui.theme.FestiveCrimson
import com.example.ui.theme.RoyalPlumBorder
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.RoyalPlumDark
import com.example.ui.theme.RoyalPlumSurface
import com.example.ui.theme.TextOnDarkPrimary
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.WinterFrostCyan
import com.example.viewmodel.AiSupportUiState

@Composable
fun CheckInDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onClaimToday: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, ArcadeGold)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = ArcadeGold)
                        Text("7-Day Check-In", style = MaterialTheme.typography.titleLarge, color = ArcadeGold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TextOnDarkPrimary)
                    }
                }

                Text(
                    text = "Check in daily to collect progressive Coins and bonus Spin Tickets!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDarkSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).chunked(4).forEach { rowDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowDays.forEach { day ->
                                val claimed = day <= account.lastCheckInDay
                                Surface(
                                    color = if (claimed) EmeraldJackpot.copy(alpha = 0.25f) else RoyalPlumCard,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (claimed) EmeraldJackpot else RoyalPlumBorder
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Day $day", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                        Text(
                                            text = "+${day * 25}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (claimed) EmeraldJackpot else ArcadeGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onClaimToday,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Claim Today's Check-In", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GiftRedeemDialog(
    onDismiss: () -> Unit,
    onRedeemCode: (String) -> Unit
) {
    var code by remember { mutableStateOf("LUCK780") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, FestiveCrimson)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, null, tint = FestiveCrimson)
                        Text("Redeem Gift Box", style = MaterialTheme.typography.titleLarge, color = ArcadeGold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TextOnDarkPrimary)
                    }
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Enter Promo Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LUCK780", "WINTER780", "VIPBONUS").forEach { preset ->
                        OutlinedButton(
                            onClick = { code = preset },
                            border = BorderStroke(1.dp, WinterFrostCyan.copy(alpha = 0.6f))
                        ) {
                            Text(preset, color = WinterFrostCyan, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Button(
                    onClick = { onRedeemCode(code) },
                    colors = ButtonDefaults.buttonColors(containerColor = FestiveCrimson, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Redeem Coins & Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VipTiersDialog(
    account: AccountEntity,
    onDismiss: () -> Unit
) {
    val nextTier = VIP_TIERS.firstOrNull { it.level == account.vipLevel + 1 }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, ArcadeGold)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, null, tint = ArcadeGold)
                        Text("VIP Club (Level ${account.vipLevel})", style = MaterialTheme.typography.titleLarge, color = ArcadeGold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TextOnDarkPrimary)
                    }
                }

                if (nextTier != null) {
                    Text(
                        text = "${account.vipPoints} / ${nextTier.minPoints} pts to ${nextTier.name}",
                        style = MaterialTheme.typography.labelLarge,
                        color = WinterFrostCyan
                    )
                    LinearProgressIndicator(
                        progress = { (account.vipPoints.toFloat() / nextTier.minPoints).coerceIn(0f, 1f) },
                        color = ArcadeGold,
                        trackColor = RoyalPlumDark,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VIP_TIERS) { tier ->
                        val unlocked = account.vipLevel >= tier.level
                        Surface(
                            color = if (unlocked) Color(0xFF391D66) else RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (unlocked) ArcadeGold else RoyalPlumBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("VIP ${tier.level} • ${tier.name}", style = MaterialTheme.typography.titleMedium, color = ArcadeGold)
                                    Text("Daily +${tier.dailyBonusCoins}", style = MaterialTheme.typography.labelLarge, color = EmeraldJackpot)
                                }
                                Text(tier.perkSummary, style = MaterialTheme.typography.bodyMedium, color = TextOnDarkSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailInboxDialog(
    messages: List<MailMessageEntity>,
    onDismiss: () -> Unit,
    onClaimMail: (MailMessageEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, WinterFrostCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mail, null, tint = WinterFrostCyan)
                        Text("Player Inbox", style = MaterialTheme.typography.titleLarge, color = TextOnDarkPrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TextOnDarkPrimary)
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(messages, key = { it.id }) { mail ->
                        Surface(
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (!mail.isRead) ArcadeGold else RoyalPlumBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(mail.subject, style = MaterialTheme.typography.titleMedium, color = ArcadeGold)
                                Text(mail.body, style = MaterialTheme.typography.bodyMedium, color = TextOnDarkSecondary)
                                Button(
                                    onClick = { onClaimMail(mail) },
                                    enabled = !mail.isClaimed,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldJackpot,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(if (mail.isClaimed) "Claimed" else "Claim +${mail.rewardCoins} Coins")
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
fun EventAndPromoteDialog(
    isPromote: Boolean,
    account: AccountEntity,
    referrals: List<ReferralRecordEntity> = emptyList(),
    onApplyReferralCode: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onOpenSpinWheel: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedLabel by remember { mutableStateOf<String?>(null) }
    var friendCodeInput by remember { mutableStateOf("") }

    val installUrl = remember(account.referralCode) {
        ReferralLinkHelper.buildInstallReferenceUrl(account.referralCode)
    }
    val appDeepLink = remember(account.referralCode) {
        ReferralLinkHelper.buildAppDeepLink(account.referralCode)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, ArcadeGold)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPromote) "Reference & Install System" else "Winter Carnival Events",
                        style = MaterialTheme.typography.titleLarge,
                        color = ArcadeGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = TextOnDarkPrimary)
                    }
                }

                if (isPromote) {
                    Text(
                        text = "Koi bhi dost aap ke Reference Link ke zariye app install aur register karega to aapko +150 Coins & +2 Spin Tickets milenge, aur dost ko +100 Extra Coins!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDarkSecondary
                    )

                    // Live Commission Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, WinterFrostCyan.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Installed Friends", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                Text(
                                    text = "${account.referralCount}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WinterFrostCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Surface(
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Coins Earned", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                Text(
                                    text = "+${account.referralEarnedCoins}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ArcadeGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Reference Code & Copy Install Link Card
                    Surface(
                        color = RoyalPlumDark,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WinterFrostCyan)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Your Unique Reference Code", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                    Text(
                                        text = account.referralCode,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ArcadeGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.testTag("referral_code_text")
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(account.referralCode))
                                        copiedLabel = "Reference code copied!"
                                    },
                                    border = BorderStroke(1.dp, WinterFrostCyan),
                                    modifier = Modifier.testTag("copy_referral_code_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = WinterFrostCyan, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Copy Code", color = WinterFrostCyan, fontSize = 12.sp)
                                }
                            }

                            Surface(
                                color = RoyalPlumCard,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RoyalPlumBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Link, null, tint = ArcadeGold, modifier = Modifier.size(18.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Install Reference Link", style = MaterialTheme.typography.labelSmall, color = ArcadeGold)
                                        Text(
                                            text = installUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnDarkPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("referral_install_link_text")
                                        )
                                        Text(
                                            text = "App Deep Link: $appDeepLink",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WinterFrostCyan,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(installUrl))
                                        copiedLabel = "Install reference link copied!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("copy_referral_link_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Copy Link", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Button(
                                    onClick = {
                                        ReferralLinkHelper.shareInstallLink(context, account.username, account.referralCode)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldJackpot, contentColor = Color.White),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("share_referral_link_button")
                                ) {
                                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Share Install", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    if (copiedLabel != null) {
                        Text(copiedLabel!!, style = MaterialTheme.typography.labelLarge, color = EmeraldJackpot)
                    }

                    // Apply Friend's Reference Code or Link (once per account)
                    if (account.referredByCode.isNullOrBlank()) {
                        Surface(
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, RoyalPlumBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Link a Friend's Reference Code (+100 Coins Bonus)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArcadeGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = friendCodeInput,
                                        onValueChange = { friendCodeInput = it },
                                        placeholder = { Text("Paste link or code (e.g. LUCK780VIP)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("claim_referral_input")
                                    )
                                    Button(
                                        onClick = {
                                            if (friendCodeInput.isNotBlank()) {
                                                onApplyReferralCode(friendCodeInput)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldJackpot, contentColor = Color.White),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("claim_referral_button")
                                    ) {
                                        Text("Apply", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = EmeraldJackpot.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EmeraldJackpot)
                        ) {
                            Text(
                                text = "Joined via Reference Code: ${account.referredByCode} (+100 Bonus Credited)",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldJackpot,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Invited Friends History List
                    Text(
                        text = "Invited Installs (${referrals.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = WinterFrostCyan,
                        fontWeight = FontWeight.Bold
                    )
                    if (referrals.isEmpty()) {
                        Text(
                            text = "No friends installed with your link yet. Share your Reference Link to start earning +150 Coins per install!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnDarkSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            referrals.take(8).forEach { rec ->
                                Surface(
                                    color = RoyalPlumCard,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, RoyalPlumBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rec.refereeName, style = MaterialTheme.typography.labelLarge, color = TextOnDarkPrimary)
                                            Text("Installed via ${rec.referralCodeUsed}", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                        }
                                        Text(
                                            text = "+${rec.referrerBonusCoins} Coins • +${rec.referrerBonusTickets} Tickets",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldJackpot,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "• GET 1,333 FOR FREE Spin Milestone\n• +Rs10 to +Rs900 Deposit Bonus Boost\n• +150 Coins per Friend Install via Reference Link\n• 7-Day Check-In Progressive Streak",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnDarkPrimary
                    )
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenSpinWheel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Go to Lucky Spin Wheel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportAiDialog(
    uiState: AiSupportUiState,
    onAskQuestion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val quickPrompts = listOf(
        "Explain Dragon Tiger vs 7up7down odds & best bet strategy",
        "How do Spin Wheel x2/x5 multipliers & 1,333 Free Cash work?",
        "How do I bind JazzCash/Easypaisa and withdraw safely?"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(26.dp),
            color = RoyalPlumSurface,
            border = BorderStroke(1.5.dp, WinterFrostCyan)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Psychology, null, tint = WinterFrostCyan)
                        Column {
                            Text("Luck780 AI VIP Advisor", style = MaterialTheme.typography.titleLarge, color = ArcadeGold)
                            Text("Deep Thinking Mode (High Reasoning)", style = MaterialTheme.typography.labelSmall, color = WinterFrostCyan)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close Support", tint = TextOnDarkPrimary)
                    }
                }

                // Quick-Ask Strategy Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickPrompts.forEach { suggestion ->
                        Surface(
                            onClick = {
                                input = suggestion
                                onAskQuestion(suggestion)
                            },
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoyalPlumBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = ArcadeGold, modifier = Modifier.size(15.dp))
                                Text(suggestion, style = MaterialTheme.typography.bodyMedium, color = TextOnDarkPrimary)
                            }
                        }
                    }
                }

                // Output Area
                Surface(
                    color = RoyalPlumDark,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RoyalPlumBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 140.dp, max = 260.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (uiState) {
                            AiSupportUiState.Idle -> {
                                Text(
                                    text = "Ask any complex question about Luck780 game probabilities, VIP tiers, Spin Wheel multipliers, or wallet payouts.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextOnDarkSecondary
                                )
                            }
                            AiSupportUiState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WinterFrostCyan, strokeWidth = 2.dp)
                                    Text(
                                        text = "Thinking deeply with gemini-4-carbon (High)...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WinterFrostCyan
                                    )
                                }
                            }
                            is AiSupportUiState.Streaming -> {
                                Text(uiState.text, style = MaterialTheme.typography.bodyMedium, color = TextOnDarkPrimary)
                            }
                            is AiSupportUiState.Success -> {
                                Text(uiState.text, style = MaterialTheme.typography.bodyMedium, color = TextOnDarkPrimary)
                            }
                            is AiSupportUiState.Error -> {
                                Text(
                                    text = "Support Notice: ${uiState.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FestiveCrimson
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ask VIP Support or Game Advisor...") },
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_support_input")
                    )
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                onAskQuestion(input)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ArcadeGold, RoundedCornerShape(12.dp))
                            .testTag("ai_support_send")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = TextOnGold)
                    }
                }
            }
        }
    }
}
