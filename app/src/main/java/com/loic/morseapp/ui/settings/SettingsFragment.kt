package com.loic.morseapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.loic.morseapp.R

//private const val ARG_PARAM1 = "param1"

/**
 * Fragment for settings.
 * This fragment will contains all settings to customize apps and info about the app, (version
 * number, privacy policy, ...)
 *
 * Use the [SettingsFragment.newInstance] factory method to create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    companion object {
        const val TAG = "SettingsFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment [SettingsFragment].
         */
        @JvmStatic
        fun newInstance() =
                SettingsFragment().apply {
                    arguments = Bundle().apply {
//                        putString(ARG_PARAM1, "param1")
                    }
                }
    }

//    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
}
