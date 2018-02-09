package com.loic.morseapp.morseconverter.vibrator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi
import com.loic.morseapp.player.PlayerController
import com.loic.morseapp.player.PlayerListener

/**
 * Class used to manage the vibrator
 */
class VibratorManager(context: Context) : PlayerListener {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @RequiresApi(Build.VERSION_CODES.O)
    override fun switchOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(PlayerController.TIME_LENGH, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(PlayerController.TIME_LENGH)
        }
    }

    override fun switchOff() {
        vibrator.cancel()
    }

    override fun playerStarted() {
    }

    override fun playerFinished() {
        vibrator.cancel()
    }

    override fun notifyProgress(progress: Float, letterIndex: Int) {
    }


    // TODO check if device has vibrator
}