package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class DiceSoundEngine {
    private val executor = Executors.newFixedThreadPool(4)
    private val inFlightTracks = AtomicInteger(0)

    @Volatile
    var enabled: Boolean = true

    private val chipTapPcm: ShortArray by lazy {
        synthesizeTone(
            durationMs = 38,
            freqStartHz = 1480.0,
            freqEndHz = 960.0,
            amplitude = 0.35,
            harmonicRatio = 0.25,
            decayRate = 11.0
        )
    }

    private val diceClackPcms: Array<ShortArray> by lazy {
        Array(4) { idx ->
            val baseFreq = 260.0 + idx * 55.0
            synthesizeDiceClack(durationMs = 46, baseFreqHz = baseFreq)
        }
    }

    private val tickNormalPcm: ShortArray by lazy {
        synthesizeTone(
            durationMs = 24,
            freqStartHz = 860.0,
            freqEndHz = 740.0,
            amplitude = 0.20,
            harmonicRatio = 0.1,
            decayRate = 14.0
        )
    }

    private val tickUrgentPcm: ShortArray by lazy {
        synthesizeTone(
            durationMs = 26,
            freqStartHz = 1180.0,
            freqEndHz = 980.0,
            amplitude = 0.24,
            harmonicRatio = 0.1,
            decayRate = 14.0
        )
    }

    private val winNormalPcm: ShortArray by lazy { buildWinChimePcm(hitLightning = false) }
    private val winLightningPcm: ShortArray by lazy { buildWinChimePcm(hitLightning = true) }

    private val settlePcm: ShortArray by lazy {
        synthesizeTone(
            durationMs = 90,
            freqStartHz = 440.0,
            freqEndHz = 330.0,
            amplitude = 0.22,
            harmonicRatio = 0.15,
            decayRate = 6.5
        )
    }

    fun playChipTap() {
        if (!enabled || inFlightTracks.get() >= 3) return
        playAsync { writePcm(chipTapPcm) }
    }

    fun playDiceTumbleStep(stepIndex: Int) {
        if (!enabled || inFlightTracks.get() >= 3) return
        val pcm = diceClackPcms[stepIndex and 3]
        playAsync { writePcm(pcm) }
    }

    fun playTimerTick(urgent: Boolean) {
        if (!enabled || inFlightTracks.get() >= 3) return
        playAsync { writePcm(if (urgent) tickUrgentPcm else tickNormalPcm) }
    }

    fun playWinChime(hitLightning: Boolean) {
        if (!enabled) return
        playAsync { writePcm(if (hitLightning) winLightningPcm else winNormalPcm) }
    }

    fun playRoundSettle() {
        if (!enabled || inFlightTracks.get() >= 3) return
        playAsync { writePcm(settlePcm) }
    }

    fun release() {
        runCatching { executor.shutdownNow() }
    }

    private fun playAsync(block: () -> Unit) {
        runCatching {
            executor.execute {
                inFlightTracks.incrementAndGet()
                try {
                    runCatching { block() }
                } finally {
                    inFlightTracks.decrementAndGet()
                }
            }
        }
    }

    private fun buildWinChimePcm(hitLightning: Boolean): ShortArray {
        val notes = if (hitLightning) {
            listOf(587.33, 739.99, 880.0, 1174.66)
        } else {
            listOf(523.25, 659.25, 783.99, 1046.5)
        }
        val noteSamples = (SAMPLE_RATE * 0.072).toInt()
        val combined = ShortArray(noteSamples * notes.size)
        notes.forEachIndexed { idx, freq ->
            val tone = synthesizeTone(
                durationMs = 72,
                freqStartHz = freq,
                freqEndHz = freq * 1.015,
                amplitude = 0.38,
                harmonicRatio = 0.3,
                decayRate = 4.5
            )
            val offset = idx * noteSamples
            val copyCount = minOf(tone.size, combined.size - offset)
            if (copyCount > 0) {
                System.arraycopy(tone, 0, combined, offset, copyCount)
            }
        }
        return combined
    }

    private fun synthesizeTone(
        durationMs: Int,
        freqStartHz: Double,
        freqEndHz: Double,
        amplitude: Double,
        harmonicRatio: Double,
        decayRate: Double
    ): ShortArray {
        val sampleCount = ((SAMPLE_RATE * durationMs) / 1000.0).toInt().coerceAtLeast(1)
        val buffer = ShortArray(sampleCount)
        var phase = 0.0
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount
            val freq = freqStartHz + (freqEndHz - freqStartHz) * t
            phase += (2.0 * PI * freq) / SAMPLE_RATE
            val env = exp(-decayRate * t)
            val raw = sin(phase) + harmonicRatio * sin(phase * 2.0)
            val sample = (raw * env * amplitude * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun synthesizeDiceClack(durationMs: Int, baseFreqHz: Double): ShortArray {
        val sampleCount = ((SAMPLE_RATE * durationMs) / 1000.0).toInt().coerceAtLeast(1)
        val buffer = ShortArray(sampleCount)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount
            phase1 += (2.0 * PI * baseFreqHz) / SAMPLE_RATE
            phase2 += (2.0 * PI * (baseFreqHz * 2.37)) / SAMPLE_RATE
            val env = exp(-10.5 * t)
            val wave = 0.65 * sin(phase1) + 0.35 * sin(phase2)
            val sample = (wave * env * 0.42 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun writePcm(samples: ShortArray) {
        if (samples.isEmpty()) return
        val byteCount = samples.size * 2
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) return

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(maxOf(byteCount, minBuf))
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            val waitMs = ((samples.size * 1000L) / SAMPLE_RATE) + 12L
            Thread.sleep(waitMs)
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }

    companion object {
        private const val SAMPLE_RATE = 22050
    }
}
