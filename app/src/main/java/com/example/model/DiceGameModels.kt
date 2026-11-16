package com.example.model

import kotlin.random.Random

enum class DiceBetZone(
    val id: String,
    val label: String,
    val oddsLabel: String,
    val multiplier: Int
) {
    LOW_2_6("low", "2 - 6", "1:1", 2),
    EXACT_7("seven", "7", "1:4", 5),
    HIGH_8_12("high", "8 - 12", "1:1", 2);

    fun matches(sum: Int): Boolean = when (this) {
        LOW_2_6 -> sum in 2..6
        EXACT_7 -> sum == 7
        HIGH_8_12 -> sum in 8..12
    }
}

data class DiceHistoryEntry(
    val sum: Int,
    val hasLightning2x: Boolean = false
)

data class DiceTableSeat(
    val id: String,
    val displayName: String,
    val cosmeticTitle: String?,
    val balanceText: String,
    val balanceValue: Long,
    val avatarColorHex: Long,
    val isLeftSide: Boolean
)

data class DiceRoundOutcome(
    val die1: Int,
    val die2: Int,
    val totalBet: Long,
    val grossPayout: Long,
    val lightningZone: DiceBetZone?,
    val winningZones: List<DiceBetZone>
) {
    val sum: Int get() = die1 + die2
    val hitLightning: Boolean get() = lightningZone != null && winningZones.contains(lightningZone)
}

object DiceRngEngine {
    const val MIN_ENTRY_COINS = 10L

    val CHIP_DENOMINATIONS = listOf(10L, 50L, 100L, 500L, 1000L, 5000L)

    val INITIAL_HISTORY = listOf(
        DiceHistoryEntry(10, false),
        DiceHistoryEntry(6, false),
        DiceHistoryEntry(8, false),
        DiceHistoryEntry(7, false),
        DiceHistoryEntry(4, true),
        DiceHistoryEntry(8, true),
        DiceHistoryEntry(6, false),
        DiceHistoryEntry(7, false),
        DiceHistoryEntry(7, true),
        DiceHistoryEntry(3, false),
        DiceHistoryEntry(12, false),
        DiceHistoryEntry(9, true)
    )

    val DEFAULT_NPC_SEATS = listOf(
        DiceTableSeat("seat_1", "name415955", "Richie Rich", "67,716.19", 67716L, 0xFFDB2777, isLeftSide = true),
        DiceTableSeat("seat_2", "name594179", "Millionaire", "56,857.19", 56857L, 0xFF2563EB, isLeftSide = true),
        DiceTableSeat("seat_3", "name892914", null, "30,499.20", 30499L, 0xFF7C3AED, isLeftSide = true),
        DiceTableSeat("seat_4", "name956012", "Human Calculator", "64,260.65", 64260L, 0xFFF59E0B, isLeftSide = false),
        DiceTableSeat("seat_5", "name949496", null, "33,160.35", 33160L, 0xFFEC4899, isLeftSide = false),
        DiceTableSeat("seat_6", "name456279", null, "30,503.42", 30503L, 0xFF0EA5E9, isLeftSide = false)
    )

    val HOW_TO_PLAY_STEPS = listOf(
        "Players Ka Table: Six players flank both sides of the golden oval table with avatars, balances, and cosmetic titles (Richie Rich, Millionaire, Human Calculator).",
        "History Bar: Top tiles track past roll sums (Red = 2-6, Blue = 7, Teal = 8-12, ⚡ = 2X Lightning Boost). Each new roll is independent RNG.",
        "Dice Dome & Countdown: Watch the golden timer count down, then the twin dice roll smoothly at a calm pace inside the glass dome.",
        "3 Betting Steps: Place chips on 2 - 6 (1:1), Lucky 7 (1:4 in the top-center), or 8 - 12 (1:1). Use REPEAT to re-bet your last round.",
        "2X Lightning Payout: Random lightning strikes boost one of the 3 winning steps by 2X for extra coin rewards.",
        "Minimum Entry & Watching Mode: You need at least 10 coins to play the game. With fewer than 10 coins, you enter WATCHING THE BATTLE mode."
    )

    const val RNG_WARNING_NOTICE =
        "Ek Zaroori Tanbih: Yeh purely chance-based (RNG) game hai. Koi fixed strategy guaranteed win nahi deti. Apna budget set karein aur responsibly khelein."

    fun rollRound(
        bets: Map<DiceBetZone, Long>,
        forcedLightningZone: DiceBetZone? = null,
        random: Random = Random.Default
    ): DiceRoundOutcome {
        val d1 = random.nextInt(1, 7)
        val d2 = random.nextInt(1, 7)
        val lightning = forcedLightningZone ?: if (random.nextInt(100) < 40) {
            DiceBetZone.entries.random(random)
        } else {
            null
        }
        return evaluateRoll(d1, d2, bets, lightning)
    }

    fun evaluateRoll(
        die1: Int,
        die2: Int,
        bets: Map<DiceBetZone, Long>,
        lightningZone: DiceBetZone? = null
    ): DiceRoundOutcome {
        val sum = die1 + die2
        val winners = DiceBetZone.entries.filter { it.matches(sum) }
        val totalBet = bets.values.sum()
        val grossPayout = bets.entries.sumOf { (zone, stake) ->
            if (zone.matches(sum)) {
                val boost = if (lightningZone == zone) 2 else 1
                stake * zone.multiplier * boost
            } else {
                0L
            }
        }
        return DiceRoundOutcome(
            die1 = die1,
            die2 = die2,
            totalBet = totalBet,
            grossPayout = grossPayout,
            lightningZone = lightningZone,
            winningZones = winners
        )
    }

    fun formatChipValue(value: Long): String = when {
        value >= 1000L -> "${value / 1000}K"
        else -> value.toString()
    }
}
