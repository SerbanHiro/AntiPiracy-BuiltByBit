package me.serbob.antipiracy

import me.serbob.antipiracy.util.RandomUtil

object InjectionConstants {

    private val BUILTBYBIT_FIELDS = listOf(
        "%%__BUILTBYBIT__%%",
        "%%__USER__%%",
        "%%__USERNAME__%%",
        "%%__STEAM64__%%",
        "%%__STEAM32__%%",
        "%%__RESOURCE__%%",
        "%%__RESOURCE_TITLE__%%",
        "%%__VERSION__%%",
        "%%__VERSION_NUMBER__%%",
        "%%__TIMESTAMP__%%",
        "%%__NONCE__%%",
    )

    fun noncePlaceholder(): String = "%%__NONCE__%%"

    fun generateRandomFields(): List<Pair<String, String>> = BUILTBYBIT_FIELDS
        .map { RandomUtil.randomValidJavaString() to it }
}