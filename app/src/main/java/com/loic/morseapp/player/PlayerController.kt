package com.loic.morseapp.player

import android.os.CountDownTimer
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by loic.dumas on 19/01/2018.
 */
class PlayerController {

    companion object {
        val TIME_LENGTH: Long = 500
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

    /**
     * @param a String in morse code, so only composed by . - or spaces
     * When the play() method is launched, player listeners are called when they need to change the state.
     */
    fun play(morseCode: String) {
        // mechanism : the morseCode string is transformed into an array of boolean
        // Then a CountDownTimer is used to be called back on every tick (every TIME_LENGTH)

        val morseSignal = transformToMorseSignal(morseCode)
        Log.d("DEBUG", morseSignal.toString())

        _listeners.forEach { it.onPlayerStarted() }

        var indexSignal = 0
        var indexChar = 0
        var previousState = false
        object : CountDownTimer((morseSignal.size + 1) * TIME_LENGTH, TIME_LENGTH) {
            override fun onFinish() {
                _listeners.forEach { it.onPlayerFinished() }
            }

            override fun onTick(millisUntilFinished: Long) {
                if (indexSignal < morseSignal.size) {
                    val currentSignal = morseSignal[indexSignal]

                    // notify if we switchOn or Off
                    if (previousState != currentSignal.activated) {
                        if (currentSignal.activated) switchOnListeners()
                        else switchOffListeners()

                        previousState = currentSignal.activated
                    }

                    notifyTotalProgress((indexSignal / morseSignal.size).toFloat())

                    // notify character changed
                    if (indexChar != currentSignal.charIndex) {
                        notifyCharacterChanged(currentSignal.charIndex)
                        indexChar = currentSignal.charIndex
                    }

                    Log.d("DEBUG", "Display signal $indexSignal: $currentSignal")

                    indexSignal++
                } else {
                    Log.e("Error", "The CharSequence is over")
                    this.cancel()
                }
            }
        }.start()
    }

    /**
     * @param activated : if true, the listener should be switch on, and switch off if false
     */
    private class Signal(val activated: Boolean, val charIndex: Int) {
        override fun toString(): String {
            return "[Activated : $activated, charIndex : $charIndex]"
        }
    }

    private fun transformToMorseSignal(morseCode: String): ArrayList<Signal> {
        val morseSignal = ArrayList<Signal>()
        for (idx in 0 until morseCode.length) {
            when (morseCode[idx]) {
                '-' -> {
                    morseSignal += Arrays.asList(Signal(true, idx), Signal(true, idx), Signal(true, idx), Signal(false, idx))
                }
                '.' -> {
                    morseSignal += Arrays.asList(Signal(true, idx), Signal(false, idx))
                }
                ' ' -> {
                    morseSignal += Arrays.asList(Signal(false, idx), Signal(false, idx), Signal(false, idx))
                }
            }
        }
        return morseSignal
    }

    private fun switchOnListeners() {
        _listeners.forEach { it.switchOn() }
    }

    private fun switchOffListeners() {
        _listeners.forEach { it.switchOff() }
    }

    private fun notifyCharacterChanged(letterIndex: Int) {
        _listeners.forEach { it.onMorseCharacterChanged(letterIndex) }
    }

    private fun notifyTotalProgress(percent: Float) {
        _listeners.forEach { it.onTotalProgressChanged(percent) }
    }
}