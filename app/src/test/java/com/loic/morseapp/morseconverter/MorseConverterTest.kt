package com.loic.morseapp.morseconverter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Loïc DUMAS on 13/01/18.
 */
class MorseConverterTest {

    @Test
    fun convertTextToMorseTest() {
        val morseConverter = MorseConverter()
        //single Character
        assertEquals("---", morseConverter.convertTextToMorse("o")) // lower case
        assertEquals(".", morseConverter.convertTextToMorse("E")) // upper case
        assertEquals(".-", morseConverter.convertTextToMorse("a"))
        assertEquals(".-.-.-", morseConverter.convertTextToMorse(".")) // special char
        assertEquals("-.-..", morseConverter.convertTextToMorse("ç")) // special letter
        assertEquals("--...", morseConverter.convertTextToMorse("7")) // number
        assertEquals("ï", morseConverter.convertTextToMorse("ï")) // unknown char
        assertEquals("~", morseConverter.convertTextToMorse("~")) // unknown char
        assertEquals("", morseConverter.convertTextToMorse("")) // empty text
        assertEquals(" ", morseConverter.convertTextToMorse(" ")) // space

        // Strings
        assertEquals("... --- ...", morseConverter.convertTextToMorse("sos")) // string
        assertEquals(".... . .-.. .-.. ---  ..-. .-. .- -. -.-  -.-.--", morseConverter.convertTextToMorse("Hello Frank !")) // string with space
        assertEquals(".... . .-.. .-.. ---  ", morseConverter.convertTextToMorse("Hello ")) // string ending with space
        assertEquals(" .... . .-.. .-.. ---", morseConverter.convertTextToMorse(" Hello")) // string starting with space
        assertEquals(".... . .-.. .-.. ---    ..-. .-. .- -. -.-  -.-.--", morseConverter.convertTextToMorse("Hello   Frank !"))
        assertEquals(".-.. --- ï -.-.", morseConverter.convertTextToMorse("Loïc"))
    }

    @Test
    fun convertMorseToTextTest() {
        val morseConverter = MorseConverter()
        // simple character
        assertEquals("", morseConverter.convertMorseToText("")) // empty
        assertEquals(" ", morseConverter.convertMorseToText(" ")) // space
        assertEquals("e", morseConverter.convertMorseToText(".")) // letter
        assertEquals("e", morseConverter.convertMorseToText(". ")) // letter + space
        assertEquals("!", morseConverter.convertMorseToText("-.-.--"))// known special character
        assertEquals("9", morseConverter.convertMorseToText("----.")) // number
        assertEquals("è", morseConverter.convertMorseToText(".-..-")) // known special letter

        // Strings
        assertEquals("sos", morseConverter.convertMorseToText("... --- ...")) // no final space
        assertEquals("sos", morseConverter.convertMorseToText("... --- ... ")) // with final space
        assertEquals(" sos", morseConverter.convertMorseToText(" ... --- ... ")) // simple space begin
        assertEquals("s os", morseConverter.convertMorseToText("...  --- ... ")) // simple space middle
        assertEquals("sos ", morseConverter.convertMorseToText("... --- ...  ")) // simple space end
        assertEquals("  ea", morseConverter.convertMorseToText("  . .-")) // multiple spaces begin
        assertEquals("ea  e", morseConverter.convertMorseToText(". .-   . ")) // multiple spaces middle
        assertEquals("se  ", morseConverter.convertMorseToText("... .   ")) // multiple spaces end
        assertEquals("   ", morseConverter.convertMorseToText("   ")) // multiple spaces
    }

    @Test(expected = UnknownMorseCharacterException::class)
    fun convertMorseToTextWithUnknownMorseCharExceptionTest() {
        MorseConverter().convertMorseToText("-.-..--")
    }

    @Test(expected = UnexpectedCharacterException::class)
    fun convertMorseToTextWithUnexpectedCharacterExceptionTest() {
        MorseConverter().convertMorseToText("~")
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