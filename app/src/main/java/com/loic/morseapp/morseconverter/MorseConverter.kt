package com.loic.morseapp.morseconverter

import java.util.*

/**
 * Convert alpha text to Morse code, or decode Morse
 */
class MorseConverter {

    companion object {
        private val alphaToMorseArray = hashMapOf(
                //letters
                'a' to "·-",
                'b' to "-···",
                'c' to "-·-·",
                'd' to "-··",
                'e' to "·",
                'f' to "··-·",
                'g' to "--·",
                'h' to "····",
                'i' to "··",
                'j' to "·---",
                'k' to "-·-",
                'l' to "·-··",
                'm' to "--",
                'n' to "-·",
                'o' to "---",
                'p' to "·--·",
                'q' to "--·-",
                'r' to "·-·",
                's' to "···",
                't' to "-",
                'u' to "··-",
                'v' to "···-",
                'w' to "·--",
                'x' to "-··-",
                'y' to "-·--",
                'z' to "--··",
                // numbers
                '0' to "-----",
                '1' to "·----",
                '2' to "··---",
                '3' to "···--",
                '4' to "····-",
                '5' to "·····",
                '6' to "-····",
                '7' to "--···",
                '8' to "---··",
                '9' to "----·",
                // symbols
                '!' to "-·-·--",
                '"' to "·-··-·",
                '$' to "···-··-",
                '\'' to "·----·",
                '(' to "-·--·",
                ')' to "-·--·-",
                '+' to "·-·-·",
                ',' to "--··--",
                '-' to "-····-",
                '.' to "·-·-·-",
                '/' to "-··-·",
                ':' to "---···",
                ',' to "-·-·-·",
                '=' to "-···-",
                '?' to "··--··",
                '@' to "·--·-·",
                // special letters
                'à' to "·--·-",
                'é' to "··-··",
                'è' to "·-··-",
                'ç' to "-·-··",
                'ü' to "··--"
        )

        private val morseToAlphaArray = alphaToMorseArray.entries.associate { (k, v) -> v to k }

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
                    letter == '\n' -> "\n" // a back to line
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
         *        This String should be composed by '-', '·', '\n' or ' '
         * @throws UnexpectedCharacterException if the string contains other characters than '-', '·', '\n' or ' '
         * @throws UnknownMorseCharacterException if a sequence of dot/dash cannot be translated.
         */
        fun convertMorseToAlpha(morseString: String): String {
            var result = ""
            var currentMorseSequence = ""

            for (stringIndex in morseString.indices) {

                when (val currentChar = morseString[stringIndex]) {
                    // continue to digest the current morse sequence
                    '-', '·' -> {
                        currentMorseSequence += currentChar
                    }
                    // end of a morse sequence or a space or return to line
                    ' ', '\n' -> {
                        if (currentMorseSequence.isNotEmpty()) { // we are analysing a morse sequence
                            if (morseToAlphaArray[currentMorseSequence] != null) { // The morse sequence exist
                                result += morseToAlphaArray[currentMorseSequence]
                                currentMorseSequence = ""
                            } else
                                throw UnknownMorseCharacterException(currentMorseSequence)
                        } else if (currentChar == ' ') { // no morseSequence, so it's a space
                            result += ' '
                        }

                        if (currentChar == '\n') { // always add a return, even if it's the en of a morse sequence
                            result += '\n'
                        }
                    }

                    else -> { // the string should be composed only by dots, dashes, \n or spaces
                        throw UnexpectedCharacterException("Unexpected character : $currentChar")
                    }
                }
            }

            // finally, check if there's still a morse sequence in progress
            if (currentMorseSequence.isNotEmpty()) {
                if (morseToAlphaArray[currentMorseSequence] != null) // The morse character exist
                    result += morseToAlphaArray[currentMorseSequence]
                else
                    throw UnknownMorseCharacterException(currentMorseSequence)
            }

            return result
        }


        /**
         * Check if the morse code contain invalid characters
         */
        fun isValidMorseCode(morseCode: String): Boolean {
            return morseCode.matches("[-· \n]*".toRegex())
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
