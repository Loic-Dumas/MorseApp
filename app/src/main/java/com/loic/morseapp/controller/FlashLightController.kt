package com.loic.morseapp.controller

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.RequiresApi

/**
 * Implementation of MorsePlayerListenerInterface for the flash Light.
 * This implementation uses the new android.hardware.camera2
 * Allow to switch on and off the device FlashLight
 */
@RequiresApi(Build.VERSION_CODES.M)
class FlashLightController(context: Context) : MorsePlayerListenerInterface {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    override fun switchOn() {
        cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
    }

    override fun switchOff() {
        cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
    }

    override fun onPlayerStarted() {}

    override fun onPlayerFinished() {
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}

}
