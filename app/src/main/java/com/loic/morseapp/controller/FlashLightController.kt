package com.loic.morseapp.controller

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log

/**
 * Implementation of MorsePlayerListenerInterface for the flash Light.
 * This implementation uses the new android.hardware.camera2
 * Allow to switch on and off the device FlashLight
 */
@RequiresApi(Build.VERSION_CODES.M)
class FlashLightController(context: Context) : FlashLightControllerInterface {

    companion object {
        const val TAG = "FlashLightController"
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    override fun switchOn() {
        try {
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)

        } catch (e: CameraAccessException) {
            if (e.reason == CameraAccessException.CAMERA_IN_USE) {
                Log.v(TAG, "Camera already in use by another app : " +
                        "android.hardware.camera2.CameraAccessException: CAMERA_IN_USE (4)")
            } else {
                Log.w(TAG, e)
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, e)
        }
    }

    override fun switchOff() {
        try {
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)

        } catch (e: CameraAccessException) {
            if (e.reason == CameraAccessException.CAMERA_IN_USE) {
                Log.v(TAG, "Camera already in use by another app : " +
                        "android.hardware.camera2.CameraAccessException: CAMERA_IN_USE (4)")
            } else {
                Log.w(TAG, e)
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, e)
        }
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished() {
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}
}
