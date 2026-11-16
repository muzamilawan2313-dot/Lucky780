package com.example.referral

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLDecoder

object ReferralLinkHelper {
    const val SHARED_WEB_BASE_URL =
        "https://ais-pre-o4t4gzkrblqyegunldrwer-326445642568.asia-east1.run.app"
    const val DEEP_LINK_SCHEME = "luck780"
    const val REFERRER_BONUS_COINS = 150L
    const val REFERRER_BONUS_TICKETS = 2
    const val REFEREE_BONUS_COINS = 100L

    fun normalizeCode(raw: String): String =
        raw.trim().uppercase().filter { it.isLetterOrDigit() }.take(20)

    fun generateUniqueReferralCode(phoneKey: String, playerUid: String): String {
        val uidSuffix = playerUid.filter { it.isLetterOrDigit() }.takeLast(5)
        val checkChar = ('A' + (phoneKey.hashCode().ushr(1) % 26))
        return "LUCK$uidSuffix$checkChar"
    }

    fun buildInstallReferenceUrl(referralCode: String): String {
        val code = normalizeCode(referralCode).ifBlank { "LUCK780VIP" }
        return "$SHARED_WEB_BASE_URL/invite?ref=$code"
    }

    fun buildAppDeepLink(referralCode: String): String {
        val code = normalizeCode(referralCode).ifBlank { "LUCK780VIP" }
        return "$DEEP_LINK_SCHEME://invite?ref=$code"
    }

    fun buildShareInviteMessage(username: String, referralCode: String): String {
        val code = normalizeCode(referralCode).ifBlank { "LUCK780VIP" }
        val installUrl = buildInstallReferenceUrl(code)
        val deepLink = buildAppDeepLink(code)
        return buildString {
            append("$username invited you to Luck780 Winter Carnival & Dice Battle!\n\n")
            append("Install & join using my Reference Link to get +100 Extra Bonus Coins (988 Starter Coins!):\n")
            append("$installUrl\n\n")
            append("Direct App Link: $deepLink\n")
            append("Reference Code: $code")
        }
    }

    fun shareInstallLink(context: Context, username: String, referralCode: String) {
        val message = buildShareInviteMessage(username, referralCode)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join Luck780 with my Reference Link (+100 Bonus Coins)")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Luck780 Install Reference Link").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun extractReferralCodeFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        val dataCode = extractReferralCodeFromInput(intent.dataString)
        if (!dataCode.isNullOrBlank()) return dataCode

        val extraRef = intent.getStringExtra("ref")
            ?: intent.getStringExtra("referral_code")
            ?: intent.getStringExtra("referrer")
        return extractReferralCodeFromInput(extraRef)
    }

    /**
     * Accepts either a full URL (https://.../invite?ref=CODE, luck780://invite?ref=CODE,
     * Play Store ?referrer=ref%3DCODE) or a plain code like LUCK780VIP.
     */
    fun extractReferralCodeFromInput(rawInput: String?): String? {
        if (rawInput.isNullOrBlank()) return null
        val trimmed = rawInput.trim()

        if (trimmed.contains("://") || trimmed.contains("?") || trimmed.contains("referrer=") || trimmed.contains("ref=")) {
            val uri = runCatching { Uri.parse(trimmed) }.getOrNull()
            if (uri != null) {
                val directParam = uri.getQueryParameter("ref")
                    ?: uri.getQueryParameter("code")
                    ?: uri.getQueryParameter("referral")
                if (!directParam.isNullOrBlank()) {
                    return normalizeCode(directParam).ifBlank { null }
                }

                val referrerParam = uri.getQueryParameter("referrer")
                if (!referrerParam.isNullOrBlank()) {
                    val decoded = runCatching { URLDecoder.decode(referrerParam, "UTF-8") }.getOrDefault(referrerParam)
                    val nestedRef = extractQueryKey(decoded, "ref")
                        ?: extractQueryKey(decoded, "code")
                        ?: decoded
                    val normalized = normalizeCode(nestedRef)
                    if (normalized.length >= 4) return normalized
                }

                val lastSeg = uri.lastPathSegment
                if (!lastSeg.isNullOrBlank() && lastSeg.lowercase() !in setOf("invite", "join", "app", "details")) {
                    val normalized = normalizeCode(lastSeg)
                    if (normalized.length >= 4) return normalized
                }
            }

            val fallbackMatch = Regex("(?:ref|code|referral)=([A-Za-z0-9]+)", RegexOption.IGNORE_CASE)
                .find(trimmed)
                ?.groupValues
                ?.getOrNull(1)
            if (!fallbackMatch.isNullOrBlank()) {
                return normalizeCode(fallbackMatch).ifBlank { null }
            }
            return null
        }

        val normalized = normalizeCode(trimmed)
        return if (normalized.length >= 4) normalized else null
    }

    private fun extractQueryKey(query: String, key: String): String? {
        val regex = Regex("(?:^|&)${Regex.escape(key)}=([^&]+)", RegexOption.IGNORE_CASE)
        return regex.find(query)?.groupValues?.getOrNull(1)
    }
}
