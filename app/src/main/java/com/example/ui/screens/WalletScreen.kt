package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.data.local.BankAccountEntity
import com.example.data.local.TransactionEntity
import com.example.model.DEPOSIT_PACKAGES
import com.example.model.DepositPackage
import com.example.model.LeaderboardPlayer
import com.example.model.SAMPLE_TOP_WINNERS
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeGoldDeep
import com.example.ui.theme.EmeraldJackpot
import com.example.ui.theme.EmeraldJackpotDark
import com.example.ui.theme.FestiveCrimson
import com.example.ui.theme.RoyalPlumBorder
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.RoyalPlumDark
import com.example.ui.theme.RoyalPlumSurface
import com.example.ui.theme.TextOnDarkPrimary
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.WinterFrostCyan
import com.example.viewmodel.WalletTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    account: AccountEntity,
    selectedTab: WalletTab,
    bankAccounts: List<BankAccountEntity>,
    transactions: List<TransactionEntity>,
    onSelectTab: (WalletTab) -> Unit,
    onBack: () -> Unit,
    onDeposit: (amountRs: Long, bonusRs: Long, method: String) -> Unit,
    onWithdraw: (amountRs: Long, bankAccount: BankAccountEntity) -> Unit,
    onBindBank: (provider: String, title: String, number: String, bankName: String) -> Unit,
    onSetPrimaryBank: (Long) -> Unit,
    onDeleteBank: (Long) -> Unit
) {
    Scaffold(
        containerColor = RoyalPlumDark,
        topBar = {
            Surface(color = RoyalPlumSurface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("wallet_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Lobby",
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
                                text = "Luck780 Wallet & Bank",
                                style = MaterialTheme.typography.titleLarge,
                                color = ArcadeGold
                            )
                        }

                        Surface(
                            color = RoyalPlumDark,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = ArcadeGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${account.coins}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ArcadeGold
                                )
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = WalletTab.entries.indexOf(selectedTab),
                        containerColor = RoyalPlumSurface,
                        contentColor = ArcadeGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(
                                    tabPositions[WalletTab.entries.indexOf(selectedTab)]
                                ),
                                color = ArcadeGold
                            )
                        }
                    ) {
                        WalletTab.entries.forEach { tab ->
                            Tab(
                                selected = tab == selectedTab,
                                onClick = { onSelectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.title,
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
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
            ) {
                when (selectedTab) {
                    WalletTab.DEPOSIT -> DepositTabContent(onDeposit = onDeposit)
                    WalletTab.WITHDRAW -> WithdrawTabContent(
                        account = account,
                        bankAccounts = bankAccounts,
                        onWithdraw = onWithdraw,
                        onNavigateToBind = { onSelectTab(WalletTab.BANK) }
                    )
                    WalletTab.BANK -> BankManagementTabContent(
                        account = account,
                        bankAccounts = bankAccounts,
                        onBindBank = onBindBank,
                        onSetPrimary = onSetPrimaryBank,
                        onDeleteBank = onDeleteBank
                    )
                    WalletTab.RANKING_HISTORY -> RankingsAndHistoryTabContent(
                        account = account,
                        transactions = transactions
                    )
                }
            }
        }
    }
}

