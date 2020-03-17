package com.loic.morseapp.controller

import android.hardware.Camera
import android.hardware.Camera.Parameters
import kotlin.concurrent.thread

/**
 * Implementation of MorsePlayerListenerInterface for the flash Light.
 * This implementation uses deprecated android.hardware.camera for devices below Api 23.
 * Allow to switch on and off the device FlashLight
 */
class FlashLightOldController : MorsePlayerListenerInterface {

    private val camera = Camera.open()
    private val parameters = camera.parameters


    override fun switchOn() {
        thread(start = true) {
            parameters.flashMode = Parameters.FLASH_MODE_TORCH
            camera.parameters = parameters
            camera.startPreview()
        }
    }

    override fun switchOff() {
        thread(start = true) {
            parameters.flashMode = Parameters.FLASH_MODE_OFF
            camera.parameters = parameters
            camera.stopPreview()
        }
    }

    override fun onPlayerStarted() {
    }

    override fun onPlayerFinished() {
        // TODO Free the camera ?
        switchOff()
    }

    override fun onTotalProgressChanged(progress: Float) {}

    override fun onMorseCharacterChanged(letterIndex: Int) {}

}
