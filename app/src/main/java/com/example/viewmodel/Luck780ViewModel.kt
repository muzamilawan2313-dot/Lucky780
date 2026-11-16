package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.SupportAiRepository
import com.example.data.local.AccountEntity
import com.example.data.local.AdminControlSettings
import com.example.data.local.BankAccountEntity
import com.example.data.local.Luck780Database
import com.example.data.local.MailMessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.ReferralRecordEntity
import com.example.data.local.SavedCredentials
import com.example.data.local.TransactionEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.Luck780Repository
import com.example.data.repository.OperationResult
import com.example.model.SpinReward
import com.example.referral.ReferralLinkHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "Luck780ViewModel"

sealed interface AiSupportUiState {
    data object Idle : AiSupportUiState
    data object Loading : AiSupportUiState
    data class Streaming(val text: String) : AiSupportUiState
    data class Success(val text: String) : AiSupportUiState
    data class Error(val message: String) : AiSupportUiState
}

enum class WalletTab(val title: String) {
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    BANK("Bank & Bind"),
    RANKING_HISTORY("Ranks & History")
}

enum class LobbyOverlay {
    NONE,
    SPIN_WHEEL,
    CHECK_IN,
    GIFT,
    VIP,
    EMAIL,
    EVENT,
    PROMOTE,
    SUPPORT_AI
}

