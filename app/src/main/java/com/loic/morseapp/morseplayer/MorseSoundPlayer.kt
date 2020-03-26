package com.loic.morseapp.morseplayer

import android.content.Context
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.loic.morseapp.R

class MorseSoundPlayer(context: Context) : MorseOutputPlayerInterface {

    private val _context = context
    private val _dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(context, context.getString(R.string.app_name)))
    private val _videoSource = ProgressiveMediaSource.Factory(_dataSourceFactory)
            .createMediaSource(RawResourceDataSource.buildRawResourceUri(R.raw.morse_sound_1sec))

    private var _player: SimpleExoPlayer? = null

    override fun onPlayerAdded() {
        _player = SimpleExoPlayer.Builder(_context).build()
        _player?.prepare(_videoSource)
        _player?.repeatMode = Player.REPEAT_MODE_ALL
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

    override fun onPlayerFinished() {
    }

    override fun onTotalProgressChanged(progress: Float) {
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
    }
}