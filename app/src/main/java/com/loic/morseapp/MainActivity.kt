package com.loic.morseapp

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.loic.morseapp.morseconverter.MorseConverter
import com.loic.morseapp.morseconverter.vibrator.VibratorManager
import com.loic.morseapp.player.PlayerController
import com.loic.morseapp.player.PlayerListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), PlayerListener {

    private val morseConverter = MorseConverter()
    private var stringToMorse = true
    val _player = PlayerController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonText()
        btConvertMode.setOnClickListener {
            stringToMorse = !stringToMorse
            setButtonText()
            etTextToConvert.setText(tvTextResult.text.toString())
            etTextToConvert.setSelection(etTextToConvert.length())

            etTextToConvert.inputType
        }

        etTextToConvert.addTextChangedListener(autoTranslate)


        _player.addListener(this)
        _player.addListener(VibratorManager(this))

        btPlay.setOnClickListener {
            _player.play(tvTextResult.text.toString())
            if (currentFocus != null) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _player.removeAllListener()
    }

    private val autoTranslate = object : TextWatcher {
        override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            convertText(sequence.toString())
        }

        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    private fun convertText(text: String) {
        if (stringToMorse) {
            tvTextResult.text = morseConverter.convertTextToMorse(text)
        } else {
            try {
                tvTextResult.text = morseConverter.convertMorseToText(text)
            } catch (e: Exception) {
                Toast.makeText(this, "Only - . and spaces are allowed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setButtonText() {
        if (stringToMorse) {
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

    override fun playerStarted() {
        Toast.makeText(this, "starting", Toast.LENGTH_SHORT).show()
    }

    override fun playerFinished() {
        viewOutput.setBackgroundColor(ContextCompat.getColor(this, R.color.switchOffColor))
        Toast.makeText(this, "Over", Toast.LENGTH_SHORT).show()
    }

    override fun notifyProgress(progress: Float, letterIndex: Int) {
    }

}
