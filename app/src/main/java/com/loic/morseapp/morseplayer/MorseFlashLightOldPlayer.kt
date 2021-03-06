@file:Suppress("DEPRECATION")

package com.loic.morseapp.morseplayer

import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.hardware.Camera.open
import android.util.Log
import kotlin.concurrent.thread

/**
 * Implementation of MorseFlashLightPlayerInterface for the flash Light.
 *
 * Warning, this class
 * This implementation uses deprecated android.hardware.camera for devices below Api 23.
 * Thread are needed as Camera is very slow.
 * Allow to switch on and off the device FlashLight.
 */
class MorseFlashLightOldPlayer : MorseFlashLightPlayerInterface {

    companion object {
        const val TAG = "MorseFlashLightOld"
    }

    private var camera: Camera? = null
    private var parameters: Parameters? = null

    override fun onPlayerAdded() {
        try {
            camera = open()
            parameters = camera?.parameters

        } catch (e: Exception) {
            Log.d(TAG, "Exception received when trying to open the camera. " +
                    "Camera probably already in use : $e")
        }
    }

    override fun onPlayerRemoved() {
        try {
            camera?.release()
            camera = null
            parameters = null

        } catch (e: Exception) {
            Log.d(TAG, "Exception received when trying to release the Camera. " +
                    "Camera probably already in use by another device, or already release :  $e")
        }
    }

    override fun switchOn() {
        thread(start = true) {
            try {
                if (camera == null) {
                    camera = open()
                    parameters = camera?.parameters
                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception received when trying to open the camera in switchOn()." +
                        "Camera was null : $e")
            }

            try {
                parameters?.flashMode = Parameters.FLASH_MODE_TORCH
                camera?.parameters = parameters
                camera?.startPreview()

            } catch (e: Exception) {
                camera = null
                Log.d(TAG, "Exception received when trying to switch on the flash. " +
                        "Camera probably already in use : $e")
            }
        }
    }

    override fun switchOff() {
        thread(start = true) {
            try {
                parameters?.flashMode = Parameters.FLASH_MODE_OFF
                camera?.parameters = parameters
                camera?.stopPreview()

            } catch (e: Exception) {
                camera = null
                Log.d(TAG, "Exception received when trying to switch off the flash. " +
                        "Camera probably already in use or null : $e")

            }
        }
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished(morseCodeFullyPlayed: Boolean) {}

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}
}
