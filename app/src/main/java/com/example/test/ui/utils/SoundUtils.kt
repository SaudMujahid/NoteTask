package com.example.test.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager

/**
 * Plays a short, low-volume notification sound to confirm a task check-off.
 * Uses the device's default notification sound at 40% volume.
 * Silently no-ops if audio is unavailable (e.g. silent mode, missing URI).
 */
fun playCheckSound(context: Context) {
    try {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val mp = MediaPlayer()
        mp.setAudioAttributes(attrs)
        mp.setDataSource(context, uri)
        mp.setVolume(0.4f, 0.4f)
        mp.setOnPreparedListener { it.start() }
        mp.setOnCompletionListener { it.release() }
        mp.prepareAsync()
    } catch (_: Exception) { /* silent fail */ }
}