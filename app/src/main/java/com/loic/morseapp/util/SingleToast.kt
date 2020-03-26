package com.loic.morseapp.util

import android.content.Context
import android.widget.Toast

/**
 * [SingleToast] avoid accumulation of Toast.
 * Create a toast with given parameters, and cancel previous toast if any.
 * Warning, if different text should be displayed, only the last one will be visible.
 */
class SingleToast {

    companion object {
        private var toast: Toast? = null

        fun showShortToast(context: Context, text: String) {
            showToast(context, text, Toast.LENGTH_SHORT)
        }

        fun showLongToast(context: Context, text: String) {
            showToast(context, text, Toast.LENGTH_LONG)
        }

        fun showToast(context: Context, text: String, duration: Int) {
            toast?.cancel()
            toast = Toast.makeText(context, text, duration)
            toast?.show()
        }
    }
}