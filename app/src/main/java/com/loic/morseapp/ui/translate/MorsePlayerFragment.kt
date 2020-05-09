package com.loic.morseapp.ui.translate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.loic.morseapp.R
import com.loic.morseapp.morseplayer.*
import com.loic.morseapp.util.SingleToast
import kotlinx.android.synthetic.main.activity_drawer.*

/**
 * Abstract Fragment for every Fragment which need access to [MorsePlayer] and output menu.
 * [MorsePlayerFragment] will initiate Morse Player and the menu bar with icons to select activated outputs.
 *
 * The main activity layout XML should contain a Coordinator Layout with id mainCoordinatorLayout.
 */
abstract class MorsePlayerFragment : Fragment() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 565
        private const val SHARED_PREF = "shared_preferences_morse_app"
        private const val SHARED_PREF_FLASHLIGHT = "pref_flashlight"
        private const val SHARED_PREF_SOUND = "pref_sound"
        private const val SHARED_PREF_VIBRATION = "pref_vibration"
    }

    private var _menu: Menu? = null
    private lateinit var _flashStatus: Status
    private lateinit var _soundStatus: Status
    private lateinit var _vibrationStatus: Status

    protected val morsePlayer = MorsePlayer()
    private val _morseSoundPlayer: MorseSoundPlayer by lazy { MorseSoundPlayer(activity!!) }
    private val _morseVibrationPlayer: MorseVibrationPlayer by lazy { MorseVibrationPlayer(activity!!) }
    private val _morseFlashPlayer: MorseFlashLightPlayerInterface by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MorseFlashLightPlayer(activity!!)
        } else {
            MorseFlashLightOldPlayer()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true);

        val sharedPreferences = activity!!.baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        _flashStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_FLASHLIGHT, Status.ON.toString()))
        _soundStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_SOUND, Status.ON.toString()))
        _vibrationStatus = Status.fromString(sharedPreferences.getString(SHARED_PREF_VIBRATION, Status.ON.toString()))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.output_menu, menu)
        _menu = menu

        // Add flash light morse player
        if (activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (_flashStatus == Status.ON) {
                addFlashControllerOrRequestPermission(false)
            }
        } else {
            _flashStatus = Status.UNAVAILABLE
        }

        // Add sound morse player
        if (activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            if (_soundStatus == Status.ON) {
                morsePlayer.addMorseOutputPlayer(_morseSoundPlayer)
            }
        } else {
            _soundStatus = Status.UNAVAILABLE
        }

        // Add vibration morse player
        if ((activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            if (_vibrationStatus == Status.ON) {
                morsePlayer.addMorseOutputPlayer(_morseVibrationPlayer)
            }
        } else {
            _vibrationStatus = Status.UNAVAILABLE
        }

        updateFlashLightMenuIcon()
        updateSoundMenuIcon()
        updateVibrationMenuIcon()
    }

    override fun onPause() {
        super.onPause()

        // save in shared pref the new status
        val sharedPreferences = activity!!.baseContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(SHARED_PREF_FLASHLIGHT, _flashStatus.toString())
                .putString(SHARED_PREF_SOUND, _soundStatus.toString())
                .putString(SHARED_PREF_VIBRATION, _vibrationStatus.toString())
                .apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_flash_light -> {
                when (_flashStatus) {
                    Status.ON -> {
                        morsePlayer.removeMorseOutputPlayer(_morseFlashPlayer)
                        _flashStatus = Status.OFF
                    }
                    Status.OFF -> {
                        addFlashControllerOrRequestPermission(true)
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(activity!!, "Flash not available")
                    }
                }
                updateFlashLightMenuIcon()
            }

            R.id.action_sound -> {
                when (_soundStatus) {
                    Status.ON -> {
                        morsePlayer.removeMorseOutputPlayer(_morseSoundPlayer)
                        _soundStatus = Status.OFF
                    }
                    Status.OFF -> {
                        morsePlayer.addMorseOutputPlayer(_morseSoundPlayer)
                        _soundStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(activity!!, "Sound not available")
                    }
                }
                updateSoundMenuIcon()
            }

            R.id.action_vibration -> {
                when (_vibrationStatus) {
                    Status.ON -> {
                        morsePlayer.removeMorseOutputPlayer(_morseVibrationPlayer)
                        _vibrationStatus = Status.OFF
                    }
                    Status.OFF -> {
                        morsePlayer.addMorseOutputPlayer(_morseVibrationPlayer)
                        _vibrationStatus = Status.ON
                    }
                    Status.UNAVAILABLE -> {
                        SingleToast.showShortToast(activity!!, "Vibration not available")
                    }
                }
                updateVibrationMenuIcon()
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
                    morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                    _flashStatus = Status.ON
                    updateFlashLightMenuIcon()
                }
            }
        }
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
        val drawable = ContextCompat.getDrawable(activity!!, drawableId)
        drawable?.alpha = 50
        return drawable
    }


    /**
     * Add [MorseFlashLightPlayerInterface] to the [morsePlayer].
     * As [MorseFlashLightOldPlayer] requires camera permission, if permission isn't granted, ask
     * the permission to the user. WARNING, in this case, no morsePlayer are added to [morsePlayer].
     * This method update [_flashStatus].
     * @param forceRequest if true and permission already refused, display a SnackBar to explain.
     */
    private fun addFlashControllerOrRequestPermission(forceRequest: Boolean) {
        if (_flashStatus != Status.UNAVAILABLE) {
            // if sdk version >= M -> we can use Camera2, otherwise, we need to request permission for old Camera
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                _flashStatus = Status.ON
            } else {
                if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    morsePlayer.addMorseOutputPlayer(_morseFlashPlayer)
                    _flashStatus = Status.ON
                } else {
                    // need to request permission, and check if user already refused.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {
                        if (forceRequest) {
                            Snackbar.make(mainCoordinatorLayout, R.string.camera_required, Snackbar.LENGTH_LONG)
                                    .setAction("Allow") {
                                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                                    }
                                    .show()
                        }
                    } else {
                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                    }
                    _flashStatus = Status.OFF
                }
            }
        }
    }


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
