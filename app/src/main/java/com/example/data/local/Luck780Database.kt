package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        ReferralRecordEntity::class,
        BankAccountEntity::class,
        TransactionEntity::class,
        MailMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class Luck780Database : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun referralDao(): ReferralDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun mailDao(): MailDao

    companion object {
        @Volatile
        private var INSTANCE: Luck780Database? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN referredByCode TEXT")
                db.execSQL("ALTER TABLE accounts ADD COLUMN referralCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN referralEarnedCoins INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS referral_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        referrerPhoneKey TEXT NOT NULL,
                        refereePhoneKey TEXT NOT NULL,
                        refereeName TEXT NOT NULL,
                        referralCodeUsed TEXT NOT NULL,
                        referrerBonusCoins INTEGER NOT NULL,
                        referrerBonusTickets INTEGER NOT NULL,
                        refereeBonusCoins INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): Luck780Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Luck780Database::class.java,
                    "luck780_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
