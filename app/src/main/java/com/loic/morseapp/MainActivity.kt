package com.loic.morseapp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
        private const val SHARED_PREF_REPEAT_MODE_ACTIVATED = "repeat_mode_activated"
    }

    private val _morsePlayer = MorsePlayer()
    private val _morseFlashPlayer: MorseFlashLightPlayerInterface by lazy { getCameraController() }
    private val _morseSoundPlayer: MorseSoundPlayer by lazy { MorseSoundPlayer(this) }
    private val _morseVibrationPlayer: MorseVibrationPlayer by lazy { MorseVibrationPlayer(this) }

    private var _alphaEditTextHasFocus = false
    private var _morseEditTextHasFocus = false
    private val _clipBoardManager: ClipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private var _menu: Menu? = null
    private lateinit var _flashStatus: Status
    private lateinit var _soundStatus: Status
    private lateinit var _vibrationStatus: Status
    private var _isRepeatMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.morse)

        // init status of each possible output, based of previous saved status in shared preferences
        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        _flashStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_FLASHLIGHT, Status.ON.toString()))
        _soundStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_SOUND, Status.ON.toString()))
        _vibrationStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_VIBRATION, Status.ON.toString()))
        _isRepeatMode = sharedPreferences.getBoolean(SHARED_PREF_REPEAT_MODE_ACTIVATED, false)

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
        etAlphaTextToTranslate.setOnFocusChangeListener { _, hasFocus ->
            _alphaEditTextHasFocus = hasFocus
            if (!hasFocus) {
                hideKeyboard()
            }
        }
        etMorseCodeToTranslate.addTextChangedListener(onMorseToTranslateChanged)
        etMorseCodeToTranslate.setOnFocusChangeListener { _, hasFocus -> _morseEditTextHasFocus = hasFocus }
        // todo Hide the keyboard when etMorseCodeToTranslate is clicked
        etMorseCodeToTranslate.setOnTouchListener { _, _ -> etMorseCodeToTranslate.requestFocus(); hideKeyboard(); true }

        btClearText.setOnClickListener {
            _morsePlayer.stop()
            etAlphaTextToTranslate.setText("")
            etMorseCodeToTranslate.setText("")
        }

        btCopyText.setOnClickListener {
            if (etAlphaTextToTranslate.text.isNotEmpty()) {
                _clipBoardManager.setPrimaryClip(ClipData.newPlainText("text", etAlphaTextToTranslate.text.toString()))
                SingleToast.showShortToast(this, getString(R.string.text_copied))
            } else {
                SingleToast.showShortToast(this, getString(R.string.nothing_to_copy))
            }
        }

        btCopyMorseCode.setOnClickListener {
            if (etMorseCodeToTranslate.text.isNotEmpty()) {
                _clipBoardManager.setPrimaryClip(ClipData.newPlainText("text", etMorseCodeToTranslate.text.toString()))
                SingleToast.showShortToast(this, getString(R.string.morse_copied))
            } else {
                SingleToast.showShortToast(this, getString(R.string.nothing_to_copy))
            }
        }

        btPasteMorseCode.setOnClickListener {
            _clipBoardManager.primaryClip?.let { clipData ->
                etMorseCodeToTranslate.insertAtCurrentSelection(clipData.getItemAt(0).text.toString().replace('.', '·', true))
            } ?: SingleToast.showShortToast(this, getString(R.string.nothing_to_paste))
        }

        btPlayStop.setOnClickListener {
            if (_morsePlayer.isPlaying) {
                _morsePlayer.stop()
            } else {
                etAlphaTextToTranslate.clearFocus()
                etMorseCodeToTranslate.clearFocus()
                hideKeyboard()
                _morsePlayer.play(etMorseCodeToTranslate.text.toString())
            }
        }

        btRepeatMode.setOnClickListener {
            _isRepeatMode = !_isRepeatMode
            setRepeatModeImage()
        }
        setRepeatModeImage()

        // morse keyboard
        btMorseKeyboardTi.setOnClickListener {
            etMorseCodeToTranslate.insertAtCurrentSelection(getString(R.string.ti_symbol))
        }

        btMorseKeyboardTa.setOnClickListener {
            etMorseCodeToTranslate.insertAtCurrentSelection(getString(R.string.ta_symbol))
        }

        btMorseKeyboardSpace.setOnClickListener {
            etMorseCodeToTranslate.insertAtCurrentSelection(" ")
        }

        btMorseKeyboardReturn.setOnClickListener {
            etMorseCodeToTranslate.insertAtCurrentSelection("\n")
        }

        btMorseKeyboardDelete.setOnClickListener {
            etMorseCodeToTranslate.deleteAtCurrentSelection()
        }
        //endregion

        versionNumber.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(SHARED_PREF_FLASHLIGHT, _flashStatus.toString())
                .putString(SHARED_PREF_SOUND, _soundStatus.toString())
                .putString(SHARED_PREF_VIBRATION, _vibrationStatus.toString())
                .putBoolean(SHARED_PREF_REPEAT_MODE_ACTIVATED, _isRepeatMode)
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
                if (_morsePlayer.isPlaying) {
                    _morsePlayer.stop()
                }
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
                if (_morsePlayer.isPlaying) {
                    _morsePlayer.stop()
                }

                try {
                    etAlphaTextToTranslate.setText(MorseConverter.convertMorseToAlpha(sequence.toString()))
                } catch (e: UnexpectedCharacterException) {
                    SingleToast.showShortToast(this@MainActivity, "${e.char} is forbidden, only - · return and spaces are allowed.")
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

    private fun setRepeatModeImage() {
        if (_isRepeatMode) {
            btRepeatMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_black_24dp))
        } else {
            btRepeatMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_grey_24dp))
        }
    }

    private fun hideKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    /**
     * @param string to insert at the current cursor position.
     * Request the focus for the editText and set the cursor at the new right position.
     */
    private fun EditText.insertAtCurrentSelection(string: String) {
        requestFocus()
        hideKeyboard()

        //region temporary fix while keyboard cannot be hidden when click on edit text...
//        val currentSelection = selectionStart
//        val newText = text.insert(selectionStart, string)
//        text = newText
//        setSelection(currentSelection + string.length)
        // fix :
        val newText = text.append(string)
        this.setText(newText.toString()) // need to cast it in String to refresh span, if we was playing
        this.setSelection(text.length)
        // endregion tmp fix
    }

    /**
     * Request the focus and delete the character before the cursor, if any.
     */
    private fun EditText.deleteAtCurrentSelection() {
        requestFocus()
        hideKeyboard()

        //region temporary fix while keyboard cannot be hidden when click on edit text...
//        if (selectionStart > 0) {
//            val currentSelection = selectionStart
//            val newText = text.removeRange(selectionStart - 1, selectionStart)
//            setText(newText)
//            setSelection(currentSelection - 1)
//        }
        // fix :
        val newText = text.removeRange(text.length - 1, text.length)
        this.setText(newText)
        this.setSelection(text.length)
        //endRegion tmp fix
    }

    //region MorsePlayerListenerInterface implementation
    override fun onPlayerAdded() {
    }

    override fun onPlayerRemoved() {
    }

    override fun switchOn() {
    }

    override fun switchOff() {
    }

    override fun onPlayerStarted() {
        btPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_black_24dp))
        btPlayStop.contentDescription = resources.getString(R.string.stop)
    }

    override fun onPlayerFinished(morseCodeFullyPlayed: Boolean) {
        btPlayStop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp))
        btPlayStop.contentDescription = resources.getString(R.string.play)

        // reset text
        etMorseCodeToTranslate.setText(etMorseCodeToTranslate.text.toString())

        if (_isRepeatMode && morseCodeFullyPlayed) {
            _morsePlayer.play(etMorseCodeToTranslate.text.toString())
        }
    }

    override fun onTotalProgressChanged(progress: Float) {
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
        if (letterIndex < etMorseCodeToTranslate.text.length) {
            val span = SpannableString(etMorseCodeToTranslate.text.toString())
            @Suppress("DEPRECATION")
            span.setSpan(BackgroundColorSpan(resources.getColor(R.color.textBackgroundColor)), letterIndex, letterIndex + 1, 0)
            etMorseCodeToTranslate.setText(span)
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
