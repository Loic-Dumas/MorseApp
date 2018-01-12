package com.loic.morseapp.morseconverter

/**
 * Convert some text to Morse code, or decode Morse
 * Created by Loïc DUMAS on 29/12/17.
 */
class MorseConverter {

    private val letterToMorse = hashMapOf('a' to ".-",
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
            '@' to ".--.-."
    )


    private val morseToLetter = hashMapOf(".-" to 'a',
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
            ".--.-." to '@'
    )


    fun convertTextToMorse(text: String): String {
        var result = ""

        for (letter in text.toLowerCase()) {
            result += letterToMorse[letter] + " "
        }
        return result
    }

    fun convertMorseToText(morse: String): String {
        var result = ""

        val parsedMorse = morse.split(" ")
        for (letter in parsedMorse) {
            result += morseToLetter[letter]
        }

        return result
    }
}