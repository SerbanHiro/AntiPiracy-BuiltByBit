package me.serbob.antipiracy

import me.serbob.antipiracy.model.Field

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
    )

    fun generateFieldTemplates(): List<Field> = BUILTBYBIT_FIELDS
        .map { Field("", it, false) }

    fun generateDefaultNonceTemplate(): Field =
        Field("", "%%__NONCE__%%", true)
}