@Composable
private fun DepositTabContent(
    onDeposit: (amountRs: Long, bonusRs: Long, method: String) -> Unit
) {
    val paymentMethods = listOf("JazzCash", "Easypaisa", "Bank Transfer", "SadaPay")
    var selectedMethod by remember { mutableStateOf(paymentMethods.first()) }
    var selectedPackage by remember { mutableStateOf<DepositPackage>(DEPOSIT_PACKAGES.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Payment Channel",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextOnDarkPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.take(2).forEach { method ->
                        MethodChip(
                            label = method,
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.drop(2).forEach { method ->
                        MethodChip(
                            label = method,
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Text(
            text = "Choose Coin Top-Up Bundle (Extra Bonus Included)",
            style = MaterialTheme.typography.titleMedium,
            color = ArcadeGold
        )

        DEPOSIT_PACKAGES.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { pkg ->
                    val isSelected = selectedPackage.id == pkg.id
                    Surface(
                        onClick = { selectedPackage = pkg },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF391D66) else RoyalPlumCard,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ArcadeGold else RoyalPlumBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("deposit_pkg_${pkg.amountRs}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            pkg.badge?.let { badge ->
                                Surface(
                                    color = EmeraldJackpot,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Rs ${pkg.amountRs}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextOnDarkPrimary
                            )
                            Text(
                                text = "+Rs ${pkg.bonusRs} Extra Bonus",
                                style = MaterialTheme.typography.labelLarge,
                                color = ArcadeGold
                            )
                            Text(
                                text = "Total: ${pkg.coinsCredited} Coins",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WinterFrostCyan
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                onDeposit(selectedPackage.amountRs, selectedPackage.bonusRs, selectedMethod)
            },
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_deposit_button")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(EmeraldJackpot, EmeraldJackpotDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEPOSIT Rs ${selectedPackage.amountRs} VIA $selectedMethod",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MethodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ArcadeGold else RoyalPlumSurface,
        border = BorderStroke(1.dp, if (selected) ArcadeGold else RoyalPlumBorder),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) TextOnGold else TextOnDarkPrimary
            )
        }
    }
}

@Composable
private fun WithdrawTabContent(
    account: AccountEntity,
    bankAccounts: List<BankAccountEntity>,
    onWithdraw: (amountRs: Long, bankAccount: BankAccountEntity) -> Unit,
    onNavigateToBind: () -> Unit
) {
    var amountInput by remember { mutableStateOf("500") }
    var selectedAccount by remember(bankAccounts) {
        mutableStateOf(bankAccounts.firstOrNull { it.isPrimary } ?: bankAccounts.firstOrNull())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, WinterFrostCyan.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Withdrawable Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDarkSecondary
                    )
                    Text(
                        text = "Rs ${account.coins}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ArcadeGold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Protected Payout",
                    tint = EmeraldJackpot,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        if (bankAccounts.isEmpty()) {
            Surface(
                color = FestiveCrimson.copy(alpha = 0.16f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, FestiveCrimson)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No Payout Account Bound",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnDarkPrimary
                    )
                    Text(
                        text = "Bind a JazzCash, Easypaisa, or Bank account first to receive secure cash withdrawals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDarkSecondary
                    )
                    Button(
                        onClick = onNavigateToBind,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold)
                    ) {
                        Text("Bind Account Now")
                    }
                }
            }
        } else {
            Text(
                text = "Select Receiving Account",
                style = MaterialTheme.typography.titleMedium,
                color = TextOnDarkPrimary
            )
            bankAccounts.forEach { bank ->
                val isSelected = selectedAccount?.id == bank.id
                Surface(
                    onClick = { selectedAccount = bank },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF391D66) else RoyalPlumCard,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) ArcadeGold else RoyalPlumBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ArcadeGold
                            )
                            Column {
                                Text(
                                    text = "${bank.providerType} • ${bank.accountTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextOnDarkPrimary
                                )
                                Text(
                                    text = "${bank.bankName} (${bank.accountNumber})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextOnDarkSecondary
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = EmeraldJackpot
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Withdrawal Amount (Min Rs 100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcadeGold,
                    unfocusedBorderColor = RoyalPlumBorder,
                    focusedTextColor = TextOnDarkPrimary,
                    unfocusedTextColor = TextOnDarkPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_amount_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("100", "500", "1000", "${account.coins}").forEachIndexed { idx, preset ->
                    OutlinedButton(
                        onClick = { amountInput = preset },
                        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (idx == 3) "Max" else "Rs $preset",
                            color = ArcadeGold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val amt = amountInput.toLongOrNull() ?: 0L
                    selectedAccount?.let { onWithdraw(amt, it) }
                },
                enabled = selectedAccount != null && (amountInput.toLongOrNull() ?: 0L) >= 100L,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_withdraw_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(ArcadeGold, ArcadeGoldDeep, FestiveCrimson))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "REQUEST SECURE WITHDRAWAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BankManagementTabContent(
    account: AccountEntity,
    bankAccounts: List<BankAccountEntity>,
    onBindBank: (provider: String, title: String, number: String, bankName: String) -> Unit,
    onSetPrimary: (Long) -> Unit,
    onDeleteBank: (Long) -> Unit
) {
    val providers = listOf("JazzCash", "Easypaisa", "Bank Transfer", "SadaPay")
    var selectedProvider by remember { mutableStateOf(providers.first()) }
    var accountTitle by remember { mutableStateOf(account.username) }
    var accountNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = RoyalPlumCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, EmeraldJackpot.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bound Phone Identity", style = MaterialTheme.typography.labelLarge, color = WinterFrostCyan)
                    Text(
                        text = "${account.countryCode} ${account.phoneNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnDarkPrimary
                    )
                }
                Surface(color = EmeraldJackpot.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                    Text(
                        text = "Verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldJackpot,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Text(
            text = "Bind Bank or Digital Wallet",
            style = MaterialTheme.typography.titleMedium,
            color = ArcadeGold
        )

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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    providers.forEach { provider ->
                        val active = provider == selectedProvider
                        Surface(
                            onClick = { selectedProvider = provider },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) ArcadeGold else RoyalPlumSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.replace(" Transfer", ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) TextOnGold else TextOnDarkPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = accountTitle,
                    onValueChange = { accountTitle = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Wallet Mobile / IBAN Number") },
                    placeholder = { Text("03001234567 or PK...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bind_account_number_input")
                )
                if (selectedProvider == "Bank Transfer") {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (e.g. HBL, Meezan)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        onBindBank(selectedProvider, accountTitle, accountNumber, bankName)
                        accountNumber = ""
                        bankName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bind_bank_button")
                ) {
                    Text("Bind $selectedProvider Account", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (bankAccounts.isNotEmpty()) {
            Text(
                text = "Linked Payout Accounts (${bankAccounts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextOnDarkPrimary
            )
            bankAccounts.forEach { bank ->
                Surface(
                    color = RoyalPlumCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (bank.isPrimary) ArcadeGold else RoyalPlumBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = ArcadeGold
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = bank.providerType,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextOnDarkPrimary
                                    )
                                    if (bank.isPrimary) {
                                        Surface(
                                            color = EmeraldJackpot.copy(alpha = 0.22f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "PRIMARY",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldJackpot,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${bank.accountTitle} • ${bank.accountNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextOnDarkSecondary
                                )
                            }
                        }

                        Row {
                            if (!bank.isPrimary) {
                                TextButton(onClick = { onSetPrimary(bank.id) }) {
                                    Text("Set Primary", color = WinterFrostCyan, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { onDeleteBank(bank.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove Account",
                                    tint = FestiveCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingsAndHistoryTabContent(
    account: AccountEntity,
    transactions: List<TransactionEntity>
) {
    var showRankings by remember { mutableStateOf(true) }
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    val combinedLeaderboard = remember(account) {
        val current = LeaderboardPlayer(
            rank = 0,
            playerName = account.username,
            playerId = account.playerUid,
            vipLevel = account.vipLevel,
            totalWinnings = account.coins + (account.vipPoints * 12),
            isCurrentUser = true
        )
        (SAMPLE_TOP_WINNERS + current)
            .sortedByDescending { it.totalWinnings }
            .mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = showRankings,
                onClick = { showRankings = true },
                leadingIcon = { Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(16.dp)) },
                label = { Text("Winner Rankings") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ArcadeGold,
                    selectedLabelColor = TextOnGold
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !showRankings,
                onClick = { showRankings = false },
                leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp)) },
                label = { Text("Transactions (${transactions.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WinterFrostCyan,
                    selectedLabelColor = RoyalPlumDark
                ),
                modifier = Modifier.weight(1f)
            )
        }

        if (showRankings) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(combinedLeaderboard) { player ->
                    Surface(
                        color = if (player.isCurrentUser) Color(0xFF391D66) else RoyalPlumCard,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (player.isCurrentUser) ArcadeGold else RoyalPlumBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (player.rank) {
                                                1 -> ArcadeGold
                                                2 -> Color(0xFFE2E8F0)
                                                3 -> Color(0xFFD97706)
                                                else -> RoyalPlumSurface
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${player.rank}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (player.rank <= 3) TextOnGold else TextOnDarkPrimary
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (player.isCurrentUser) "${player.playerName} (You)" else player.playerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextOnDarkPrimary
                                    )
                                    Text(
                                        text = "ID: ${player.playerId} • VIP ${player.vipLevel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinterFrostCyan
                                    )
                                }
                            }
                            Text(
                                text = "${player.totalWinnings} Coins",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArcadeGold
                            )
                        }
                    }
                }
            }
        } else {
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No transactions yet. Play games or make a deposit!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnDarkSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        val isPositive = tx.amount >= 0
                        Surface(
                            color = RoyalPlumCard,
                            shape = RoundedCornerShape(14.dp),
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
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextOnDarkPrimary
                                    )
                                    Text(
                                        text = "${tx.method} • ${tx.status} • ${formatter.format(Date(tx.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOnDarkSecondary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isPositive) "+${tx.amount}" else "${tx.amount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isPositive) EmeraldJackpot else FestiveCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
