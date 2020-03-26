package com.loic.morseapp.morseplayer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * [MorsePlayer.MorseOutputPlayer] implementation for the vibration.
 */
class MorseVibrationPlayer(context: Context) : MorsePlayer.MorseOutputPlayer {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    override fun onPlayerAdded() {
    }

    override fun onPlayerRemoved() {
    }

    override fun switchOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(MorsePlayer.TIME_LENGTH * 3, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(MorsePlayer.TIME_LENGTH * 3)
        }
    }

    override fun switchOff() {
        vibrator.cancel()
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished() {
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}

}