@OptIn(ExperimentalCoroutinesApi::class)
class Luck780ViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Luck780Database.getInstance(application)
    private val preferencesManager = PreferencesManager(application)
    private val repository = Luck780Repository(
        accountDao = database.accountDao(),
        bankAccountDao = database.bankAccountDao(),
        transactionDao = database.transactionDao(),
        mailDao = database.mailDao(),
        preferencesManager = preferencesManager,
        referralDao = database.referralDao()
    )
    private val aiRepository = SupportAiRepository()

    private val _activePhoneKey = MutableStateFlow<String?>(null)
    val activePhoneKey: StateFlow<String?> = _activePhoneKey.asStateFlow()

    private val _immediateAccount = MutableStateFlow<AccountEntity?>(null)

    val savedCredentials: StateFlow<SavedCredentials> = repository.savedCredentialsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SavedCredentials())

    val adminSettings: StateFlow<AdminControlSettings> = repository.adminSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminControlSettings())

    val allAccounts: StateFlow<List<AccountEntity>> = repository.observeAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAccount: StateFlow<AccountEntity?> = _activePhoneKey
        .flatMapLatest { key ->
            if (key == null) {
                flowOf(null)
            } else {
                combine(repository.observeAccount(key), _immediateAccount) { dbAccount, immediate ->
                    dbAccount ?: immediate?.takeIf { it.phoneKey == key }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bankAccounts: StateFlow<List<BankAccountEntity>> = _activePhoneKey
        .flatMapLatest { key ->
            if (key == null) flowOf(emptyList()) else repository.observeBankAccounts(key)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = _activePhoneKey
        .flatMapLatest { key ->
            if (key == null) flowOf(emptyList()) else repository.observeTransactions(key)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mailMessages: StateFlow<List<MailMessageEntity>> = _activePhoneKey
        .flatMapLatest { key ->
            if (key == null) flowOf(emptyList()) else repository.observeMail(key)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referrals: StateFlow<List<ReferralRecordEntity>> = _activePhoneKey
        .flatMapLatest { key ->
            if (key == null) flowOf(emptyList()) else repository.observeReferrals(key)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReferralCode: StateFlow<String?> = repository.pendingReferralCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthBusy = MutableStateFlow(false)
    val isAuthBusy: StateFlow<Boolean> = _isAuthBusy.asStateFlow()

    private val _activeOverlay = MutableStateFlow(LobbyOverlay.NONE)
    val activeOverlay: StateFlow<LobbyOverlay> = _activeOverlay.asStateFlow()

    private val _selectedWalletTab = MutableStateFlow(WalletTab.DEPOSIT)
    val selectedWalletTab: StateFlow<WalletTab> = _selectedWalletTab.asStateFlow()

    private val _aiSupportState = MutableStateFlow<AiSupportUiState>(AiSupportUiState.Idle)
    val aiSupportState: StateFlow<AiSupportUiState> = _aiSupportState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDemoAccountExists()
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun login(countryCode: String, phone: String, password: String, savePassword: Boolean) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authError.value = null
            when (val res = repository.login(countryCode, phone, password, savePassword)) {
                is AuthResult.Success -> {
                    _immediateAccount.value = res.account
                    _activePhoneKey.value = res.account.phoneKey
                    _toastMessage.tryEmit("Welcome back, ${res.account.username}!")
                }
                is AuthResult.Error -> {
                    _authError.value = res.message
                }
            }
            _isAuthBusy.value = false
        }
    }

    fun register(
        countryCode: String,
        phone: String,
        password: String,
        confirmPassword: String,
        username: String,
        savePassword: Boolean,
        referralCode: String = ""
    ) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            _authError.value = null
            when (
                val res = repository.register(
                    countryCode = countryCode,
                    rawPhone = phone,
                    password = password,
                    confirmPassword = confirmPassword,
                    username = username,
                    savePassword = savePassword,
                    referralInput = referralCode
                )
            ) {
                is AuthResult.Success -> {
                    _immediateAccount.value = res.account
                    _activePhoneKey.value = res.account.phoneKey
                    val bonusMsg = if (!res.account.referredByCode.isNullOrBlank()) {
                        "Account created via Reference Link! +988 Welcome Coins credited."
                    } else {
                        "Account created! +888 Welcome Coins credited."
                    }
                    _toastMessage.tryEmit(bonusMsg)
                }
                is AuthResult.Error -> {
                    _authError.value = res.message
                }
            }
            _isAuthBusy.value = false
        }
    }

    fun captureReferralIntent(intent: Intent?) {
        val code = ReferralLinkHelper.extractReferralCodeFromIntent(intent) ?: return
        viewModelScope.launch {
            repository.savePendingReferralCode(code)
            val key = _activePhoneKey.value
            if (key != null) {
                when (val res = repository.applyReferralCode(key, code)) {
                    is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                    is OperationResult.Error -> _toastMessage.tryEmit("Invite link detected ($code)")
                }
            } else {
                _toastMessage.tryEmit("Reference invite $code applied! Register to get +100 Bonus Coins.")
            }
        }
    }

    fun applyReferralCode(rawCodeOrLink: String) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.applyReferralCode(key, rawCodeOrLink)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun resetPassword(countryCode: String, phone: String, newPassword: String) {
        viewModelScope.launch {
            when (val res = repository.resetPassword(countryCode, phone, newPassword)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _immediateAccount.value = null
            _activePhoneKey.value = null
            _activeOverlay.value = LobbyOverlay.NONE
        }
    }

    fun openOverlay(overlay: LobbyOverlay) {
        _activeOverlay.value = overlay
    }

    fun closeOverlay() {
        _activeOverlay.value = LobbyOverlay.NONE
    }

    fun selectWalletTab(tab: WalletTab) {
        _selectedWalletTab.value = tab
    }

    fun deposit(amountRs: Long, bonusRs: Long, method: String) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.deposit(key, amountRs, bonusRs, method)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun withdraw(amountRs: Long, bankAccount: BankAccountEntity) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.withdraw(key, amountRs, bankAccount)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun bindBankAccount(
        providerType: String,
        accountTitle: String,
        accountNumber: String,
        bankName: String
    ) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.bindBankAccount(key, providerType, accountTitle, accountNumber, bankName)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun setPrimaryBankAccount(accountId: Long) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            repository.setPrimaryBankAccount(key, accountId)
            _toastMessage.tryEmit("Primary payout account updated.")
        }
    }

    fun deleteBankAccount(accountId: Long) {
        viewModelScope.launch {
            repository.deleteBankAccount(accountId)
            _toastMessage.tryEmit("Bound account removed.")
        }
    }

    fun applySpinWheelResult(reward: SpinReward, usedCoinFallback: Boolean) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.applySpinWheelResult(key, reward, usedCoinFallback)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun claimFreeCash() {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.claimFreeCashMilestone(key)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun performCheckIn() {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.performDailyCheckIn(key)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun redeemGiftCode(code: String) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.redeemGiftCode(key, code)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun claimMail(mail: MailMessageEntity) {
        val key = _activePhoneKey.value ?: return
        viewModelScope.launch {
            when (val res = repository.claimMailReward(key, mail)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    suspend fun playDiceGameRound(
        betCoins: Long,
        grossPayoutCoins: Long,
        summaryLabel: String
    ): Long {
        val key = _activePhoneKey.value ?: return 0L
        return repository.recordDiceGameOutcome(key, betCoins, grossPayoutCoins, summaryLabel)
    }

    fun askAiSupport(query: String) {
        if (query.isBlank()) return
        val account = currentAccount.value
        val contextSummary = if (account != null) {
            "User: ${account.username} (ID: ${account.playerUid}), VIP ${account.vipLevel} (${account.vipPoints} pts), " +
                "Coins: ${account.coins}, Free Cash Progress: ${account.freeCashProgress}/1333, " +
                "Spin Tickets: ${account.bonusTickets}, Active Multiplier: ${account.activeMultiplier}x, " +
                "Bank Bound: ${account.isBound}"
        } else {
            "Guest browsing Luck780"
        }

        viewModelScope.launch {
            _aiSupportState.value = AiSupportUiState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    aiRepository.streamAdvice(query, contextSummary).collect { chunk ->
                        accumulated.append(chunk)
                        if (accumulated.isNotEmpty()) {
                            _aiSupportState.value = AiSupportUiState.Streaming(accumulated.toString())
                        }
                    }
                }
                if (accumulated.isNotBlank()) {
                    _aiSupportState.value = AiSupportUiState.Success(accumulated.toString())
                } else {
                    _aiSupportState.value = AiSupportUiState.Error("Advisor returned an empty response. Please try again.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Support request timed out", e)
                _aiSupportState.value = AiSupportUiState.Error(
                    "Deep thinking timed out after 60 seconds. Please try a shorter question."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI Support failed", e)
                _aiSupportState.value = AiSupportUiState.Error(
                    e.localizedMessage ?: "Unable to reach Luck780 AI Support."
                )
            }
        }
    }

    fun resetAiSupportState() {
        _aiSupportState.value = AiSupportUiState.Idle
    }

    // --- Admin Panel Actions ---

    fun adminAdjustCoins(phoneKey: String, delta: Long, reason: String) {
        viewModelScope.launch {
            when (val res = repository.adjustUserCoins(phoneKey, delta, reason)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminGrantTickets(phoneKey: String, tickets: Int) {
        viewModelScope.launch {
            when (val res = repository.grantUserTickets(phoneKey, tickets)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminSetVipLevel(phoneKey: String, level: Int) {
        viewModelScope.launch {
            when (val res = repository.setUserVipLevel(phoneKey, level)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminApproveWithdrawal(tx: TransactionEntity) {
        viewModelScope.launch {
            when (val res = repository.approveWithdrawal(tx)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminRejectWithdrawal(tx: TransactionEntity) {
        viewModelScope.launch {
            when (val res = repository.rejectAndRefundWithdrawal(tx)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminBroadcastMail(subject: String, body: String, rewardCoins: Long) {
        viewModelScope.launch {
            when (val res = repository.broadcastSystemMail(subject, body, rewardCoins)) {
                is OperationResult.Success -> _toastMessage.tryEmit(res.message)
                is OperationResult.Error -> _toastMessage.tryEmit(res.message)
            }
        }
    }

    fun adminUpdateMarquee(text: String) {
        viewModelScope.launch {
            repository.updateAdminSettings(marqueeAnnouncement = text.trim())
            _toastMessage.tryEmit("Live lobby announcement updated!")
        }
    }

    fun adminTogglePromoBanner(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAdminSettings(freeSpinPromoEnabled = enabled)
            _toastMessage.tryEmit(if (enabled) "Spin promo banner enabled." else "Spin promo banner paused.")
        }
    }
}
