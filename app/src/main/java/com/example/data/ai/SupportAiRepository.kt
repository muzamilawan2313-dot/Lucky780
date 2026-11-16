package com.example.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupportAiRepository {
    private val systemPrompt = content {
        text(
            """
            You are the official Luck780 VIP Concierge & Game Strategy Advisor.
            Help users with:
            1. Explaining rules, odds, and strategies for Luck780 mini-games: Dragon Tiger Fight, Roulette Zoo, Goal Jackpot, Fortune Gems, 7up7down, Rocket, Thunder Fire, 9 Coins, Starlight Princess, and Car Roulette.
            2. Explaining how the Lucky Spin Wheel (5/15/25/50/100 coins, x2/x5 multipliers, +1 bonus tickets) and the 1,333 Free Cash milestone work.
            3. Guiding users on Deposits (+Rs10/+Rs35/+Rs65 bonuses), Withdrawals, Binding JazzCash/Easypaisa/Bank accounts, VIP tiers (1-5), Check-ins, and Gift codes (LUCK780, WINTER780, VIPBONUS).
            Keep answers clear, structured, helpful, and concise. Remind players to play responsibly with virtual coins.
            """.trimIndent()
        )
    }

    // Required model: gemini-4-carbon with ThinkingLevel.HIGH and NO maxOutputTokens
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-carbon",
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig {
                    thinkingLevel = ThinkingLevel.HIGH
                }
            },
            systemInstruction = systemPrompt
        )
    }

    fun streamAdvice(userQuery: String, contextSummary: String): Flow<String> {
        val enrichedPrompt = """
            Player Account Context:
            $contextSummary

            Player Question:
            $userQuery
        """.trimIndent()

        return model.generateContentStream(enrichedPrompt).map { chunk ->
            chunk.text.orEmpty()
        }
    }
}
