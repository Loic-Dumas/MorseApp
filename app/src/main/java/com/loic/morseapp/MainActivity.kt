package com.loic.morseapp

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.loic.morseapp.controller.*
import com.loic.morseapp.morseconverter.MorseConverter
import com.loic.morseapp.morseconverter.UnexpectedCharacterException
import com.loic.morseapp.morseconverter.UnknownMorseCharacterException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MorsePlayerListenerInterface {

    private var textToMorse = true
    private val _morsePlayer = MorsePlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Init player by adding player
        _morsePlayer.addListener(this)


        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                _morsePlayer.addListener(FlashLightController(this))
            } else {
                _morsePlayer.addListener(FlashLightOldController())
            }
        }

        if ((getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            _morsePlayer.addListener(VibratorController(this))
        }
        //endregion

        //region Set the view (button, ...)
        setButtonText()
        btConvertMode.setOnClickListener {
            _morsePlayer.stop()
            textToMorse = !textToMorse
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
        if (textToMorse) {
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
        if (textToMorse) {
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
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMorseCharacterChanged(letterIndex: Int) {
        if (letterIndex < tvTextResult.text.length) {
            val span = SpannableString(tvTextResult.text.toString())
            span.setSpan(BackgroundColorSpan(Color.GREEN), letterIndex, letterIndex + 1, 0)
            tvTextResult.text = span
        }
    }

}
