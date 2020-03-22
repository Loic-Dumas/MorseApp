package com.loic.morseapp.morseconverter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit test for [MorseConverter].
 */
class MorseConverterTest {

    @Test
    fun convertAlphaToMorseTest() {
        //single Character
        assertEquals("---", MorseConverter.convertAlphaToMorse("o")) // lower case
        assertEquals(".", MorseConverter.convertAlphaToMorse("E")) // upper case
        assertEquals(".-", MorseConverter.convertAlphaToMorse("a"))
        assertEquals(".-.-.-", MorseConverter.convertAlphaToMorse(".")) // special char
        assertEquals("-.-..", MorseConverter.convertAlphaToMorse("ç")) // special letter
        assertEquals("--...", MorseConverter.convertAlphaToMorse("7")) // number
        assertEquals("ï", MorseConverter.convertAlphaToMorse("ï")) // unknown char
        assertEquals("~", MorseConverter.convertAlphaToMorse("~")) // unknown char
        assertEquals("", MorseConverter.convertAlphaToMorse("")) // empty text
        assertEquals(" ", MorseConverter.convertAlphaToMorse(" ")) // space

        // Strings
        assertEquals("... --- ...", MorseConverter.convertAlphaToMorse("sos")) // string
        assertEquals(".... . .-.. .-.. ---  ..-. .-. .- -. -.-  -.-.--", MorseConverter.convertAlphaToMorse("Hello Frank !")) // string with space
        assertEquals(".... . .-.. .-.. ---  ", MorseConverter.convertAlphaToMorse("Hello ")) // string ending with space
        assertEquals(" .... . .-.. .-.. ---", MorseConverter.convertAlphaToMorse(" Hello")) // string starting with space
        assertEquals(".... . .-.. .-.. ---    ..-. .-. .- -. -.-  -.-.--", MorseConverter.convertAlphaToMorse("Hello   Frank !"))
        assertEquals(".-.. --- ï -.-.", MorseConverter.convertAlphaToMorse("Loïc"))
    }

    @Test
    fun convertMorseToAlphaTest() {
        // simple character
        assertEquals("", MorseConverter.convertMorseToAlpha("")) // empty
        assertEquals(" ", MorseConverter.convertMorseToAlpha(" ")) // space
        assertEquals("e", MorseConverter.convertMorseToAlpha(".")) // letter
        assertEquals("e", MorseConverter.convertMorseToAlpha(". ")) // letter + space
        assertEquals("!", MorseConverter.convertMorseToAlpha("-.-.--"))// known special character
        assertEquals("9", MorseConverter.convertMorseToAlpha("----.")) // number
        assertEquals("è", MorseConverter.convertMorseToAlpha(".-..-")) // known special letter

        // Strings
        assertEquals("sos", MorseConverter.convertMorseToAlpha("... --- ...")) // no final space
        assertEquals("sos", MorseConverter.convertMorseToAlpha("... --- ... ")) // with final space
        assertEquals(" sos", MorseConverter.convertMorseToAlpha(" ... --- ... ")) // simple space begin
        assertEquals("s os", MorseConverter.convertMorseToAlpha("...  --- ... ")) // simple space middle
        assertEquals("sos ", MorseConverter.convertMorseToAlpha("... --- ...  ")) // simple space end
        assertEquals("  ea", MorseConverter.convertMorseToAlpha("  . .-")) // multiple spaces begin
        assertEquals("ea  e", MorseConverter.convertMorseToAlpha(". .-   . ")) // multiple spaces middle
        assertEquals("se  ", MorseConverter.convertMorseToAlpha("... .   ")) // multiple spaces end
        assertEquals("   ", MorseConverter.convertMorseToAlpha("   ")) // multiple spaces
    }

    @Test(expected = UnknownMorseCharacterException::class)
    fun convertMorseToAlphaWithUnknownMorseCharExceptionTest() {
        MorseConverter.convertMorseToAlpha("-.-..--")
    }

    @Test(expected = UnexpectedCharacterException::class)
    fun convertMorseToAlphaWithUnexpectedCharacterExceptionTest() {
        MorseConverter.convertMorseToAlpha("~")
    }

    @Test
    fun isCorrectMorseCode() {
        assertEquals(true, MorseConverter.isValidMorseCode(""))
        assertEquals(true, MorseConverter.isValidMorseCode(" "))
        assertEquals(true, MorseConverter.isValidMorseCode("."))
        assertEquals(true, MorseConverter.isValidMorseCode("-"))
        assertEquals(true, MorseConverter.isValidMorseCode(" .-"))
        assertEquals(true, MorseConverter.isValidMorseCode("   .... ----"))
        assertEquals(false, MorseConverter.isValidMorseCode("e"))
        assertEquals(false, MorseConverter.isValidMorseCode("--22"))
    }
}