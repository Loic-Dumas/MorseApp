package com.loic.morseapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.loic.morseapp.controller.*
import com.loic.morseapp.morseconverter.MorseConverter
import com.loic.morseapp.morseconverter.UnexpectedCharacterException
import com.loic.morseapp.morseconverter.UnknownMorseCharacterException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MorsePlayerListenerInterface {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 565
        private const val SHARED_PREF = "shared_preferences_morse_app"
        private const val SHARED_PREF_FLASHLIGHT = "pref_flashlight"
        private const val SHARED_PREF_SOUND = "pref_sound"
        private const val SHARED_PREF_VIBRATOR = "pref_vibrator"
    }

    private val _morsePlayer = MorsePlayer()
    private var _textToMorse = true
    private val _flashListener: FlashLightControllerInterface by lazy { getCameraController() }
    private val _soundListener: SoundController by lazy { SoundController() }
    private val _vibratorListener: VibratorController by lazy { VibratorController(this) }

    private var _menu: Menu? = null
    private lateinit var _flashStatus: Status
    private lateinit var _soundStatus: Status
    private lateinit var _vibratorStatus: Status


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init status of each possible output, based of previous saved status in shared preferences
        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        _flashStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_FLASHLIGHT, Status.ON.toString()))
        _soundStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_SOUND, Status.ON.toString()))
        _vibratorStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_VIBRATOR, Status.ON.toString()))

        //region Init player by adding player
        _morsePlayer.addListener(this)

        // Add flash light controller
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (_flashStatus == Status.ON) {
                addFlashControllerOrRequestPermission(false)
            }
        } else {
            _flashStatus = Status.UNAVAILABLE
        }

        // todo Add sound listener
        // Add sound controller
        _soundStatus = Status.UNAVAILABLE

        // Add vibrator controller
        if ((getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            if (_vibratorStatus == Status.ON) {
                _morsePlayer.addListener(_vibratorListener)
            }
        } else {
            _vibratorStatus = Status.UNAVAILABLE
        }
        //endregion

        //region Set the view (button, ...)
        setButtonText()

        btConvertMode.setOnClickListener {
            _morsePlayer.stop()
            _textToMorse = !_textToMorse
            etTextToConvert.setText(tvTextResult.text.toString()) //swap text
            etTextToConvert.setSelection(etTextToConvert.length()) //move cursor at the end of text
            setButtonText()
        }

        etTextToConvert.addTextChangedListener(onTextToTranslateChanged)

        btPlay.setOnClickListener {
            _morsePlayer.play(tvTextResult.text.toString())
            if (currentFocus != null) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        }

        btStop.setOnClickListener {
            _morsePlayer.stop()
        }
        //endregion
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(SHARED_PREF_FLASHLIGHT, _flashStatus.toString())
                .putString(SHARED_PREF_SOUND, _soundStatus.toString())
                .putString(SHARED_PREF_VIBRATOR, _vibratorStatus.toString())
                .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        _morsePlayer.removeAllListener()
    }

    private fun setButtonText() {
        if (_textToMorse) {
            btConvertMode.text = getString(R.string.toMorse)
        } else {
            btConvertMode.text = getString(R.string.toString)
        }
    }

    /**
     * TextWatcher used to translate the written text in morse every time the text imput is updated.
     */
    private val onTextToTranslateChanged = object : TextWatcher {
        override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            convertText(sequence.toString())
        }

        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    private fun convertText(text: String) {
        if (_textToMorse) {
            tvTextResult.text = MorseConverter.convertTextToMorse(text)
        } else {
            try {
                tvTextResult.text = MorseConverter.convertMorseToText(text)
            } catch (e: UnexpectedCharacterException) {
                Toast.makeText(this, "${e.char} is forbidden, only - . and spaces are allowed.", Toast.LENGTH_SHORT).show()
            } catch (e: UnknownMorseCharacterException) {
                Toast.makeText(this, "Impossible to recognize this Character : ${e.morseChar}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //region Toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.output_menu, menu)
        _menu = menu;

        updateFlashLightMenuIcon()
        updateSoundMenuIcon()
        updateVibratorMenuIcon()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_flash_light -> {
                when (_flashStatus) {
                    Status.ON -> {
                        _morsePlayer.removeListener(_flashListener)
                        _flashStatus = Status.OFF
                    }
                    Status.OFF -> {
                        addFlashControllerOrRequestPermission(true)
                    }
                    Status.UNAVAILABLE -> {
                        Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
                    }
                }
                updateFlashLightMenuIcon()
            }
            R.id.action_sound -> {
                when (_soundStatus) {
                    Status.ON -> {
//                        _morsePlayer.removeListener(_soundListener)
                        _soundStatus = Status.OFF
                    }
                    Status.OFF -> {
//                        _morsePlayer.addListener(_soundListener)
                        _soundStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        Toast.makeText(this, "Sound not available", Toast.LENGTH_SHORT).show()
                    }
                }
                updateSoundMenuIcon()
            }
            R.id.action_vibrator -> {
                when (_vibratorStatus) {
                    Status.ON -> {
                        _morsePlayer.removeListener(_vibratorListener)
                        _vibratorStatus = Status.OFF
                    }
                    Status.OFF -> {
                        _morsePlayer.addListener(_vibratorListener)
                        _vibratorStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        Toast.makeText(this, "Vibrator not available", Toast.LENGTH_SHORT).show()
                    }
                }
                updateVibratorMenuIcon()
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

    private fun updateVibratorMenuIcon() {
        when (_vibratorStatus) {
            Status.ON -> _menu?.findItem(R.id.action_vibrator)?.setIcon(R.drawable.ic_vibrator_on)
            Status.OFF -> _menu?.findItem(R.id.action_vibrator)?.setIcon(R.drawable.ic_vibrator_off)
            Status.UNAVAILABLE -> _menu?.findItem(R.id.action_vibrator)?.icon = getTransparentIcon(R.drawable.ic_vibrator_off)
        }
    }

    private fun getTransparentIcon(drawableId: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        drawable?.alpha = 50
        return drawable
    }
    //endregion

    /**
     * Add [FlashLightControllerInterface] to the [_morsePlayer].
     * As [FlashLightOldController] requires camera permission, if permission isn't granted, ask
     * the permission to the user. WARNING, in this case, no listener are added to [_morsePlayer].
     * This method update [_flashStatus].
     * @param forceRequest if true and permission already refused, display a SnackBar to explain.
     */
    private fun addFlashControllerOrRequestPermission(forceRequest: Boolean) {
        if (_flashStatus != Status.UNAVAILABLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                _morsePlayer.addListener(_flashListener)
                _flashStatus = Status.ON
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    _morsePlayer.addListener(_flashListener)
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
     * Return the right [FlashLightControllerInterface] depending of the android API version.
     */
    private fun getCameraController(): FlashLightControllerInterface {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FlashLightController(this)
        } else {
            FlashLightOldController()
        }
    }

    /**
     * To Handle permission request, in this case to use camera.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    _morsePlayer.addListener(_flashListener)
                    _flashStatus = Status.ON
                    updateFlashLightMenuIcon()
                }
            }
        }
    }

    //region MorsePlayerListenerInterface implementation
    override fun switchOn() {
        viewOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.switchOnColor))
    }

    override fun switchOff() {
        viewOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.switchOffColor))
    }

    override fun onPlayerStarted() {
        Toast.makeText(this, "starting", Toast.LENGTH_SHORT).show()
    }

    override fun onPlayerFinished() {
        viewOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.switchOffColor))
        tvTextResult.text = tvTextResult.text.toString()
        Toast.makeText(this, "Over", Toast.LENGTH_SHORT).show()

        tvTextResult.text = SpannableString(tvTextResult.text.toString())
    }

    override fun onTotalProgressChanged(progress: Float) {
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
        if (letterIndex < tvTextResult.text.length) {
            val span = SpannableString(tvTextResult.text.toString())
            span.setSpan(BackgroundColorSpan(Color.GREEN), letterIndex, letterIndex + 1, 0)
            tvTextResult.text = span
        }
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
