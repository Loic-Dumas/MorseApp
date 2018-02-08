package com.loic.morseapp.player

/**
 * Created by loic.dumas on 19/01/2018.
 */
interface PlayerListener {
    fun switchOn()
    fun switchOff()
    fun playerStarted()
    fun playerFinished()
    fun notifyProgress(progress: Float, letterIndex: Int)
}