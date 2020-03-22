package com.loic.morseapp.morseconverter

import java.util.*

/**
 * Convert alpha text to Morse code, or decode Morse
 */
class MorseConverter {

    companion object {
        private val alphaToMorseArray = hashMapOf(
                //letters
                'a' to ".-",
                'b' to "-...",
                'c' to "-.-.",
                'd' to "-..",
                'e' to ".",
                'f' to "..-.",
                'g' to "--.",
                'h' to "....",
                'i' to "..",
                'j' to ".---",
                'k' to "-.-",
                'l' to ".-..",
                'm' to "--",
                'n' to "-.",
                'o' to "---",
                'p' to ".--.",
                'q' to "--.-",
                'r' to ".-.",
                's' to "...",
                't' to "-",
                'u' to "..-",
                'v' to "...-",
                'w' to ".--",
                'x' to "-..-",
                'y' to "-.--",
                'z' to "--..",
                // numbers
                '0' to "-----",
                '1' to ".----",
                '2' to "..---",
                '3' to "...--",
                '4' to "....-",
                '5' to ".....",
                '6' to "-....",
                '7' to "--...",
                '8' to "---..",
                '9' to "----.",
                // symbols
                '!' to "-.-.--",
                '"' to ".-..-.",
                '$' to "...-..-",
                '\'' to ".----.",
                '(' to "-.--.",
                ')' to "-.--.-",
                '+' to ".-.-.",
                ',' to "--..--",
                '-' to "-....-",
                '.' to ".-.-.-",
                '/' to "-..-.",
                ':' to "---...",
                ',' to "-.-.-.",
                '=' to "-...-",
                '?' to "..--..",
                '@' to ".--.-.",
                // special letters
                'à' to ".--.-",
                'é' to "..-..",
                'è' to ".-..-",
                'ç' to "-.-..",
                'ü' to "..--"
        )

        private val morseToAphaArray = alphaToMorseArray.entries.associate { (k, v) -> v to k }

        /**
         * @return The alpha text in parameter into a string of morse code.
         * If a letter isn't known, the letter isn't translated.
         */
        fun convertAlphaToMorse(alphaText: String): String {
            var result = ""

            for (letter in alphaText.toLowerCase(Locale.getDefault())) {
                val morseLetter = alphaToMorseArray[letter]
                result += when {
                    morseLetter != null -> "$morseLetter " // the letter is in the morse dictionary
                    letter == ' ' -> " " // a space
                    else -> "$letter " // unknown letter
                }
            }

            // if the last letter is a character, remove the last ending space
            if (alphaText.isNotEmpty() && alphaText.last() != ' ') result = result.removeSuffix(" ")

            return result
        }

        /**
         * Transform a Morse code to a alpha text.
         * @param morseString the morseSequence to decode in alpha text.
         *        This String should be composed by '-', '.' or ' '
         * @throws UnexpectedCharacterException if the string contains other characters than '-', '.' or ' '
         * @throws UnknownMorseCharacterException if a sequence of dot/dash cannot be translated.
         */
        fun convertMorseToAlpha(morseString: String): String {
            var result = ""

            var morseStringIndex = 0
            var currentChar: Char
            var currentMorseLetter = ""

            while (morseStringIndex < morseString.length) {
                currentChar = morseString[morseStringIndex]

                when (currentChar) {
                    '-', '.' -> { // continue to digest current morse char
                        currentMorseLetter += currentChar
                        morseStringIndex++
                    }
                    ' ' -> { // end of a morse char or space
                        if (currentMorseLetter.isNotEmpty()) { // a morse character is being analyzed
                            if (morseToAphaArray[currentMorseLetter] != null) // The morse character exist
                                result += morseToAphaArray[currentMorseLetter]
                            else
                                throw UnknownMorseCharacterException(currentMorseLetter)
                            morseStringIndex++
                            currentMorseLetter = ""
                        } else { // a space
                            result += " "
                            morseStringIndex++
                        }
                    }
                    else -> { // the string should be composed only by dots, dashes or spaces
                        throw UnexpectedCharacterException("Unexpected character : $currentChar")
                    }
                }

            }
            if (currentMorseLetter.isNotEmpty()) { // a morse character is being analyzed
                if (morseToAphaArray[currentMorseLetter] != null) // The morse character exist
                    result += morseToAphaArray[currentMorseLetter]
                else
                    throw UnknownMorseCharacterException(currentMorseLetter)
            }

            return result
        }


        /**
         * Check if the morse code contain invalid characters
         */
        fun isValidMorseCode(morseCode: String): Boolean {
            return morseCode.matches("[-. ]*".toRegex())
        }
    }
}

class UnknownMorseCharacterException(val morseChar: String) : Exception() {
    override val message: String
        get() = "Not recognized morse character : $morseChar"
}


class UnexpectedCharacterException(val char: String) : Exception() {
    override val message: String
        get() = "Unexpected character : $char"
}
