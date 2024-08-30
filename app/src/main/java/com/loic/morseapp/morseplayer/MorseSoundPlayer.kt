package com.loic.morseapp.morseplayer

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.loic.morseapp.R


class MorseSoundPlayer(private val _context: Context) : MorsePlayer.MorseOutputPlayer {

    private var _player: ExoPlayer? = null

    override fun onPlayerAdded() {
        _player = ExoPlayer.Builder(_context).build()
        _player?.setMediaItem(getMediaItem())
        _player?.prepare()
        _player?.repeatMode = Player.REPEAT_MODE_ALL
    }


    private fun getMediaItem(): MediaItem {

        val mmd = MediaMetadata.Builder()
            .setTitle("Morse")
            .setArtist("Hector")
            .build()

        val rawResourceUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + _context.packageName + "/" + R.raw.morse_sound_1sec)

        val rmd = MediaItem.RequestMetadata.Builder()
            .setMediaUri(rawResourceUri)
            .build()

        return MediaItem.Builder()
            .setMediaId("123")
            .setMediaMetadata(mmd)
            .setRequestMetadata(rmd)
            .build()

    }

    override fun onPlayerRemoved() {
        _player?.release()
        _player = null
    }

    override fun switchOn() {
        _player?.playWhenReady = true
    }

    override fun switchOff() {
        _player?.playWhenReady = false
        _player?.seekTo(0)
    }

    override fun onPlayerStarted() {
    }

    override fun onPlayerFinished(morseCodeFullyPlayed: Boolean) {
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
    }
}