package com.loic.morseapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.loic.morseapp.morseconverter.MorseConverter
import com.loic.morseapp.morseconverter.UnexpectedCharacterException
import com.loic.morseapp.morseconverter.UnknownMorseCharacterException
import com.loic.morseapp.morseplayer.*
import com.loic.morseapp.util.SingleToast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MorsePlayer.MorseOutputPlayer {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 565
        private const val SHARED_PREF = "shared_preferences_morse_app"
        private const val SHARED_PREF_FLASHLIGHT = "pref_flashlight"
        private const val SHARED_PREF_SOUND = "pref_sound"
        private const val SHARED_PREF_VIBRATION = "pref_vibration"
    }

    private val _morsePlayer = MorsePlayer()
    private val _morseFlashPlayer: MorseFlashLightPlayerInterface by lazy { getCameraController() }
    private val _morseSoundPlayer: MorseSoundPlayer by lazy { MorseSoundPlayer(this) }
    private val _morseVibrationPlayer: MorseVibrationPlayer by lazy { MorseVibrationPlayer(this) }

    private var _alphaEditTextHasFocus = false
    private var _morseEditTextHasFocus = false

    private var _menu: Menu? = null
    private lateinit var _flashStatus: Status
    private lateinit var _soundStatus: Status
    private lateinit var _vibrationStatus: Status


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init status of each possible output, based of previous saved status in shared preferences
        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        _flashStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_FLASHLIGHT, Status.ON.toString()))
        _soundStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_SOUND, Status.ON.toString()))
        _vibrationStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_VIBRATION, Status.ON.toString()))

        //region Init player by adding output players
        _morsePlayer.addMorseOutputPlayer(this)

        // Add flash light morse player
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (_flashStatus == Status.ON) {
                addFlashControllerOrRequestPermission(false)
            }
        } else {
            _flashStatus = Status.UNAVAILABLE
        }

        // Add sound morse player
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            if (_soundStatus == Status.ON) {
                _morsePlayer.addMorseOutputPlayer(_morseSoundPlayer)
            }
        } else {
            _soundStatus = Status.UNAVAILABLE
        }

        // Add vibration morse player
        if ((getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            if (_vibrationStatus == Status.ON) {
                _morsePlayer.addMorseOutputPlayer(_morseVibrationPlayer)
            }
        } else {
            _vibrationStatus = Status.UNAVAILABLE
        }
        //endregion

        //region Set the view (button, ...)
        etAlphaTextToTranslate.addTextChangedListener(onAlphaToTranslateChanged)
        etAlphaTextToTranslate.setOnFocusChangeListener { _, hasFocus -> _alphaEditTextHasFocus = hasFocus }
        etMorseCodeToTranslate.addTextChangedListener(onMorseToTranslateChanged)
        etMorseCodeToTranslate.setOnFocusChangeListener { _, hasFocus -> _morseEditTextHasFocus = hasFocus }

        btPlayStop.setOnClickListener {
            if (_morsePlayer.isPlaying) {
                _morsePlayer.stop()
            } else {
                _morsePlayer.play(etMorseCodeToTranslate.text.toString())
                if (currentFocus != null) {
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }
            }
        }

        //endregion
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(SHARED_PREF_FLASHLIGHT, _flashStatus.toString())
                .putString(SHARED_PREF_SOUND, _soundStatus.toString())
                .putString(SHARED_PREF_VIBRATION, _vibrationStatus.toString())
                .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        _morsePlayer.removeAllMorseOutputPlayer()
    }

    /**
     * TextWatcher used to translate the written alpha text into morse time the text change.
     */
    private val onAlphaToTranslateChanged = object : TextWatcher {
        override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (_alphaEditTextHasFocus) {
                _morsePlayer.stop()

                etMorseCodeToTranslate.setText(MorseConverter.convertAlphaToMorse(sequence.toString()))
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    /**
     * TextWatcher used to translate the written morse code into alpha text time the text change.
     */
    private val onMorseToTranslateChanged = object : TextWatcher {
        override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (_morseEditTextHasFocus) {
                _morsePlayer.stop()

                try {
                    etAlphaTextToTranslate.setText(MorseConverter.convertMorseToAlpha(sequence.toString()))
                } catch (e: UnexpectedCharacterException) {
                    SingleToast.showShortToast(this@MainActivity, "${e.char} is forbidden, only - . and spaces are allowed.")
                } catch (e: UnknownMorseCharacterException) {
                    SingleToast.showShortToast(this@MainActivity, "Impossible to recognize this Character : ${e.morseChar}")
                }
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    //region Toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.output_menu, menu)
        _menu = menu;

        updateFlashLightMenuIcon()
        updateSoundMenuIcon()
        updateVibrationMenuIcon()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_flash_light -> {
                when (_flashStatus) {
                    Status.ON -> {
                        _morsePlayer.removeMorseOutputPlayer(_morseFlashPlayer)
                        _flashStatus = Status.OFF
                    }
                    Status.OFF -> {
                        addFlashControllerOrRequestPermission(true)
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(this, "Flash not available")
                    }
                }
                updateFlashLightMenuIcon()
            }
            R.id.action_sound -> {
                when (_soundStatus) {
                    Status.ON -> {
                        _morsePlayer.removeMorseOutputPlayer(_morseSoundPlayer)
                        _soundStatus = Status.OFF
                    }
                    Status.OFF -> {
                        _morsePlayer.addMorseOutputPlayer(_morseSoundPlayer)
                        _soundStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(this, "Sound not available")
                    }
                }
                updateSoundMenuIcon()
            }
            R.id.action_vibration -> {
                when (_vibrationStatus) {
                    Status.ON -> {
                        _morsePlayer.removeMorseOutputPlayer(_morseVibrationPlayer)
                        _vibrationStatus = Status.OFF
                    }
                    Status.OFF -> {
                        _morsePlayer.addMorseOutputPlayer(_morseVibrationPlayer)
                        _vibrationStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(this, "Vibration not available")
                    }
                }
                updateVibrationMenuIcon()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateFlashLightMenuIcon() {
        when (_flashStatus) {
            Status.ON -> _menu?.findItem(R.id.action_flash_light)?.setIcon(R.drawable.ic_flashlight_on)
            Status.OFF -> _menu?.findItem(R.id.action_flash_light)?.setIcon(R.drawable.ic_flashlight_off)
            Status.UNAVAILABLE -> _menu?.findItem(R.id.action_flash_light)?.icon = getTransparentIcon(R.drawable.ic_flashlight_off)
        }
    }

    private fun updateSoundMenuIcon() {
        when (_soundStatus) {
            Status.ON -> _menu?.findItem(R.id.action_sound)?.setIcon(R.drawable.ic_sound_on)
            Status.OFF -> _menu?.findItem(R.id.action_sound)?.setIcon(R.drawable.ic_sound_off)
            Status.UNAVAILABLE -> _menu?.findItem(R.id.action_sound)?.icon = getTransparentIcon(R.drawable.ic_sound_off)
        }
    }

    private fun updateVibrationMenuIcon() {
        when (_vibrationStatus) {
            Status.ON -> _menu?.findItem(R.id.action_vibration)?.setIcon(R.drawable.ic_vibration_on)
            Status.OFF -> _menu?.findItem(R.id.action_vibration)?.setIcon(R.drawable.ic_vibration_off)
            Status.UNAVAILABLE -> _menu?.findItem(R.id.action_vibration)?.icon = getTransparentIcon(R.drawable.ic_vibration_off)
        }
    }

    private fun getTransparentIcon(drawableId: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        drawable?.alpha = 50
        return drawable
    }
    //endregion

    /**
     * Add [MorseFlashLightPlayerInterface] to the [_morsePlayer].
     * As [MorseFlashLightOldPlayer] requires camera permission, if permission isn't granted, ask
     * the permission to the user. WARNING, in this case, no morsePlayer are added to [_morsePlayer].
     * This method update [_flashStatus].
     * @param forceRequest if true and permission already refused, display a SnackBar to explain.
     */
    private fun addFlashControllerOrRequestPermission(forceRequest: Boolean) {
        if (_flashStatus != Status.UNAVAILABLE) {
            // if sdk version >= M -> we can use Camera2, otherwise, we need to request permission for old Camera
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                _morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                _flashStatus = Status.ON
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    _morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                    _flashStatus = Status.ON
                } else {
                    // need to request permission, and check if user already refused.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        if (forceRequest) {
                            Snackbar.make(mainActivityCoordinatorLayout, R.string.camera_required, Snackbar.LENGTH_LONG)
                                    .setAction("Allow") {
                                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                                    }
                                    .show()
                        }
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                    }
                    _flashStatus = Status.OFF
                }
            }
        }
    }

    /**
     * Return the right [MorseFlashLightPlayerInterface] depending of the android API version.
     */
    private fun getCameraController(): MorseFlashLightPlayerInterface {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MorseFlashLightPlayer(this)
        } else {
            MorseFlashLightOldPlayer()
        }
    }

    /**
     * To Handle permission request, in this case to use camera.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    _morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                    _flashStatus = Status.ON
                    updateFlashLightMenuIcon()
                }
            }
        }
    }

    override fun onPlayerAdded() {
    }

    override fun onPlayerRemoved() {
    }

    //region MorsePlayerListenerInterface implementation
    override fun switchOn() {
    }

    override fun switchOff() {
    }

    override fun onPlayerStarted() {
        btPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_black_24dp))
        btPlayStop.contentDescription = resources.getString(R.string.stop)
    }

    override fun onPlayerFinished() {
        btPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp))
        btPlayStop.contentDescription = resources.getString(R.string.play)

//        tvMorseCode.text = tvMorseCode.text.toString()
//
//        tvMorseCode.text = SpannableString(tvMorseCode.text.toString())
    }

    override fun onTotalProgressChanged(progress: Float) {
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
//        if (letterIndex < tvMorseCode.text.length) {
//            val span = SpannableString(tvMorseCode.text.toString())
//            span.setSpan(BackgroundColorSpan(Color.GREEN), letterIndex, letterIndex + 1, 0)
//            tvMorseCode.text = span
//        }
    }
    //endregion

    /**
     * Enumeration for the status of each output signal.
     * ON : output available and activated.
     * OFF : output available but deactivated.
     * UNAVAILABLE : output unavailable (device don't have this output)
     */
    enum class Status {
        ON, OFF, UNAVAILABLE;

        companion object {
            fun fromString(key: String?): Status {
                return when (key) {
                    "ON" -> ON
                    "OFF" -> OFF
                    else -> UNAVAILABLE
                }
            }
        }
    }
}
