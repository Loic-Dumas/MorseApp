package com.loic.morseapp.player

/**
 * Created by loic.dumas on 19/01/2018.
 */
interface PlayerListener {
    fun switchOn()
    fun switchOff()
    fun onPlayerStarted()
    fun onPlayerFinished()
    fun onTotalProgressChanged(progress: Float)
    fun onMorseCharacterChanged(letterIndex: Int)
}