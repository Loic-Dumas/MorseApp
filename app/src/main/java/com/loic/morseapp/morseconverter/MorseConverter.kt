package com.loic.morseapp.morseconverter

/**
 * Convert some text to Morse code, or decode Morse
 * Created by Loïc DUMAS on 29/12/17.
 */
class MorseConverter {

    private val _letterToMorseArray = hashMapOf(
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


    private val _morseToLetterArray = hashMapOf(
            //letters
            ".-" to 'a',
            "-..." to 'b',
            "-.-." to 'c',
            "-.." to 'd',
            "." to 'e',
            "..-." to 'f',
            "--." to 'g',
            "...." to 'h',
            ".." to 'i',
            ".---" to 'j',
            "-.-" to 'k',
            ".-.." to 'l',
            "--" to 'm',
            "-." to 'n',
            "---" to 'o',
            ".--." to 'p',
            "--.-" to 'q',
            ".-." to 'r',
            "..." to 's',
            "-" to 't',
            "..-" to 'u',
            "...-" to 'v',
            ".--" to 'w',
            "-..-" to 'x',
            "-.--" to 'y',
            "--.." to 'z',
            // numbers
            "-----" to '0',
            ".----" to '1',
            "..---" to '2',
            "...--" to '3',
            "....-" to '4',
            "....." to '5',
            "-...." to '6',
            "--..." to '7',
            "---.." to '8',
            "----." to '9',
            // symbols
            "-.-.--" to '!',
            ".-..-." to '"',
            "...-..-" to '$',
            ".----." to '\'',
            "-.--." to '(',
            "-.--.-" to ')',
            ".-.-." to '+',
            "--..--" to ',',
            "-....-" to '-',
            ".-.-.-" to '.',
            "-..-." to '/',
            "---..." to ':',
            "-.-.-." to ',',
            "-...-" to '=',
            "..--.." to '?',
            ".--.-." to '@',
            // special letters
            ".--.-" to 'à',
            "..-.." to 'é',
            ".-..-" to 'è',
            "-.-.." to 'ç',
            "..--" to 'ü'
    )

    /**
     * @return The text given in parameter into a string of morse code.
     * If a letter isn't known, the letter isn't translated
     */
    fun convertTextToMorse(text: String): String {
        var result = ""

        for (letter in text.toLowerCase()) {
            val morseLetter = _letterToMorseArray[letter]
            result += when {
                morseLetter != null -> morseLetter + " "
                letter.equals(" ") -> "     "
                else -> letter + " "
            }
        }
        return result.removeSuffix(" ")
    }

    /**
     * Transform a Morse code to a String
     * @param morseCode the morseSequence to decode in text.
     *        This String should be composed by '-', '.' or ' '
     * @throws Exception if the string contains other characters than '-', '.' or ' '
     */
    fun convertMorseToText(morseCode: String): String {
        var result = ""

        var idx = 0
        var currentChar: Char
        var currentMorseLetter = ""

        while (idx < morseCode.length) {
            currentChar = morseCode[idx]

            when (currentChar) {
                '-', '.' -> {
                    currentMorseLetter += currentChar
                    idx++
                }
                ' ' -> {
                    if (!currentMorseLetter.isEmpty() && _morseToLetterArray[currentMorseLetter] != null) {
                        result += _morseToLetterArray[currentMorseLetter]
                        idx++
                        currentMorseLetter = ""
                    } else {
                        result += " "
                        while (idx < morseCode.length && morseCode[idx] == ' ') {
                            idx++
                        }
                    }
                }
                else -> {
                    throw Exception("Unexpected character : $currentChar")
                }
            }

        }
        if (!currentMorseLetter.isEmpty() && _morseToLetterArray[currentMorseLetter] != null) {
            result += _morseToLetterArray[currentMorseLetter]
        }

        return result
    }
}
