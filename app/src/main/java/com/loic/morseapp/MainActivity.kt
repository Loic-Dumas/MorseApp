package com.loic.morseapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.loic.morseapp.morseconverter.MorseConverter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val morseConverter = MorseConverter()
    private var stringToMorse = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonText()
        btConvertMode.setOnClickListener {
            stringToMorse = !stringToMorse
            setButtonText()
            etTextToConvert.setText(tvTextResult.text.toString())
            etTextToConvert.setSelection(etTextToConvert.length())
        }

        etTextToConvert.addTextChangedListener(autoTranslate)

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
        if (stringToMorse) btConvertMode.text = getString(R.string.toMorse)
        else btConvertMode.text = getString(R.string.toString)
    }

}
