package com.loic.morseapp.controller

import android.os.CountDownTimer
import android.util.Log

/**
 * Created by loic.dumas on 19/01/2018.
 */
class MorsePlayer {

    companion object {
        const val TIME_LENGTH: Long = 300
    }

    private val _listeners = ArrayList<MorsePlayerListenerInterface>()
    private var _timer: CountDownTimer? = null

    fun addListener(listener: MorsePlayerListenerInterface) {
        _listeners.add(listener)
    }

    fun removeListener(listener: MorsePlayerListenerInterface) {
        listener.switchOff()
        _listeners.remove(listener)
    }

    fun removeAllListener() {
        _listeners.clear()
    }

    /**
     * @param morseCode a String in morse code, so only composed by . - or spaces
     * When the play() method is launched, player listeners are called when they need to change the state.
     */
    fun play(morseCode: String) {
        // mechanism : the morseCode string is transformed into an array of boolean
        // Then a CountDownTimer is used to be called back on every tick (every TIME_LENGTH)

        stop()

        val morseSignal = transformToMorseSignal(morseCode)
        Log.d("DEBUG", morseSignal.toString())

        notifyPlayerStarted()

        var indexSignal = 0
        var indexChar = -1
        var previousState = false
        _timer = object : CountDownTimer((morseSignal.size) * TIME_LENGTH, TIME_LENGTH) {
            override fun onFinish() {
                notifyPlayerFinished()
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

                    notifyTotalProgress(indexSignal.toFloat() / morseSignal.size)

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
     * Stop the player
     */
    fun stop() {
        _timer?.cancel()
        switchOffListeners()
        notifyPlayerFinished()
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
        for (idx in morseCode.indices) {
            when (morseCode[idx]) {
                '-' -> {
                    morseSignal += listOf(Signal(true, idx), Signal(true, idx), Signal(true, idx))
                }
                '.' -> {
                    morseSignal += listOf(Signal(true, idx))
                }
                ' ' -> {
                    morseSignal += listOf(Signal(false, idx), Signal(false, idx))
                }
            }
            morseSignal += listOf(Signal(false, idx))
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

    private fun notifyPlayerStarted() {
        _listeners.forEach { it.onPlayerStarted() }
    }

    private fun notifyPlayerFinished() {
        _listeners.forEach { it.onPlayerFinished() }
    }
}