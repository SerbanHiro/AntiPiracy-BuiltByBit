package me.serbob.antipiracy.model

data class Field(
    val name: String,
    val value: String,
    val nonce: Boolean
) {
    constructor(
        name: String,
        value: String
    ): this(name, value, false)
}