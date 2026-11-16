package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.AccountEntity
import com.example.data.local.AdminControlSettings
import com.example.data.local.BankAccountDao
import com.example.data.local.BankAccountEntity
import com.example.data.local.MailDao
import com.example.data.local.MailMessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.ReferralDao
import com.example.data.local.ReferralRecordEntity
import com.example.data.local.SavedCredentials
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.model.SpinReward
import com.example.model.VIP_TIERS
import com.example.referral.ReferralLinkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.math.min

sealed interface AuthResult {
    data class Success(val account: AccountEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

sealed interface OperationResult {
    data class Success(val message: String) : OperationResult
    data class Error(val message: String) : OperationResult
}

class Luck780Repository(
    private val accountDao: AccountDao,
    private val bankAccountDao: BankAccountDao,
    private val transactionDao: TransactionDao,
    private val mailDao: MailDao,
    private val preferencesManager: PreferencesManager,
    private val referralDao: ReferralDao? = null
) {
    val savedCredentialsFlow: Flow<SavedCredentials> = preferencesManager.savedCredentialsFlow
    val adminSettingsFlow: Flow<AdminControlSettings> = preferencesManager.adminSettingsFlow
    val pendingReferralCodeFlow: Flow<String?> = preferencesManager.pendingReferralCodeFlow

    fun observeAccount(phoneKey: String): Flow<AccountEntity?> =
        accountDao.observeAccount(phoneKey)

    fun observeAllAccounts(): Flow<List<AccountEntity>> =
        accountDao.observeAllAccounts()

    fun observeBankAccounts(phoneKey: String): Flow<List<BankAccountEntity>> =
        bankAccountDao.observeBankAccounts(phoneKey)

    fun observeTransactions(phoneKey: String): Flow<List<TransactionEntity>> =
        transactionDao.observeTransactions(phoneKey)

    fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAllTransactions()

    fun observeMail(phoneKey: String): Flow<List<MailMessageEntity>> =
        mailDao.observeMail(phoneKey)

    fun observeReferrals(phoneKey: String): Flow<List<ReferralRecordEntity>> =
        referralDao?.observeReferrals(phoneKey) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun savePendingReferralCode(code: String) = withContext(Dispatchers.IO) {
        preferencesManager.savePendingReferralCode(code)
    }

    suspend fun clearPendingReferralCode() = withContext(Dispatchers.IO) {
        preferencesManager.clearPendingReferralCode()
    }

    suspend fun ensureDemoAccountExists() = withContext(Dispatchers.IO) {
        val demoKey = buildPhoneKey("+92", "3001234567")
        if (accountDao.getAccount(demoKey) == null) {
            val demo = AccountEntity(
                phoneKey = demoKey,
                countryCode = "+92",
                phoneNumber = "3001234567",
                passwordHash = hashPassword(demoKey, "luck780"),
                username = "LuckyAce780",
                playerUid = "78084291",
                vipLevel = 3,
                vipPoints = 1850L,
                coins = 2480L,
                freeCashProgress = 1120L,
                bonusTickets = 4,
                activeMultiplier = 1,
                lastCheckInDay = 2,
                lastCheckInEpochDay = 0L,
                isBound = true,
                giftClaimed = false
            )
            accountDao.upsertAccount(demo)
            seedInitialAccountData(demoKey, isDemo = true)
        }
    }

    suspend fun login(
        countryCode: String,
        rawPhone: String,
        password: String,
        savePassword: Boolean
    ): AuthResult = withContext(Dispatchers.IO) {
        val phone = rawPhone.trim().filter { it.isDigit() }
        if (phone.length < 7) {
            return@withContext AuthResult.Error("Please enter a valid mobile number.")
        }
        if (password.length < 4) {
            return@withContext AuthResult.Error("Password must be at least 4 characters.")
        }
        val key = buildPhoneKey(countryCode, phone)
        if (key == buildPhoneKey("+92", "3001234567")) {
            ensureDemoAccountExists()
        }
        val existing = accountDao.getAccount(key)
            ?: return@withContext AuthResult.Error("No account found for $countryCode $phone. Tap Register to create one.")
        if (existing.passwordHash != hashPassword(key, password)) {
            return@withContext AuthResult.Error("Incorrect password. Please try again or use Forgot Password.")
        }
        preferencesManager.saveLoginPreference(
            savePassword = savePassword,
            countryCode = countryCode,
            phoneNumber = phone,
            password = password,
            activePhoneKey = key
        )
        AuthResult.Success(existing)
    }

    suspend fun register(
        countryCode: String,
        rawPhone: String,
        password: String,
        confirmPassword: String,
        username: String,
        savePassword: Boolean,
        referralInput: String = ""
    ): AuthResult = withContext(Dispatchers.IO) {
        val phone = rawPhone.trim().filter { it.isDigit() }
        if (phone.length < 7) {
            return@withContext AuthResult.Error("Please enter a valid mobile number (at least 7 digits).")
        }
        if (password.length < 4) {
            return@withContext AuthResult.Error("Password must be at least 4 characters.")
        }
        if (password != confirmPassword) {
            return@withContext AuthResult.Error("Passwords do not match.")
        }
        val key = buildPhoneKey(countryCode, phone)
        if (accountDao.getAccount(key) != null) {
            return@withContext AuthResult.Error("An account with $countryCode $phone already exists. Please login.")
        }

        val parsedRefCode = if (referralInput.isNotBlank()) {
            ReferralLinkHelper.extractReferralCodeFromInput(referralInput)
                ?: return@withContext AuthResult.Error("Referral code must be at least 4 characters or a valid invite link.")
        } else {
            null
        }

        if (parsedRefCode != null) {
            ensureDemoAccountExists()
        }

        val cleanName = username.trim().ifBlank { "Player_${phone.takeLast(4)}" }
        val uid = "780${(10000..99999).random()}"
        val ownReferralCode = ReferralLinkHelper.generateUniqueReferralCode(key, uid)
        val extraJoinCoins = if (parsedRefCode != null) ReferralLinkHelper.REFEREE_BONUS_COINS else 0L

        val account = AccountEntity(
            phoneKey = key,
            countryCode = countryCode,
            phoneNumber = phone,
            passwordHash = hashPassword(key, password),
            username = cleanName,
            playerUid = uid,
            vipLevel = 1,
            vipPoints = 150L,
            coins = 888L + extraJoinCoins,
            freeCashProgress = 850L,
            bonusTickets = 3,
            activeMultiplier = 1,
            lastCheckInDay = 0,
            lastCheckInEpochDay = 0L,
            isBound = false,
            giftClaimed = false,
            referralCode = ownReferralCode,
            referredByCode = parsedRefCode
        )
        accountDao.upsertAccount(account)
        seedInitialAccountData(key, isDemo = false)

        if (parsedRefCode != null) {
            creditReferralPair(
                referee = account,
                refCodeUsed = parsedRefCode,
                alreadyCreditedReferee = true
            )
            preferencesManager.clearPendingReferralCode()
        }

        preferencesManager.saveLoginPreference(
            savePassword = savePassword,
            countryCode = countryCode,
            phoneNumber = phone,
            password = password,
            activePhoneKey = key
        )
        AuthResult.Success(account)
    }

    suspend fun applyReferralCode(phoneKey: String, rawCodeOrLink: String): OperationResult =
        withContext(Dispatchers.IO) {
            val account = accountDao.getAccount(phoneKey)
                ?: return@withContext OperationResult.Error("Account missing.")
            if (!account.referredByCode.isNullOrBlank()) {
                return@withContext OperationResult.Error("Reference code ${account.referredByCode} already linked.")
            }
            val parsedCode = ReferralLinkHelper.extractReferralCodeFromInput(rawCodeOrLink)
                ?: return@withContext OperationResult.Error("Enter a valid reference code or paste a reference link.")
            if (account.referralCode.equals(parsedCode, ignoreCase = true)) {
                return@withContext OperationResult.Error("You cannot use your own reference code.")
            }

            ensureDemoAccountExists()
            val updatedReferee = account.copy(
                coins = account.coins + ReferralLinkHelper.REFEREE_BONUS_COINS,
                referredByCode = parsedCode
            )
            accountDao.updateAccount(updatedReferee)
            creditReferralPair(
                referee = updatedReferee,
                refCodeUsed = parsedCode,
                alreadyCreditedReferee = false
            )
            preferencesManager.clearPendingReferralCode()
            OperationResult.Success(
                "Reference linked ($parsedCode)! +${ReferralLinkHelper.REFEREE_BONUS_COINS} Bonus Coins added!"
            )
        }

    private suspend fun creditReferralPair(
        referee: AccountEntity,
        refCodeUsed: String,
        alreadyCreditedReferee: Boolean
    ) {
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = referee.phoneKey,
                type = "REFERRAL_JOIN",
                title = "Reference Link Join Bonus ($refCodeUsed)",
                amount = ReferralLinkHelper.REFEREE_BONUS_COINS,
                method = "Reference System"
            )
        )

        val referrer = accountDao.getAccountByReferralCode(refCodeUsed)
        if (referrer != null && referrer.phoneKey != referee.phoneKey) {
            accountDao.updateAccount(
                referrer.copy(
                    coins = referrer.coins + ReferralLinkHelper.REFERRER_BONUS_COINS,
                    bonusTickets = referrer.bonusTickets + ReferralLinkHelper.REFERRER_BONUS_TICKETS,
                    referralCount = referrer.referralCount + 1,
                    referralEarnedCoins = referrer.referralEarnedCoins + ReferralLinkHelper.REFERRER_BONUS_COINS
                )
            )
            referralDao?.insertReferral(
                ReferralRecordEntity(
                    referrerPhoneKey = referrer.phoneKey,
                    refereePhoneKey = referee.phoneKey,
                    refereeName = referee.username,
                    referralCodeUsed = refCodeUsed,
                    referrerBonusCoins = ReferralLinkHelper.REFERRER_BONUS_COINS,
                    referrerBonusTickets = ReferralLinkHelper.REFERRER_BONUS_TICKETS,
                    refereeBonusCoins = ReferralLinkHelper.REFEREE_BONUS_COINS
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = referrer.phoneKey,
                    type = "REFERRAL_BONUS",
                    title = "Invite Commission: ${referee.username} installed!",
                    amount = ReferralLinkHelper.REFERRER_BONUS_COINS,
                    method = "Reference System"
                )
            )
            mailDao.insertMail(
                MailMessageEntity(
                    ownerPhoneKey = referrer.phoneKey,
                    subject = "New Install via Your Reference Link!",
                    body = "${referee.username} joined with code $refCodeUsed! +${ReferralLinkHelper.REFERRER_BONUS_COINS} Coins & +${ReferralLinkHelper.REFERRER_BONUS_TICKETS} Spin Tickets credited.",
                    rewardCoins = 0L,
                    isClaimed = true
                )
            )
        } else if (!alreadyCreditedReferee) {
            // Even for an external link code, log clean confirmation in referee's history
        }
    }

