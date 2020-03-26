package com.loic.morseapp.morseplayer

/**
 * Created by loic.dumas on 19/01/2018.
 */
interface MorseOutputPlayerInterface {
    fun onPlayerAdded()
    fun onPlayerRemoved()
    fun switchOn()
    fun switchOff()
    fun onPlayerStarted()
    fun onPlayerFinished()
    fun onTotalProgressChanged(progress: Float)
    fun onMorseCharacterChanged(letterIndex: Int)
}