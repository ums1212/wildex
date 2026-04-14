package dev.comon.wildex.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.staticCompositionLocalOf
import dev.comon.wildex.R

class BgmManager {
    private var player: MediaPlayer? = null
    private var currentIsDark: Boolean? = null
    private var isPaused: Boolean = false

    fun play(context: Context, isDarkTheme: Boolean) {
        if (player != null && currentIsDark == isDarkTheme) {
            if (isPaused) resume()
            return
        }
        releasePlayer()
        val rawRes = if (isDarkTheme) R.raw.pixel_pines else R.raw.pine_circuitry
        player = MediaPlayer.create(context.applicationContext, rawRes)?.apply {
            isLooping = true
            start()
        }
        currentIsDark = isDarkTheme
        isPaused = false
    }

    fun switchTheme(context: Context, isDarkTheme: Boolean) {
        if (currentIsDark == isDarkTheme) return
        val wasPlaying = player?.isPlaying == true || isPaused
        releasePlayer()
        if (wasPlaying) {
            val rawRes = if (isDarkTheme) R.raw.pixel_pines else R.raw.pine_circuitry
            player = MediaPlayer.create(context.applicationContext, rawRes)?.apply {
                isLooping = true
                start()
            }
            currentIsDark = isDarkTheme
            isPaused = false
        } else {
            currentIsDark = isDarkTheme
        }
    }

    fun pause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
            }
        }
    }

    fun resume() {
        player?.let {
            if (isPaused) {
                it.start()
                isPaused = false
            }
        }
    }

    fun setVolume(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        player?.setVolume(clamped, clamped)
    }

    fun stop() {
        releasePlayer()
        currentIsDark = null
        isPaused = false
    }

    private fun releasePlayer() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}

val LocalBgmManager = staticCompositionLocalOf<BgmManager> {
    error("BgmManager가 제공되지 않았습니다.")
}
