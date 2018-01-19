package com.loic.morseapp.player

import android.util.Log

/**
 * Created by loic.dumas on 19/01/2018.
 */
class PlayerController {

    val TIME : Long = 1000
    private val _listeners = ArrayList<PlayerListener>()
    var _currentPlayer = Thread()

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
//        if (morseCode.matches(Regex()))

        _currentPlayer = Thread {
            for (char in morseCode) {
                when (char) {
                    '-' -> {

                        Log.d("Player", "begin - ")

                        switchOnListeners()
                        Thread.sleep(3 * TIME)
                        Log.d("Player", "medium - ")
                        switchOffListeners()
                        Thread.sleep(TIME)
                        Log.d("Player", "end - ")
                    }
                    '.' -> {
                        Log.d("Player", "begin . ")
                        switchOnListeners()

                        Thread.sleep(TIME)
                        Log.d("Player", "medium . ")
                        switchOffListeners()
                        Thread.sleep(TIME)
                        Log.d("Player", "end . ")
                    }
                    ' ' -> {
                        Log.d("Player", "begin ' ' ")
                        switchOffListeners()
                        Thread.sleep(TIME)
                        Log.d("Player", "end ' ' ")
                    }
                    else -> {
                        Log.d("Player", "begin ? ")
                        switchOffListeners()
                        Thread.sleep(TIME)
                        Log.d("Player", "end ? ")
                    }
                }
            }

        }
        _currentPlayer.start()



    }

    private fun switchOnListeners() {
        for (listener in _listeners) {
            listener.switchOn()
        }
    }

    private fun switchOffListeners() {
        for (listener in _listeners) {
            listener.switchOff()
        }
    }
}