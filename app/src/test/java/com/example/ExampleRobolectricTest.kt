package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountEntity
import com.example.data.local.AdminControlSettings
import com.example.data.local.Luck780Database
import com.example.data.local.PreferencesManager
import com.example.data.repository.AuthResult
import com.example.data.repository.Luck780Repository
import com.example.data.repository.OperationResult
import com.example.model.DiceBetZone
import com.example.model.DiceRngEngine
import com.example.model.SpinReward
import com.example.referral.ReferralLinkHelper
import com.example.ui.dialogs.EventAndPromoteDialog
import com.example.ui.screens.DiceBettingScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.theme.Luck780Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app name matches Luck780`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Luck780", appName)
    }

    @Test
    fun `DiceRngEngine evaluates zones, odds, and lightning 2X multipliers accurately`() {
        assertEquals(3, DiceBetZone.entries.size)

        // 3 + 4 = 7 -> EXACT_7 (5x)
        val sevenOutcome = DiceRngEngine.evaluateRoll(
            die1 = 3,
            die2 = 4,
            bets = mapOf(
                DiceBetZone.EXACT_7 to 10L,
                DiceBetZone.HIGH_8_12 to 10L
            ),
            lightningZone = null
        )
        assertEquals(7, sevenOutcome.sum)
        assertEquals(20L, sevenOutcome.totalBet)
        assertEquals(50L, sevenOutcome.grossPayout) // 10*5
        assertEquals(listOf(DiceBetZone.EXACT_7), sevenOutcome.winningZones)

        // 5 + 5 = 10 -> HIGH_8_12 (2x) with Lightning 2X = 4x
        val tenOutcome = DiceRngEngine.evaluateRoll(
            die1 = 5,
            die2 = 5,
            bets = mapOf(DiceBetZone.LOW_2_6 to 10L, DiceBetZone.HIGH_8_12 to 20L),
            lightningZone = DiceBetZone.HIGH_8_12
        )
        assertEquals(80L, tenOutcome.grossPayout) // 20*2*2
        assertTrue(tenOutcome.hitLightning)
        assertEquals(listOf(DiceBetZone.HIGH_8_12), tenOutcome.winningZones)
    }

    @Test
    fun `lobby displays Dice Battle Table card`() {
        val demoAccount = AccountEntity(
            phoneKey = "+923416877144",
            countryCode = "+92",
            phoneNumber = "3416877144",
            username = "name983302",
            playerUid = "8395482",
            passwordHash = "secret",
            coins = 1000L
        )

        composeRule.setContent {
            Luck780Theme {
                LobbyScreen(
                    account = demoAccount,
                    adminSettings = AdminControlSettings(),
                    unreadMailCount = 1,
                    onBuyCoin = {},
                    onOpenWalletTab = {},
                    onOpenOverlay = {},
                    onClaimFreeCash = {},
                    onOpenDiceGame = {},
                    onOpenAdmin = {},
                    onLogout = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dice_lobby_stage").assertIsDisplayed()
        composeRule.onNodeWithTag("lobby_category_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("dice_game_logo_tile").assertIsDisplayed()
        composeRule.onNodeWithText("Dice Battle Table").assertIsDisplayed()
        composeRule.onNodeWithTag("open_dice_game_button").assertIsDisplayed()
    }

    @Test
    fun `DiceBettingScreen enters WATCHING THE BATTLE when coins below 10`() {
        val lowBalanceAccount = AccountEntity(
            phoneKey = "+923416877144",
            countryCode = "+92",
            phoneNumber = "3416877144",
            username = "name983302",
            playerUid = "8395482",
            passwordHash = "secret",
            coins = 0L
        )

        composeRule.setContent {
            Luck780Theme {
                DiceBettingScreen(
                    account = lowBalanceAccount,
                    onBack = {},
                    onOpenDeposit = {},
                    onPlayDiceRound = { _, gross, _ -> gross }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("watching_battle_banner").assertIsDisplayed()
        composeRule.onNodeWithText("WATCHING THE BATTLE.").assertIsDisplayed()
        composeRule.onNodeWithText("You need at least 10 coins to play the game").assertIsDisplayed()
    }

    @Test
    fun `DiceBettingScreen places bet on 2-6 zone and reveals ROLL button`() {
        val playableAccount = AccountEntity(
            phoneKey = "+923416877144",
            countryCode = "+92",
            phoneNumber = "3416877144",
            username = "name983302",
            playerUid = "8395482",
            passwordHash = "secret",
            coins = 500L
        )

        composeRule.setContent {
            Luck780Theme {
                DiceBettingScreen(
                    account = playableAccount,
                    onBack = {},
                    onOpenDeposit = {},
                    onPlayDiceRound = { _, gross, _ -> gross }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dice_sound_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("dice_zone_seven").assertIsDisplayed()
        composeRule.onNodeWithTag("dice_zone_high").assertIsDisplayed()
        composeRule.onNodeWithTag("dice_zone_odd").assertDoesNotExist()
        composeRule.onNodeWithTag("dice_zone_even").assertDoesNotExist()
        composeRule.onNodeWithTag("dice_chip_10").assertIsDisplayed()
        composeRule.onNodeWithTag("dice_zone_low").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dice_roll_button").assertIsDisplayed()
    }

    @Test
    fun `register deposit spin and withdraw flow updates Room state`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, Luck780Database::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = Luck780Repository(
                accountDao = db.accountDao(),
                bankAccountDao = db.bankAccountDao(),
                transactionDao = db.transactionDao(),
                mailDao = db.mailDao(),
                preferencesManager = PreferencesManager(context)
            )

            val reg = repo.register("+92", "3019998877", "pass123", "pass123", "Zain", true)
            assertTrue(reg is AuthResult.Success)
            val phoneKey = (reg as AuthResult.Success).account.phoneKey

            val dep = repo.deposit(phoneKey, 100L, 10L, "JazzCash")
            assertTrue(dep is OperationResult.Success)

            val spin = repo.applySpinWheelResult(phoneKey, SpinReward.CashCoins(50L), usedCoinFallback = false)
            assertTrue(spin is OperationResult.Success)

            val updated = db.accountDao().getAccount(phoneKey)!!
            // 888 starter + 110 deposit + 50 spin = 1048 coins
            assertEquals(1048L, updated.coins)

            // Admin coin adjustment & broadcast mail
            val adj = repo.adjustUserCoins(phoneKey, 250L, "VIP Support Bonus")
            assertTrue(adj is OperationResult.Success)
            assertEquals(1298L, db.accountDao().getAccount(phoneKey)!!.coins)

            val mailRes = repo.broadcastSystemMail("Weekend Carnival", "Double rewards live!", 100L)
            assertTrue(mailRes is OperationResult.Success)
            val mails = db.mailDao().observeMail(phoneKey).first()
            assertTrue(mails.any { it.subject == "Weekend Carnival" && it.rewardCoins == 100L })

            val vipRes = repo.setUserVipLevel(phoneKey, 3)
            assertTrue(vipRes is OperationResult.Success)
            assertEquals(3, db.accountDao().getAccount(phoneKey)!!.vipLevel)
        } finally {
            db.close()
        }
    }

    @Test
    fun `demo login succeeds immediately on fresh database without seeding race`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, Luck780Database::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val prefs = PreferencesManager(context)
            prefs.clearAll()
            val initialCreds = prefs.savedCredentialsFlow.first()
            assertEquals("3001234567", initialCreds.phoneNumber)
            assertEquals("luck780", initialCreds.password)

            val repo = Luck780Repository(
                accountDao = db.accountDao(),
                bankAccountDao = db.bankAccountDao(),
                transactionDao = db.transactionDao(),
                mailDao = db.mailDao(),
                preferencesManager = prefs,
                referralDao = db.referralDao()
            )
            val loginResult = repo.login("+92", "3001234567", "luck780", savePassword = true)
            assertTrue(loginResult is AuthResult.Success)
            assertEquals("LuckyAce780", (loginResult as AuthResult.Success).account.username)
        } finally {
            db.close()
        }
    }

    @Test
    fun `referral install links parse cleanly and credit both referrer and referee end-to-end`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, Luck780Database::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            // Verify HTTPS install link, deep link, and Play referrer parsing
            val httpsLink = ReferralLinkHelper.buildInstallReferenceUrl("luck780vip")
            assertEquals("LUCK780VIP", ReferralLinkHelper.extractReferralCodeFromInput(httpsLink))
            assertEquals(
                "LUCK9921A",
                ReferralLinkHelper.extractReferralCodeFromInput("luck780://invite?ref=luck9921a")
            )
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(httpsLink))
            assertEquals("LUCK780VIP", ReferralLinkHelper.extractReferralCodeFromIntent(intent))

            val repo = Luck780Repository(
                accountDao = db.accountDao(),
                bankAccountDao = db.bankAccountDao(),
                transactionDao = db.transactionDao(),
                mailDao = db.mailDao(),
                preferencesManager = PreferencesManager(context),
                referralDao = db.referralDao()
            )
            repo.ensureDemoAccountExists()
            val demoBefore = db.accountDao().getAccount("+92_3001234567")!!

            // New player registers using the full HTTPS install reference link
            val reg = repo.register(
                countryCode = "+92",
                rawPhone = "3215556677",
                password = "pass1234",
                confirmPassword = "pass1234",
                username = "AliInvited",
                savePassword = true,
                referralInput = httpsLink
            )
            assertTrue(reg is AuthResult.Success)
            val newAccount = (reg as AuthResult.Success).account
            // 888 starter + 100 referral bonus = 988 Coins, plus unique own referral code
            assertEquals(988L, newAccount.coins)
            assertEquals("LUCK780VIP", newAccount.referredByCode)
            assertTrue(newAccount.referralCode != "LUCK780VIP")

            // Referrer gets +150 Coins, +2 Spin Tickets, referralCount=1, and referral record
            val demoAfter = db.accountDao().getAccount("+92_3001234567")!!
            assertEquals(demoBefore.coins + 150L, demoAfter.coins)
            assertEquals(demoBefore.bonusTickets + 2, demoAfter.bonusTickets)
            assertEquals(1, demoAfter.referralCount)
            assertEquals(150L, demoAfter.referralEarnedCoins)

            val records = repo.observeReferrals("+92_3001234567").first()
            assertEquals(1, records.size)
            assertEquals("AliInvited", records.first().refereeName)

            // Cross-device install link code (not yet in local DB) still credits +100 join bonus
            val extReg = repo.register(
                countryCode = "+92",
                rawPhone = "3334445566",
                password = "pass1234",
                confirmPassword = "pass1234",
                username = "RemoteFriend",
                savePassword = true,
                referralInput = "https://ais-pre-o4t4gzkrblqyegunldrwer-326445642568.asia-east1.run.app/invite?ref=EXT9988Z"
            )
            assertTrue(extReg is AuthResult.Success)
            assertEquals(988L, (extReg as AuthResult.Success).account.coins)
            assertEquals("EXT9988Z", extReg.account.referredByCode)
        } finally {
            db.close()
        }
    }

    @Test
    fun `Promote Reference System dialog displays unique link and actions`() {
        val account = AccountEntity(
            phoneKey = "+92_3001234567",
            countryCode = "+92",
            phoneNumber = "3001234567",
            passwordHash = "secret",
            username = "LuckyAce780",
            playerUid = "78084291",
            referralCode = "LUCK780VIP",
            referralCount = 2,
            referralEarnedCoins = 300L
        )
        composeRule.setContent {
            Luck780Theme {
                EventAndPromoteDialog(
                    isPromote = true,
                    account = account,
                    onDismiss = {},
                    onOpenSpinWheel = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("referral_code_text").assertIsDisplayed()
        composeRule.onNodeWithTag("referral_install_link_text").assertIsDisplayed()
        composeRule.onNodeWithTag("copy_referral_link_button").assertIsDisplayed()
        composeRule.onNodeWithTag("share_referral_link_button").assertIsDisplayed()
        composeRule.onNodeWithTag("claim_referral_input").performScrollTo().assertIsDisplayed()
    }
}
