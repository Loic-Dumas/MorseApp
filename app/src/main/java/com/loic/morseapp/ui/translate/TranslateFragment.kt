package com.loic.morseapp.ui.translate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.loic.morseapp.R
import com.loic.morseapp.ui.settings.SettingsFragment

/**
 * Fragment for the translate view.
 * It will display an area to enter text and button to play this text with outputs.
 */
class TranslateFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [TranslateFragment].
         */
        fun newInstance() = TranslateFragment()

        const val TAG = "TranslateFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_translate, container, false)
        return root
    }
}