    suspend fun resetPassword(
        countryCode: String,
        rawPhone: String,
        newPassword: String
    ): OperationResult {
        val phone = rawPhone.trim().filter { it.isDigit() }
        if (phone.length < 7) return OperationResult.Error("Enter your registered phone number.")
        if (newPassword.length < 4) return OperationResult.Error("New password must be at least 4 characters.")
        val key = buildPhoneKey(countryCode, phone)
        val account = accountDao.getAccount(key)
            ?: return OperationResult.Error("Account not found for $countryCode $phone.")
        accountDao.updateAccount(account.copy(passwordHash = hashPassword(key, newPassword)))
        return OperationResult.Success("Password updated for $countryCode $phone. You can now log in.")
    }

    suspend fun logout() {
        preferencesManager.clearSession()
    }

    suspend fun deposit(
        phoneKey: String,
        amountRs: Long,
        bonusRs: Long,
        method: String
    ): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        val totalCredit = amountRs + bonusRs
        val newPoints = account.vipPoints + (amountRs / 2)
        val newVip = calculateVipLevel(newPoints)
        val newTickets = account.bonusTickets + if (amountRs >= 500) 2 else 1
        accountDao.updateAccount(
            account.copy(
                coins = account.coins + totalCredit,
                vipPoints = newPoints,
                vipLevel = newVip,
                bonusTickets = newTickets
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = "DEPOSIT",
                title = "$method Top-Up (+Rs$bonusRs Bonus)",
                amount = amountRs,
                bonusAmount = bonusRs,
                status = "Completed",
                method = method
            )
        )
        return OperationResult.Success("Deposited Rs $amountRs + Rs $bonusRs bonus! (+$totalCredit Coins)")
    }

    suspend fun withdraw(
        phoneKey: String,
        amountRs: Long,
        bankAccount: BankAccountEntity
    ): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        if (amountRs < 100) return OperationResult.Error("Minimum withdrawal is Rs 100.")
        if (amountRs > account.coins) return OperationResult.Error("Insufficient coin balance.")

        accountDao.updateAccount(account.copy(coins = account.coins - amountRs))
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = "WITHDRAW",
                title = "Withdraw to ${bankAccount.providerType} (${bankAccount.accountNumber.takeLast(4)})",
                amount = -amountRs,
                bonusAmount = 0L,
                status = "Processing",
                method = bankAccount.providerType
            )
        )
        return OperationResult.Success("Withdrawal of Rs $amountRs submitted to ${bankAccount.providerType}.")
    }

    suspend fun bindBankAccount(
        phoneKey: String,
        providerType: String,
        accountTitle: String,
        accountNumber: String,
        bankName: String
    ): OperationResult {
        if (accountTitle.isBlank()) return OperationResult.Error("Account holder name is required.")
        if (accountNumber.trim().length < 7) return OperationResult.Error("Enter a valid account or wallet number.")
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")

        bankAccountDao.insertBankAccount(
            BankAccountEntity(
                ownerPhoneKey = phoneKey,
                providerType = providerType,
                accountTitle = accountTitle.trim(),
                accountNumber = accountNumber.trim(),
                bankName = bankName.trim().ifBlank { providerType },
                isPrimary = true
            )
        )
        val bindBonus = if (!account.isBound) 50L else 0L
        accountDao.updateAccount(
            account.copy(
                isBound = true,
                coins = account.coins + bindBonus
            )
        )
        if (bindBonus > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "FREE_CASH",
                    title = "Account Bind Welcome Reward",
                    amount = bindBonus,
                    method = providerType
                )
            )
        }
        return OperationResult.Success(
            if (bindBonus > 0) "$providerType bound successfully! +$bindBonus Bonus Coins added."
            else "$providerType account added to your wallet."
        )
    }

    suspend fun setPrimaryBankAccount(phoneKey: String, accountId: Long) {
        bankAccountDao.setPrimaryBankAccount(phoneKey, accountId)
    }

    suspend fun deleteBankAccount(accountId: Long) {
        bankAccountDao.deleteBankAccount(accountId)
    }

    suspend fun applySpinWheelResult(
        phoneKey: String,
        reward: SpinReward,
        usedCoinFallback: Boolean
    ): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        if (!usedCoinFallback && account.bonusTickets <= 0) {
            return OperationResult.Error("No spin tickets left.")
        }
        if (usedCoinFallback && account.coins < 20) {
            return OperationResult.Error("Need 20 Coins or 1 Bonus Ticket to spin.")
        }

        val ticketDelta = if (usedCoinFallback) 0 else -1
        val costDelta = if (usedCoinFallback) -20L else 0L
        var coinReward = 0L
        var newMultiplier = account.activeMultiplier
        var extraTickets = 0
        val progressGain = 45L

        val summary = when (reward) {
            is SpinReward.CashCoins -> {
                coinReward = reward.amount * account.activeMultiplier
                if (account.activeMultiplier > 1) {
                    newMultiplier = 1
                }
                "+$coinReward Coins won from Lucky Wheel!"
            }
            is SpinReward.MultiplierBoost -> {
                newMultiplier = reward.factor
                "Activated ${reward.factor}x Win Multiplier for your next win!"
            }
            is SpinReward.BonusTicket -> {
                extraTickets = reward.count
                "+${reward.count} Bonus Spin Ticket awarded!"
            }
        }

        val updatedFreeCash = min(1333L, account.freeCashProgress + progressGain)
        accountDao.updateAccount(
            account.copy(
                coins = account.coins + costDelta + coinReward,
                bonusTickets = (account.bonusTickets + ticketDelta + extraTickets).coerceAtLeast(0),
                activeMultiplier = newMultiplier,
                freeCashProgress = updatedFreeCash
            )
        )
        if (coinReward > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "SPIN_WIN",
                    title = "Lucky Spin Wheel Prize",
                    amount = coinReward,
                    method = "Turntable"
                )
            )
        }
        return OperationResult.Success(summary)
    }

    suspend fun claimFreeCashMilestone(phoneKey: String): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        return if (account.freeCashProgress >= 1333L) {
            accountDao.updateAccount(
                account.copy(
                    coins = account.coins + 1333L,
                    freeCashProgress = 250L
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "FREE_CASH",
                    title = "1,333 Free Cash Jackpot Claimed",
                    amount = 1333L,
                    method = "Event"
                )
            )
            OperationResult.Success("Claimed 1,333 Free Coins!")
        } else {
            val boost = 120L
            val nextProgress = min(1333L, account.freeCashProgress + boost)
            accountDao.updateAccount(
                account.copy(
                    freeCashProgress = nextProgress,
                    coins = account.coins + 30L
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "FREE_CASH",
                    title = "Daily Free Cash Assist (+30 Coins)",
                    amount = 30L,
                    method = "Free Cash"
                )
            )
            OperationResult.Success("Got +30 Coins & boosted 1,333 Free Cash bar to $nextProgress/1,333!")
        }
    }

    suspend fun performDailyCheckIn(phoneKey: String): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        val today = System.currentTimeMillis() / 86_400_000L
        if (account.lastCheckInEpochDay == today) {
            return OperationResult.Error("Today's check-in is already claimed! Come back tomorrow.")
        }
        val nextDay = if (account.lastCheckInDay >= 7) 1 else account.lastCheckInDay + 1
        val rewardCoins = nextDay * 25L
        val rewardTicket = if (nextDay == 3 || nextDay == 7) 1 else 0
        accountDao.updateAccount(
            account.copy(
                coins = account.coins + rewardCoins,
                bonusTickets = account.bonusTickets + rewardTicket,
                lastCheckInDay = nextDay,
                lastCheckInEpochDay = today,
                vipPoints = account.vipPoints + 40L,
                vipLevel = calculateVipLevel(account.vipPoints + 40L)
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = "CHECK_IN",
                title = "Day $nextDay Check-In Reward",
                amount = rewardCoins,
                method = "Check-In"
            )
        )
        return OperationResult.Success(
            "Day $nextDay Check-In claimed! +$rewardCoins Coins" +
                if (rewardTicket > 0) " & +$rewardTicket Spin Ticket!" else "!"
        )
    }

    suspend fun redeemGiftCode(phoneKey: String, code: String): OperationResult {
        val normalized = code.trim().uppercase()
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        val validCodes = mapOf(
            "LUCK780" to 150L,
            "WINTER780" to 200L,
            "VIPBONUS" to 120L
        )
        val reward = validCodes[normalized]
            ?: return OperationResult.Error("Invalid gift code. Try LUCK780, WINTER780, or VIPBONUS.")
        if (account.giftClaimed && normalized == "LUCK780") {
            return OperationResult.Error("Welcome gift LUCK780 was already redeemed.")
        }
        accountDao.updateAccount(
            account.copy(
                coins = account.coins + reward,
                bonusTickets = account.bonusTickets + 1,
                giftClaimed = if (normalized == "LUCK780") true else account.giftClaimed
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = "GIFT_BONUS",
                title = "Gift Code $normalized Redeemed",
                amount = reward,
                method = "Gift Box"
            )
        )
        return OperationResult.Success("Gift code $normalized redeemed! +$reward Coins & +1 Spin Ticket.")
    }

    suspend fun claimMailReward(phoneKey: String, mail: MailMessageEntity): OperationResult {
        if (mail.isClaimed) {
            mailDao.markRead(mail.id)
            return OperationResult.Error("Reward in this mail was already claimed.")
        }
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Account missing.")
        mailDao.markClaimed(mail.id)
        if (mail.rewardCoins > 0) {
            accountDao.updateAccount(account.copy(coins = account.coins + mail.rewardCoins))
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "GIFT_BONUS",
                    title = "Inbox Reward: ${mail.subject}",
                    amount = mail.rewardCoins,
                    method = "E-Mail"
                )
            )
            return OperationResult.Success("Claimed +${mail.rewardCoins} Coins from inbox!")
        }
        return OperationResult.Success("Message marked as read.")
    }

    suspend fun recordDiceGameOutcome(
        phoneKey: String,
        betCoins: Long,
        grossPayoutCoins: Long,
        summaryLabel: String
    ): Long = withContext(Dispatchers.IO) {
        val account = accountDao.getAccount(phoneKey) ?: return@withContext 0L
        if (betCoins <= 0L) return@withContext 0L
        val multiplier = if (grossPayoutCoins > 0) account.activeMultiplier else 1
        val finalPayout = grossPayoutCoins * multiplier
        val netDelta = finalPayout - betCoins
        val updatedCoins = (account.coins + netDelta).coerceAtLeast(0L)
        val newVipPoints = account.vipPoints + (betCoins / 5).coerceAtLeast(5L)
        accountDao.updateAccount(
            account.copy(
                coins = updatedCoins,
                activeMultiplier = if (grossPayoutCoins > 0 && account.activeMultiplier > 1) 1 else account.activeMultiplier,
                vipPoints = newVipPoints,
                vipLevel = calculateVipLevel(newVipPoints)
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = if (netDelta >= 0) "GAME_WIN" else "GAME_PLAY",
                title = if (finalPayout > 0) "Dice Battle Win ($summaryLabel)" else "Dice Battle ($summaryLabel)",
                amount = netDelta,
                method = "Dice Battle"
            )
        )
        finalPayout
    }

    private suspend fun seedInitialAccountData(phoneKey: String, isDemo: Boolean) {
        if (isDemo) {
            bankAccountDao.insertBankAccount(
                BankAccountEntity(
                    ownerPhoneKey = phoneKey,
                    providerType = "JazzCash",
                    accountTitle = "LuckyAce780",
                    accountNumber = "03001234567",
                    bankName = "JazzCash Mobile Wallet",
                    isPrimary = true
                )
            )
            bankAccountDao.insertBankAccount(
                BankAccountEntity(
                    ownerPhoneKey = phoneKey,
                    providerType = "Easypaisa",
                    accountTitle = "LuckyAce780",
                    accountNumber = "03459876543",
                    bankName = "Easypaisa Wallet",
                    isPrimary = false
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "DEPOSIT",
                    title = "JazzCash Top-Up (+Rs65 Bonus)",
                    amount = 500,
                    bonusAmount = 65,
                    method = "JazzCash"
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = phoneKey,
                    type = "SPIN_WIN",
                    title = "Lucky Spin Wheel Prize",
                    amount = 100,
                    method = "Turntable"
                )
            )
        }
        mailDao.insertMail(
            MailMessageEntity(
                ownerPhoneKey = phoneKey,
                subject = "Welcome to Luck780 Winter Carnival!",
                body = "Claim your starter gift and spin the Turntable toward the 1,333 Free Cash milestone.",
                rewardCoins = 100L
            )
        )
        mailDao.insertMail(
            MailMessageEntity(
                ownerPhoneKey = phoneKey,
                subject = "Extra +Rs10 Bonus on Deposits",
                body = "Top up Rs 100 or more via JazzCash, Easypaisa, or Bank Transfer to earn instant bonus coins and Spin Tickets.",
                rewardCoins = 50L
            )
        )
    }

    // --- Admin Panel Operations ---

    suspend fun adjustUserCoins(phoneKey: String, deltaCoins: Long, reason: String): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Player not found.")
        val nextCoins = (account.coins + deltaCoins).coerceAtLeast(0L)
        accountDao.updateAccount(account.copy(coins = nextCoins))
        transactionDao.insertTransaction(
            TransactionEntity(
                ownerPhoneKey = phoneKey,
                type = if (deltaCoins >= 0) "ADMIN_CREDIT" else "ADMIN_DEBIT",
                title = reason.ifBlank { if (deltaCoins >= 0) "Admin Bonus Credit" else "Admin Balance Adjustment" },
                amount = deltaCoins,
                status = "Completed",
                method = "Admin Panel"
            )
        )
        val sign = if (deltaCoins >= 0) "+" else ""
        return OperationResult.Success("Updated ${account.username} balance by $sign$deltaCoins Coins.")
    }

    suspend fun grantUserTickets(phoneKey: String, tickets: Int): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Player not found.")
        accountDao.updateAccount(
            account.copy(bonusTickets = (account.bonusTickets + tickets).coerceAtLeast(0))
        )
        return OperationResult.Success("Granted +$tickets Spin Tickets to ${account.username}.")
    }

    suspend fun setUserVipLevel(phoneKey: String, vipLevel: Int): OperationResult {
        val account = accountDao.getAccount(phoneKey) ?: return OperationResult.Error("Player not found.")
        val clamped = vipLevel.coerceIn(1, 5)
        val minPts = VIP_TIERS.firstOrNull { it.level == clamped }?.minPoints ?: 0L
        accountDao.updateAccount(
            account.copy(
                vipLevel = clamped,
                vipPoints = maxOf(account.vipPoints, minPts)
            )
        )
        return OperationResult.Success("${account.username} set to VIP $clamped.")
    }

    suspend fun approveWithdrawal(tx: TransactionEntity): OperationResult {
        if (tx.status == "Completed") return OperationResult.Error("Already approved.")
        transactionDao.updateTransactionStatus(tx.id, "Completed")
        return OperationResult.Success("Approved withdrawal #${tx.id} (Rs ${kotlin.math.abs(tx.amount)}).")
    }

    suspend fun rejectAndRefundWithdrawal(tx: TransactionEntity): OperationResult {
        if (tx.status != "Processing") return OperationResult.Error("Only pending withdrawals can be refunded.")
        transactionDao.updateTransactionStatus(tx.id, "Rejected (Refunded)")
        val refund = kotlin.math.abs(tx.amount)
        val account = accountDao.getAccount(tx.ownerPhoneKey)
        if (account != null) {
            accountDao.updateAccount(account.copy(coins = account.coins + refund))
            transactionDao.insertTransaction(
                TransactionEntity(
                    ownerPhoneKey = tx.ownerPhoneKey,
                    type = "REFUND",
                    title = "Refund: ${tx.title}",
                    amount = refund,
                    status = "Completed",
                    method = "Admin Refund"
                )
            )
        }
        return OperationResult.Success("Rejected #${tx.id} and refunded $refund Coins.")
    }

    suspend fun broadcastSystemMail(subject: String, body: String, rewardCoins: Long): OperationResult {
        if (subject.isBlank() || body.isBlank()) {
            return OperationResult.Error("Subject and message body are required.")
        }
        val accounts = accountDao.getAllAccountsOnce()
        accounts.forEach { acc ->
            mailDao.insertMail(
                MailMessageEntity(
                    ownerPhoneKey = acc.phoneKey,
                    subject = subject.trim(),
                    body = body.trim(),
                    rewardCoins = rewardCoins.coerceAtLeast(0L)
                )
            )
        }
        return OperationResult.Success("Broadcast sent to ${accounts.size} player(s)!")
    }

    suspend fun updateAdminSettings(
        marqueeAnnouncement: String? = null,
        freeSpinPromoEnabled: Boolean? = null
    ) {
        preferencesManager.updateAdminSettings(
            marqueeAnnouncement = marqueeAnnouncement,
            freeSpinPromoEnabled = freeSpinPromoEnabled
        )
    }

    private fun calculateVipLevel(points: Long): Int {
        return VIP_TIERS.lastOrNull { points >= it.minPoints }?.level ?: 1
    }

    companion object {
        fun buildPhoneKey(countryCode: String, phone: String): String =
            "${countryCode.trim()}_${phone.trim().filter { it.isDigit() }}"

        private fun hashPassword(salt: String, password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$salt::$password::luck780".toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
