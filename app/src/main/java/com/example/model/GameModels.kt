package com.example.model

data class CountryCodeOption(
    val code: String,
    val country: String,
    val flagEmoji: String
)

val COUNTRY_CODES = listOf(
    CountryCodeOption("+92", "Pakistan", "\uD83C\uDDF5\uD83C\uDDF0"),
    CountryCodeOption("+971", "UAE", "\uD83C\uDDE6\uD83C\uDDEA"),
    CountryCodeOption("+966", "Saudi Arabia", "\uD83C\uDDF8\uD83C\uDDE6"),
    CountryCodeOption("+44", "United Kingdom", "\uD83C\uDDEC\uD83C\uDDE7"),
    CountryCodeOption("+1", "USA / Canada", "\uD83C\uDDFA\uD83C\uDDF8"),
    CountryCodeOption("+91", "India", "\uD83C\uDDEE\uD83C\uDDF3")
)

sealed interface SpinReward {
    data class CashCoins(val amount: Long) : SpinReward
    data class MultiplierBoost(val factor: Int) : SpinReward
    data class BonusTicket(val count: Int) : SpinReward
}

data class WheelSegment(
    val label: String,
    val subLabel: String,
    val reward: SpinReward,
    val colorHex: Long
)

val SPIN_WHEEL_SEGMENTS = listOf(
    WheelSegment("25", "Coins", SpinReward.CashCoins(25), 0xFFFF2E54),
    WheelSegment("x2", "Boost", SpinReward.MultiplierBoost(2), 0xFF7C3AED),
    WheelSegment("50", "Coins", SpinReward.CashCoins(50), 0xFFFF9F00),
    WheelSegment("+1", "Ticket", SpinReward.BonusTicket(1), 0xFF0EA5E9),
    WheelSegment("5", "Coins", SpinReward.CashCoins(5), 0xFF10B981),
    WheelSegment("x5", "Boost", SpinReward.MultiplierBoost(5), 0xFFD946EF),
    WheelSegment("100", "Jackpot", SpinReward.CashCoins(100), 0xFFE11D48),
    WheelSegment("15", "Coins", SpinReward.CashCoins(15), 0xFFF59E0B)
)

data class DepositPackage(
    val id: String,
    val amountRs: Long,
    val bonusRs: Long,
    val coinsCredited: Long,
    val badge: String? = null
)

val DEPOSIT_PACKAGES = listOf(
    DepositPackage("pkg_100", 100, 10, 110, "+Rs10 Extra"),
    DepositPackage("pkg_300", 300, 35, 335, "+Rs35 Bonus"),
    DepositPackage("pkg_500", 500, 65, 565, "Popular"),
    DepositPackage("pkg_1000", 1000, 150, 1150, "+15% Boost"),
    DepositPackage("pkg_3000", 3000, 500, 3500, "VIP Pick"),
    DepositPackage("pkg_5000", 5000, 900, 5900, "Best Value")
)

data class VipTier(
    val level: Int,
    val name: String,
    val minPoints: Long,
    val dailyBonusCoins: Long,
    val withdrawLimitRs: Long,
    val perkSummary: String
)

val VIP_TIERS = listOf(
    VipTier(1, "Bronze Spark", 0, 25, 10_000, "Daily check-in boost & standard fast payouts"),
    VipTier(2, "Silver Frost", 500, 50, 25_000, "+5% deposit bonus & 1 weekly free spin ticket"),
    VipTier(3, "Crimson Gold", 1500, 100, 50_000, "+10% cashback on table games & priority queue"),
    VipTier(4, "Royal Sapphire", 3500, 200, 100_000, "+15% VIP bonus & instant wallet settlement"),
    VipTier(5, "Diamond Legend", 7500, 500, 250_000, "Dedicated AI VIP Concierge & exclusive 5x events")
)

data class LeaderboardPlayer(
    val rank: Int,
    val playerName: String,
    val playerId: String,
    val vipLevel: Int,
    val totalWinnings: Long,
    val isCurrentUser: Boolean = false
)

val SAMPLE_TOP_WINNERS = listOf(
    LeaderboardPlayer(1, "Zainab_Royale", "78010294", 5, 148500),
    LeaderboardPlayer(2, "HamzaKing77", "78044812", 5, 119200),
    LeaderboardPlayer(3, "AyeshaStar", "78029103", 4, 94750),
    LeaderboardPlayer(4, "BilalAce92", "78067321", 4, 72400),
    LeaderboardPlayer(5, "UsmanDragon", "78051908", 3, 56100)
)
