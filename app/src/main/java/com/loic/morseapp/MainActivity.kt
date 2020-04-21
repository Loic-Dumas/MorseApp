package com.loic.morseapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.loic.morseapp.morseconverter.MorseConverter
import com.loic.morseapp.morseconverter.UnexpectedCharacterException
import com.loic.morseapp.morseconverter.UnknownMorseCharacterException
import com.loic.morseapp.morseplayer.MorsePlayer
import com.loic.morseapp.util.SingleToast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : MorsePlayerActivity(), MorsePlayer.MorseOutputPlayer {

    companion object {
        private const val SHARED_PREF = "shared_preferences_morse_app"
        private const val SHARED_PREF_REPEAT_MODE_ACTIVATED = "repeat_mode_activated"
    }

    private var _alphaEditTextHasFocus = false
    private var _morseEditTextHasFocus = false
    private val _clipBoardManager: ClipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private var _isRepeatMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.morse)

        // init status of each possible output, based of previous saved status in shared preferences
        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        _isRepeatMode = sharedPreferences.getBoolean(SHARED_PREF_REPEAT_MODE_ACTIVATED, false)

        morsePlayer.addMorseOutputPlayer(this)


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
            morsePlayer.stop()
            etAlphaTextToTranslate.setText("")
            etMorseCodeToTranslate.setText("")
        }

        btCopyText.setOnClickListener {
            if (etAlphaTextToTranslate.text.isNotEmpty()) {
                _clipBoardManager.primaryClip = ClipData.newPlainText("text", etAlphaTextToTranslate.text.toString());
                SingleToast.showShortToast(this, getString(R.string.text_copied))
            } else {
                SingleToast.showShortToast(this, getString(R.string.nothing_to_copy))
            }
        }

        btCopyMorseCode.setOnClickListener {
            if (etMorseCodeToTranslate.text.isNotEmpty()) {
                _clipBoardManager.primaryClip = ClipData.newPlainText("text", etMorseCodeToTranslate.text.toString());
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
            if (morsePlayer.isPlaying) {
                morsePlayer.stop()
            } else {
                etAlphaTextToTranslate.clearFocus()
                etMorseCodeToTranslate.clearFocus()
                hideKeyboard()
                morsePlayer.play(etMorseCodeToTranslate.text.toString())
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

    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putBoolean(SHARED_PREF_REPEAT_MODE_ACTIVATED, _isRepeatMode)
                .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        morsePlayer.removeAllMorseOutputPlayer()
    }

    /**
     * TextWatcher used to translate the written alpha text into morse time the text change.
     */
    private val onAlphaToTranslateChanged = object : TextWatcher {
        override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (_alphaEditTextHasFocus) {
                if (morsePlayer.isPlaying) {
                    morsePlayer.stop()
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
                if (morsePlayer.isPlaying) {
                    morsePlayer.stop()
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
            morsePlayer.play(etMorseCodeToTranslate.text.toString())
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

}
