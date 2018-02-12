package com.loic.morseapp.manager.vibrator

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
            vibrator.vibrate(VibrationEffect.createOneShot(PlayerController.TIME_LENGTH * 3, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(PlayerController.TIME_LENGTH * 3)
        }
    }

    override fun switchOff() {
        vibrator.cancel()
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished() {
        vibrator.cancel()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}


    // TODO check if device has vibrator
}