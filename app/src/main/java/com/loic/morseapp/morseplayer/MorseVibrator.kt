package com.loic.morseapp.morseplayer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

interface MorseVibrator {
    fun start()
    fun stop()
}

@RequiresApi(Build.VERSION_CODES.S)
class ModernMorseVibrator(context: Context) : MorseVibrator {

    private val _vibrator by lazy { (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator }

    override fun start() {
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), 0)
        _vibrator.vibrate(effect)
    }

    override fun stop() {
        _vibrator.cancel()
    }
}

class LegacyMorseVibrator(context: Context) : MorseVibrator {
    private val _vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    override fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _vibrator.vibrate(VibrationEffect.createOneShot(MorsePlayer.TIME_LENGTH * 3, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            _vibrator.vibrate(MorsePlayer.TIME_LENGTH * 3)
        }
    }

    override fun stop() {
        _vibrator.cancel()
    }
}
