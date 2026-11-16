package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE phoneKey = :phoneKey LIMIT 1")
    fun observeAccount(phoneKey: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts ORDER BY vipLevel DESC, coins DESC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsOnce(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE phoneKey = :phoneKey LIMIT 1")
    suspend fun getAccount(phoneKey: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE UPPER(referralCode) = UPPER(:referralCode) LIMIT 1")
    suspend fun getAccountByReferralCode(referralCode: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)
}

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts WHERE ownerPhoneKey = :phoneKey ORDER BY isPrimary DESC, createdAt DESC")
    fun observeBankAccounts(phoneKey: String): Flow<List<BankAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(bankAccount: BankAccountEntity)

    @Query("UPDATE bank_accounts SET isPrimary = (id = :accountId) WHERE ownerPhoneKey = :phoneKey")
    suspend fun setPrimaryBankAccount(phoneKey: String, accountId: Long)

    @Query("DELETE FROM bank_accounts WHERE id = :accountId")
    suspend fun deleteBankAccount(accountId: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE ownerPhoneKey = :phoneKey ORDER BY timestamp DESC")
    fun observeTransactions(phoneKey: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET status = :newStatus WHERE id = :txId")
    suspend fun updateTransactionStatus(txId: Long, newStatus: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface MailDao {
    @Query("SELECT * FROM mail_messages WHERE ownerPhoneKey = :phoneKey ORDER BY timestamp DESC")
    fun observeMail(phoneKey: String): Flow<List<MailMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMail(mail: MailMessageEntity)

    @Query("UPDATE mail_messages SET isRead = 1 WHERE id = :mailId")
    suspend fun markRead(mailId: Long)

    @Query("UPDATE mail_messages SET isRead = 1, isClaimed = 1 WHERE id = :mailId")
    suspend fun markClaimed(mailId: Long)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referral_records WHERE referrerPhoneKey = :referrerPhoneKey ORDER BY createdAt DESC")
    fun observeReferrals(referrerPhoneKey: String): Flow<List<ReferralRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(record: ReferralRecordEntity)
}
