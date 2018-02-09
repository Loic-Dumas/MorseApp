package com.loic.morseapp.player

import android.os.CountDownTimer
import android.util.Log
import java.util.*

/**
 * Created by loic.dumas on 19/01/2018.
 */
class PlayerController {

    companion object {
        val TIME_LENGH: Long = 200
    }

    private val _listeners = ArrayList<PlayerListener>()

    fun addListener(listener: PlayerListener) {
        _listeners.add(listener)
    }

    fun removeListener(listener: PlayerListener) {
        _listeners.remove(listener)
    }

    fun removeAllListener() {
        _listeners.clear()
    }

    fun play(morseCode: String) {

        // check if it's a correct morse code

        // transform to boolean signal
        val morseSignal = ArrayList<Boolean>()

        for (char in morseCode) {        // simplifié, on gère tous les espaces pareil
            when (char) {
                '-' -> {
                    morseSignal += Arrays.asList(true, true, true, false)
                }
                '.' -> {
                    morseSignal += Arrays.asList(true, false)
                }
                ' ' -> {
                    morseSignal += Arrays.asList(false, false, false)
                }
            }
        }
        Log.d("DEBUG", morseSignal.toString())


        // play
        var index = 0
        val timer = object : CountDownTimer(morseSignal.size * TIME_LENGH, TIME_LENGH) {
            override fun onFinish() {
                _listeners.forEach { it.playerFinished() }
            }

            override fun onTick(millisUntilFinished: Long) {
                if (index < morseSignal.size) {
                    Log.d("DEBUG", "Display : ${morseSignal[index]}")
                    // faire un check pour éviter de rappeler le listener si on était déjà dans le bon état
                    if (morseSignal[index]) switchOnListeners()
                    else switchOffListeners()
                    index++
                } else {
                    Log.e("Error", "The CharSequence is over")
                    this.cancel()
                }
            }
        }
        timer.start()

        _listeners.forEach { it.playerStarted() }
    }

    private fun switchOnListeners() {
        _listeners.forEach { it.switchOn() }
    }

    private fun switchOffListeners() {
        _listeners.forEach { it.switchOff() }
    }
}