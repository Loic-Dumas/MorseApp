package com.loic.morseapp.controller

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi

/**
 * Class used to manage the vibrator
 */
class VibratorController(context: Context) : MorsePlayerListenerInterface {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @RequiresApi(Build.VERSION_CODES.O)
    override fun switchOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(MorsePlayer.TIME_LENGTH * 3, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
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


    // TODO check if device has vibrator
}