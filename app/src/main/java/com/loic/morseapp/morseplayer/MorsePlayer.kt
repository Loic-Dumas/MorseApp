package com.loic.morseapp.morseplayer

import android.os.CountDownTimer
import android.util.Log
import com.loic.morseapp.morseplayer.MorsePlayer.MorseOutputPlayer

/**
 * [MorsePlayer] plays a morse sequence by calling output (Flashlight, sound, ...).
 * These output implement [MorseOutputPlayer].
 */
class MorsePlayer {

    companion object {
        const val TIME_LENGTH: Long = 160
    }

    private val _morseOutputPlayers = ArrayList<MorseOutputPlayer>()
    private var _timer: CountDownTimer? = null

    var isPlaying = false

    fun addMorseOutputPlayer(player: MorseOutputPlayer) {
        if (!_morseOutputPlayers.contains(player)) {
            player.onPlayerAdded()
            _morseOutputPlayers.add(player)
        }
    }

    fun removeMorseOutputPlayer(player: MorseOutputPlayer) {
        player.switchOff()
        player.onPlayerRemoved()
        _morseOutputPlayers.removeAll(listOf(player))
    }

    fun removeAllMorseOutputPlayer() {
        _morseOutputPlayers.forEach { it.onPlayerRemoved() }
        _morseOutputPlayers.clear()
    }

    /**
     * @param morseCode a String in morse code, so only composed by . - or spaces
     * When the play() method is launched, player outputs are called when they need to change the state.
     */
    fun play(morseCode: String) {
        // mechanism : the morseCode string is transformed into an array of boolean
        // Then a CountDownTimer is used to be called back on every tick (every TIME_LENGTH)

        stop()

        val morseSignal = transformToMorseSignal(morseCode)
        Log.d("DEBUG", morseSignal.toString())

        isPlaying = true
        notifyPlayerStarted()

        var indexSignal = 0
        var indexChar = -1
        var previousState = false
        _timer = object : CountDownTimer((morseSignal.size) * TIME_LENGTH, TIME_LENGTH) {
            override fun onFinish() {
                isPlaying = false
                notifyPlayerFinished(true)
            }

            override fun onTick(millisUntilFinished: Long) {
                if (indexSignal < morseSignal.size) {
                    val currentSignal = morseSignal[indexSignal]

                    // notify if we switchOn or Off
                    if (previousState != currentSignal.activated) {
                        if (currentSignal.activated) switchOnOutputs()
                        else switchOffOutputs()

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
        isPlaying = false
        switchOffOutputs()
        notifyPlayerFinished(false)
    }

    /**
     * @param activated : if true, the output should be switch on, and switch off if false
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

    private fun switchOnOutputs() {
        _morseOutputPlayers.forEach { it.switchOn() }
    }

    private fun switchOffOutputs() {
        _morseOutputPlayers.forEach { it.switchOff() }
    }

    private fun notifyCharacterChanged(letterIndex: Int) {
        _morseOutputPlayers.forEach { it.onMorseCharacterChanged(letterIndex) }
    }

    private fun notifyTotalProgress(percent: Float) {
        _morseOutputPlayers.forEach { it.onTotalProgressChanged(percent) }
    }

    private fun notifyPlayerStarted() {
        _morseOutputPlayers.forEach { it.onPlayerStarted() }
    }

    private fun notifyPlayerFinished(morseCodeFullyPlayed: Boolean) {
        _morseOutputPlayers.forEach { it.onPlayerFinished(morseCodeFullyPlayed) }
    }

    /**
     * Output triggered by [MorsePlayer] must implement this interface.
     */
    interface MorseOutputPlayer {
        /**
         * This is called when the player is added to [MorsePlayer].
         * Initialisation of the output must be done here.
         */
        fun onPlayerAdded()

        /**
         * This is called when the player is removed of [MorsePlayer].
         * If the output used need to be released, this must be done here.
         */
        fun onPlayerRemoved()
        fun switchOn()
        fun switchOff()
        fun onPlayerStarted()

        /**
         * @param morseCodeFullyPlayed true if the morse sequence have been played until the end,
         * false if the player have been stop.
         */
        fun onPlayerFinished(morseCodeFullyPlayed: Boolean)
        fun onTotalProgressChanged(progress: Float)
        fun onMorseCharacterChanged(letterIndex: Int)
    }
}
