package com.loic.morseapp.morseconverter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Loïc DUMAS on 13/01/18.
 */
class MorseConverterTest {

    @Test
    fun convertTextToMorseTest() {
        //single Character
        assertEquals("---", MorseConverter.convertTextToMorse("o")) // lower case
        assertEquals(".", MorseConverter.convertTextToMorse("E")) // upper case
        assertEquals(".-", MorseConverter.convertTextToMorse("a"))
        assertEquals(".-.-.-", MorseConverter.convertTextToMorse(".")) // special char
        assertEquals("-.-..", MorseConverter.convertTextToMorse("ç")) // special letter
        assertEquals("--...", MorseConverter.convertTextToMorse("7")) // number
        assertEquals("ï", MorseConverter.convertTextToMorse("ï")) // unknown char
        assertEquals("~", MorseConverter.convertTextToMorse("~")) // unknown char
        assertEquals("", MorseConverter.convertTextToMorse("")) // empty text
        assertEquals(" ", MorseConverter.convertTextToMorse(" ")) // space

        // Strings
        assertEquals("... --- ...", MorseConverter.convertTextToMorse("sos")) // string
        assertEquals(".... . .-.. .-.. ---  ..-. .-. .- -. -.-  -.-.--", MorseConverter.convertTextToMorse("Hello Frank !")) // string with space
        assertEquals(".... . .-.. .-.. ---  ", MorseConverter.convertTextToMorse("Hello ")) // string ending with space
        assertEquals(" .... . .-.. .-.. ---", MorseConverter.convertTextToMorse(" Hello")) // string starting with space
        assertEquals(".... . .-.. .-.. ---    ..-. .-. .- -. -.-  -.-.--", MorseConverter.convertTextToMorse("Hello   Frank !"))
        assertEquals(".-.. --- ï -.-.", MorseConverter.convertTextToMorse("Loïc"))
    }

    @Test
    fun convertMorseToTextTest() {
        // simple character
        assertEquals("", MorseConverter.convertMorseToText("")) // empty
        assertEquals(" ", MorseConverter.convertMorseToText(" ")) // space
        assertEquals("e", MorseConverter.convertMorseToText(".")) // letter
        assertEquals("e", MorseConverter.convertMorseToText(". ")) // letter + space
        assertEquals("!", MorseConverter.convertMorseToText("-.-.--"))// known special character
        assertEquals("9", MorseConverter.convertMorseToText("----.")) // number
        assertEquals("è", MorseConverter.convertMorseToText(".-..-")) // known special letter

        // Strings
        assertEquals("sos", MorseConverter.convertMorseToText("... --- ...")) // no final space
        assertEquals("sos", MorseConverter.convertMorseToText("... --- ... ")) // with final space
        assertEquals(" sos", MorseConverter.convertMorseToText(" ... --- ... ")) // simple space begin
        assertEquals("s os", MorseConverter.convertMorseToText("...  --- ... ")) // simple space middle
        assertEquals("sos ", MorseConverter.convertMorseToText("... --- ...  ")) // simple space end
        assertEquals("  ea", MorseConverter.convertMorseToText("  . .-")) // multiple spaces begin
        assertEquals("ea  e", MorseConverter.convertMorseToText(". .-   . ")) // multiple spaces middle
        assertEquals("se  ", MorseConverter.convertMorseToText("... .   ")) // multiple spaces end
        assertEquals("   ", MorseConverter.convertMorseToText("   ")) // multiple spaces
    }

    @Test(expected = UnknownMorseCharacterException::class)
    fun convertMorseToTextWithUnknownMorseCharExceptionTest() {
        MorseConverter.convertMorseToText("-.-..--")
    }

    @Test(expected = UnexpectedCharacterException::class)
    fun convertMorseToTextWithUnexpectedCharacterExceptionTest() {
        MorseConverter.convertMorseToText("~")
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