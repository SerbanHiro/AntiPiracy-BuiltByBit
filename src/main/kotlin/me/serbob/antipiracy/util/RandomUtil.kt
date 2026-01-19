package me.serbob.antipiracy.util

object RandomUtil {

    private val numbers = '0'..'9'
    private val lowerLetters = 'a'..'z'
    private val upperLetters = 'A'..'Z'

    fun randomLength(): Int = (5..20).random()
    fun randomNumber(): Int = (1..10).random()

    fun randomLetter(): Char = ((upperLetters) + (lowerLetters)).random()
    fun randomUpperLetter(): Char = (upperLetters).random()
    fun randomLowerLetter(): Char = (lowerLetters).random()

    fun randomChar(): Char = ((upperLetters) + (lowerLetters) + (numbers)).random()
    fun randomHexChar(): Char = ((numbers) + (lowerLetters)).random()

    fun randomValidJavaString(): String = randomValidJavaString(randomLength())

    fun randomValidJavaString(
        length: Int
    ): String = buildString {
        append(randomLetter())
        repeat(length - 1) {
            append(randomChar())
        }
    }

    fun randomFakeNonce(): String = buildString {
        repeat(32) {
            append(randomHexChar())
        }
    }
}