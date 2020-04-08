package com.loic.morseapp.morseconverter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test for [MorseConverter].
 */
class MorseConverterTest {

    @Test
    fun convertAlphaToMorseTest() {
        //single Character
        assertEquals("---", MorseConverter.convertAlphaToMorse("o")) // lower case
        assertEquals("·", MorseConverter.convertAlphaToMorse("E")) // upper case
        assertEquals("·-", MorseConverter.convertAlphaToMorse("a"))
        assertEquals("·-·-·-", MorseConverter.convertAlphaToMorse(".")) // normal dot
        assertEquals("·-·-·-", MorseConverter.convertAlphaToMorse("·")) // middle dot used as ti symbol
        assertEquals("-·-··", MorseConverter.convertAlphaToMorse("ç")) // special letter
        assertEquals("--···", MorseConverter.convertAlphaToMorse("7")) // number
        assertEquals("ï", MorseConverter.convertAlphaToMorse("ï")) // unknown char
        assertEquals("~", MorseConverter.convertAlphaToMorse("~")) // unknown char
        assertEquals("", MorseConverter.convertAlphaToMorse("")) // empty text
        assertEquals(" ", MorseConverter.convertAlphaToMorse(" ")) // space

        // Strings
        assertEquals("··· --- ···", MorseConverter.convertAlphaToMorse("sos")) // string
        assertEquals("···· · ·-·· ·-·· ---  ··-· ·-· ·- -· -·-  -·-·--", MorseConverter.convertAlphaToMorse("Hello Frank !")) // string with space
        assertEquals("···· · ·-·· ·-·· ---  ", MorseConverter.convertAlphaToMorse("Hello ")) // string ending with space
        assertEquals(" ···· · ·-·· ·-·· ---", MorseConverter.convertAlphaToMorse(" Hello")) // string starting with space
        assertEquals("···· · ·-·· ·-·· ---    ··-· ·-· ·- -· -·-  -·-·--", MorseConverter.convertAlphaToMorse("Hello   Frank !"))
        assertEquals("·-·· --- ï -·-·", MorseConverter.convertAlphaToMorse("Loïc"))
    }

    @Test
    fun convertMorseToAlphaTest() {
        // simple character
        assertEquals("", MorseConverter.convertMorseToAlpha("")) // empty
        assertEquals(" ", MorseConverter.convertMorseToAlpha(" ")) // space
        assertEquals("e", MorseConverter.convertMorseToAlpha("·")) // letter
        assertEquals("e", MorseConverter.convertMorseToAlpha("· ")) // letter + space
        assertEquals("!", MorseConverter.convertMorseToAlpha("-·-·--"))// known special character
        assertEquals("·", MorseConverter.convertMorseToAlpha("·-·-·-"))// known special character
        assertEquals("9", MorseConverter.convertMorseToAlpha("----·")) // number
        assertEquals("è", MorseConverter.convertMorseToAlpha("·-··-")) // known special letter

        // Strings
        assertEquals("sos", MorseConverter.convertMorseToAlpha("··· --- ···")) // no final space
        assertEquals("sos", MorseConverter.convertMorseToAlpha("··· --- ··· ")) // with final space
        assertEquals(" sos", MorseConverter.convertMorseToAlpha(" ··· --- ··· ")) // simple space begin
        assertEquals("s os", MorseConverter.convertMorseToAlpha("···  --- ··· ")) // simple space middle
        assertEquals("sos ", MorseConverter.convertMorseToAlpha("··· --- ···  ")) // simple space end
        assertEquals("  ea", MorseConverter.convertMorseToAlpha("  · ·-")) // multiple spaces begin
        assertEquals("ea  e", MorseConverter.convertMorseToAlpha("· ·-   · ")) // multiple spaces middle
        assertEquals("se  ", MorseConverter.convertMorseToAlpha("··· ·   ")) // multiple spaces end
        assertEquals("   ", MorseConverter.convertMorseToAlpha("   ")) // multiple spaces

        // with back to line
        assertEquals("\n", MorseConverter.convertMorseToAlpha("\n")) // simple back to line
        assertEquals("\ne", MorseConverter.convertMorseToAlpha("\n·")) // back to line with a dot
        assertEquals("\n ", MorseConverter.convertMorseToAlpha("\n ")) // back to line with a space
        assertEquals("\n \n", MorseConverter.convertMorseToAlpha("\n \n")) // multiple back to line with a space
        assertEquals("\n\n\n\n", MorseConverter.convertMorseToAlpha("\n\n\n\n")) // multiple back to line
        assertEquals("e\n", MorseConverter.convertMorseToAlpha("· \n")) // dot followed by a space the a back to line, so there's not space
        assertEquals("e \n", MorseConverter.convertMorseToAlpha("·  \n")) // like previous but with two space
        assertEquals("e\ne\na\n", MorseConverter.convertMorseToAlpha("·\n·\n·-\n")) // back to line act like a morse sequence separator

    }

    @Test(expected = UnknownMorseCharacterException::class)
    fun convertMorseToAlphaWithUnknownMorseCharExceptionTest() {
        MorseConverter.convertMorseToAlpha("-·-··--")
    }

    @Test(expected = UnexpectedCharacterException::class)
    fun convertMorseToAlphaWithUnexpectedCharacterExceptionTest() {
        MorseConverter.convertMorseToAlpha("~")
    }

    @Test
    fun isCorrectMorseCode() {
        assertTrue(MorseConverter.isValidMorseCode(""))
        assertTrue(MorseConverter.isValidMorseCode(" "))
        assertTrue(MorseConverter.isValidMorseCode("·"))
        assertTrue(MorseConverter.isValidMorseCode("-"))
        assertTrue(MorseConverter.isValidMorseCode("\n"))
        assertTrue(MorseConverter.isValidMorseCode(" ·-"))
        assertTrue(MorseConverter.isValidMorseCode("   ···· ----"))

        assertFalse(MorseConverter.isValidMorseCode("e"))
        assertFalse(MorseConverter.isValidMorseCode("--22"))
    }
}