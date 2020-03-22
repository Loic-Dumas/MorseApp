package com.loic.morseapp.morseplayer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi

/**
 * [MorseOutputPlayerInterface] implementation for the vibration.
 */
class MorseVibrationPlayer(context: Context) : MorseOutputPlayerInterface {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @RequiresApi(Build.VERSION_CODES.O)
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