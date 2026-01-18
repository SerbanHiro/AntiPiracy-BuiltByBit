package me.serbob.antipiracy.util

object RandomUtil {

    fun randomLength(): Int = (5..20).random()
    fun randomNumber(): Int = (1..10).random()

    fun randomLetter(): Char = (('A'..'Z') + ('a'..'z')).random()
    fun randomUpperLetter(): Char = ('A'..'Z').random()
    fun randomLowerLetter(): Char = ('a'..'z').random()

    fun randomChar(): Char = (('A'..'Z') + ('a'..'z') + ('0'..'9')).random()
    fun randomHexChar(): Char = (('0'..'9') + ('a'..'f')).random()

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