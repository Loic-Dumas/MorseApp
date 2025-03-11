package com.loic.morseapp.morseplayer

import android.content.Context
import android.os.Build

/**
 * [MorsePlayer.MorseOutputPlayer] implementation for the vibration.
 */
class MorseVibrationPlayer(context: Context) : MorsePlayer.MorseOutputPlayer {

    private val vibrator: MorseVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ModernMorseVibrator(context)
    } else {
        LegacyMorseVibrator(context)
    }

    override fun onPlayerAdded() {
    }

    override fun onPlayerRemoved() {
    }

    override fun switchOn() {
        vibrator.start()
    }

    override fun switchOff() {
        vibrator.stop()
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished(morseCodeFullyPlayed: Boolean) {
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}
}
