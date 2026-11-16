package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.dialogs.CheckInDialog
import com.example.ui.dialogs.EmailInboxDialog
import com.example.ui.dialogs.EventAndPromoteDialog
import com.example.ui.dialogs.GiftRedeemDialog
import com.example.ui.dialogs.SpinWheelDialog
import com.example.ui.dialogs.SupportAiDialog
import com.example.ui.dialogs.VipTiersDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DiceBettingScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.Luck780Theme
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.TextOnDarkPrimary
import com.example.viewmodel.LobbyOverlay
import com.example.viewmodel.Luck780ViewModel
import com.example.viewmodel.WalletTab

const val ROUTE_AUTH = "auth"
const val ROUTE_LOBBY = "lobby"
const val ROUTE_WALLET = "wallet"
const val ROUTE_ADMIN = "admin"
const val ROUTE_DICE_GAME = "dice_game"

class MainActivity : ComponentActivity() {
    private val luckViewModel: Luck780ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        // Capture any referral/install deep link from initial launch intent
        luckViewModel.captureReferralIntent(intent)

        // STEP 2: Enable Edge-to-Edge display
        enableEdgeToEdge()

        // STEP 3: Render Compose UI
        setContent {
            Luck780Theme {
                Luck780App(viewModel = luckViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        luckViewModel.captureReferralIntent(intent)
    }
}

@Composable
fun Luck780App(
    viewModel: Luck780ViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val activePhoneKey by viewModel.activePhoneKey.collectAsStateWithLifecycle()
    val savedCredentials by viewModel.savedCredentials.collectAsStateWithLifecycle()
    val adminSettings by viewModel.adminSettings.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val mailMessages by viewModel.mailMessages.collectAsStateWithLifecycle()
    val referrals by viewModel.referrals.collectAsStateWithLifecycle()
    val pendingReferralCode by viewModel.pendingReferralCode.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isAuthBusy by viewModel.isAuthBusy.collectAsStateWithLifecycle()
    val activeOverlay by viewModel.activeOverlay.collectAsStateWithLifecycle()
    val selectedWalletTab by viewModel.selectedWalletTab.collectAsStateWithLifecycle()
    val aiSupportState by viewModel.aiSupportState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(activePhoneKey, currentAccount, currentRoute) {
        if (activePhoneKey != null && currentAccount != null && currentRoute == ROUTE_AUTH) {
            navController.navigate(ROUTE_LOBBY) {
                popUpTo(ROUTE_AUTH) { inclusive = true }
                launchSingleTop = true
            }
        } else if (activePhoneKey == null && currentRoute != null && currentRoute != ROUTE_AUTH && currentRoute != ROUTE_ADMIN) {
            navController.navigate(ROUTE_AUTH) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_AUTH,
            enterTransition = { fadeIn(animationSpec = tween(110)) },
            exitTransition = { fadeOut(animationSpec = tween(90)) },
            popEnterTransition = { fadeIn(animationSpec = tween(110)) },
            popExitTransition = { fadeOut(animationSpec = tween(90)) }
        ) {
            composable(ROUTE_AUTH) {
                AuthScreen(
                    savedCredentials = savedCredentials,
                    pendingReferralCode = pendingReferralCode,
                    authError = authError,
                    isBusy = isAuthBusy,
                    onClearError = viewModel::clearAuthError,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onResetPassword = viewModel::resetPassword,
                    onOpenAdmin = { navController.navigate(ROUTE_ADMIN) }
                )
            }

            composable(ROUTE_LOBBY) {
                val account = currentAccount
                if (account != null) {
                    LobbyScreen(
                        account = account,
                        adminSettings = adminSettings,
                        unreadMailCount = mailMessages.count { !it.isRead },
                        onBuyCoin = {
                            viewModel.selectWalletTab(WalletTab.DEPOSIT)
                            navController.navigate(ROUTE_WALLET)
                        },
                        onOpenWalletTab = { tab ->
                            viewModel.selectWalletTab(tab)
                            navController.navigate(ROUTE_WALLET)
                        },
                        onOpenOverlay = viewModel::openOverlay,
                        onClaimFreeCash = viewModel::claimFreeCash,
                        onOpenDiceGame = { navController.navigate(ROUTE_DICE_GAME) },
                        onOpenAdmin = { navController.navigate(ROUTE_ADMIN) },
                        onLogout = viewModel::logout
                    )
                }
            }

            composable(ROUTE_DICE_GAME) {
                val account = currentAccount
                if (account != null) {
                    DiceBettingScreen(
                        account = account,
                        onBack = { navController.popBackStack() },
                        onOpenDeposit = {
                            viewModel.selectWalletTab(WalletTab.DEPOSIT)
                            navController.navigate(ROUTE_WALLET)
                        },
                        onPlayDiceRound = viewModel::playDiceGameRound
                    )
                }
            }

            composable(ROUTE_WALLET) {
                val account = currentAccount
                if (account != null) {
                    WalletScreen(
                        account = account,
                        selectedTab = selectedWalletTab,
                        bankAccounts = bankAccounts,
                        transactions = transactions,
                        onSelectTab = viewModel::selectWalletTab,
                        onBack = { navController.popBackStack() },
                        onDeposit = viewModel::deposit,
                        onWithdraw = viewModel::withdraw,
                        onBindBank = viewModel::bindBankAccount,
                        onSetPrimaryBank = viewModel::setPrimaryBankAccount,
                        onDeleteBank = viewModel::deleteBankAccount
                    )
                }
            }

            composable(ROUTE_ADMIN) {
                AdminPanelScreen(
                    allAccounts = allAccounts,
                    allTransactions = allTransactions,
                    adminSettings = adminSettings,
                    onBack = { navController.popBackStack() },
                    onAdjustCoins = viewModel::adminAdjustCoins,
                    onGrantTickets = viewModel::adminGrantTickets,
                    onSetVipLevel = viewModel::adminSetVipLevel,
                    onApproveWithdrawal = viewModel::adminApproveWithdrawal,
                    onRejectWithdrawal = viewModel::adminRejectWithdrawal,
                    onBroadcastMail = viewModel::adminBroadcastMail,
                    onUpdateMarquee = viewModel::adminUpdateMarquee,
                    onTogglePromoBanner = viewModel::adminTogglePromoBanner
                )
            }
        }

        // Overlays & Modal Dialogs
        val account = currentAccount
        if (account != null) {
            when (activeOverlay) {
                LobbyOverlay.SPIN_WHEEL -> SpinWheelDialog(
                    account = account,
                    onDismiss = viewModel::closeOverlay,
                    onSpinCompleted = viewModel::applySpinWheelResult,
                    onClaimMilestone = viewModel::claimFreeCash
                )
                LobbyOverlay.CHECK_IN -> CheckInDialog(
                    account = account,
                    onDismiss = viewModel::closeOverlay,
                    onClaimToday = viewModel::performCheckIn
                )
                LobbyOverlay.GIFT -> GiftRedeemDialog(
                    onDismiss = viewModel::closeOverlay,
                    onRedeemCode = viewModel::redeemGiftCode
                )
                LobbyOverlay.VIP -> VipTiersDialog(
                    account = account,
                    onDismiss = viewModel::closeOverlay
                )
                LobbyOverlay.EMAIL -> EmailInboxDialog(
                    messages = mailMessages,
                    onDismiss = viewModel::closeOverlay,
                    onClaimMail = viewModel::claimMail
                )
                LobbyOverlay.EVENT -> EventAndPromoteDialog(
                    isPromote = false,
                    account = account,
                    referrals = referrals,
                    onApplyReferralCode = viewModel::applyReferralCode,
                    onDismiss = viewModel::closeOverlay,
                    onOpenSpinWheel = { viewModel.openOverlay(LobbyOverlay.SPIN_WHEEL) }
                )
                LobbyOverlay.PROMOTE -> EventAndPromoteDialog(
                    isPromote = true,
                    account = account,
                    referrals = referrals,
                    onApplyReferralCode = viewModel::applyReferralCode,
                    onDismiss = viewModel::closeOverlay,
                    onOpenSpinWheel = { viewModel.openOverlay(LobbyOverlay.SPIN_WHEEL) }
                )
                LobbyOverlay.SUPPORT_AI -> SupportAiDialog(
                    uiState = aiSupportState,
                    onAskQuestion = viewModel::askAiSupport,
                    onDismiss = viewModel::closeOverlay
                )
                LobbyOverlay.NONE -> Unit
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = RoyalPlumCard,
                contentColor = TextOnDarkPrimary,
                actionColor = ArcadeGold
            )
        }
    }
}
