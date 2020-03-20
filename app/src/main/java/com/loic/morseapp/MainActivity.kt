package com.loic.morseapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
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
        private const val CAMERA_PERMISSION_REQUEST_CODE = 50
    }

    /**
     * Enumeration for the status of each output signal.
     * ON : output available and activated.
     * OFF : output available but deactivated.
     * ON : output unavailable (device don't have this output or permission refused.
     */
    enum class Status {
        ON, OFF, UNAVAILABLE
    }

    private val _morsePlayer = MorsePlayer()
    private var _textToMorse = true
    private lateinit var _flashListener: MorsePlayerListenerInterface
    private lateinit var _soundListener: MorsePlayerListenerInterface
    private lateinit var _vibratorListener: MorsePlayerListenerInterface

    private var _menu: Menu? = null
    private var _flashStatus = Status.OFF // TODO init with persistance
    private var _soundStatus = Status.OFF
    private var _vibratorStatus = Status.OFF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Init player by adding player
        _morsePlayer.addListener(this)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                _flashListener = FlashLightController(this)
                _morsePlayer.addListener(_flashListener)
                _flashStatus = Status.ON
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // Permission has already been granted
                    _flashListener = FlashLightOldController()
                    _morsePlayer.addListener(_flashListener)
                    _flashStatus = Status.ON
                } else {
                    // Permission not granted, need to ask it
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                }
            }
        }

        // todo Add sound listener
        _soundStatus = Status.UNAVAILABLE


        if ((getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            _vibratorListener = VibratorController(this)
            _morsePlayer.addListener(_vibratorListener)
            _vibratorStatus = Status.ON
        } else {
            _vibratorStatus = Status.UNAVAILABLE
        }
        //endregion

        //region Set the view (button, ...)
        setButtonText()
        btConvertMode.setOnClickListener {
            _morsePlayer.stop()
            _textToMorse = !_textToMorse
            setButtonText()
            etTextToConvert.setText(tvTextResult.text.toString())
            etTextToConvert.setSelection(etTextToConvert.length())
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

    override fun onDestroy() {
        super.onDestroy()
        _morsePlayer.removeAllListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.output_menu, menu)
        _menu = menu;

        updateFlashLightMenuIcon()
        updateSoundMenuIcon()
        updateVibratorMenuIcon()

        return true
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
            Status.ON -> _menu?.findItem(R.id.action_vibrator)?.setIcon(R.drawable.ic_vibration_on)
            Status.OFF -> _menu?.findItem(R.id.action_vibrator)?.setIcon(R.drawable.ic_vibration_off)
            Status.UNAVAILABLE -> _menu?.findItem(R.id.action_vibrator)?.icon = getTransparentIcon(R.drawable.ic_vibration_off)
        }
    }

    private fun getTransparentIcon(drawableId: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        drawable?.alpha = 50
        return drawable
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
                        _morsePlayer.addListener(_flashListener)
                        _flashStatus = Status.ON
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
                        Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
                    }
                }
                updateVibratorMenuIcon()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * To Handle permission request, in this case to use camera.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    _morsePlayer.addListener(FlashLightOldController())
                    _flashStatus = Status.ON
                }
            }
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

    private fun setButtonText() {
        if (_textToMorse) {
            btConvertMode.text = getString(R.string.toMorse)
        } else {
            btConvertMode.text = getString(R.string.toString)
        }
    }


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
}
