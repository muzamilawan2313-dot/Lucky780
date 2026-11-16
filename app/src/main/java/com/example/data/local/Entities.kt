package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val phoneKey: String,
    val countryCode: String,
    val phoneNumber: String,
    val passwordHash: String,
    val username: String,
    val playerUid: String,
    val vipLevel: Int = 2,
    val vipPoints: Long = 650L,
    val coins: Long = 1250L,
    val freeCashProgress: Long = 980L,
    val bonusTickets: Int = 3,
    val activeMultiplier: Int = 1,
    val lastCheckInDay: Int = 2,
    val lastCheckInEpochDay: Long = 0L,
    val isBound: Boolean = false,
    val giftClaimed: Boolean = false,
    val referralCode: String = "LUCK780VIP",
    val referredByCode: String? = null,
    val referralCount: Int = 0,
    val referralEarnedCoins: Long = 0L
)

@Entity(tableName = "referral_records")
data class ReferralRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerPhoneKey: String,
    val refereePhoneKey: String,
    val refereeName: String,
    val referralCodeUsed: String,
    val referrerBonusCoins: Long,
    val referrerBonusTickets: Int,
    val refereeBonusCoins: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerPhoneKey: String,
    val providerType: String,
    val accountTitle: String,
    val accountNumber: String,
    val bankName: String,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerPhoneKey: String,
    val type: String,
    val title: String,
    val amount: Long,
    val bonusAmount: Long = 0L,
    val status: String = "Completed",
    val method: String = "Wallet",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mail_messages")
data class MailMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerPhoneKey: String,
    val subject: String,
    val body: String,
    val rewardCoins: Long = 0L,
    val isClaimed: Boolean = false,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
