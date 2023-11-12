package com.example.hack.ui.model

data class Question(
    val text: String,
    val one: String,
    val two: String,
    val three: String,
    val four: String,
    val rightVariant: Int,
    var userVariant: Int? = null,
    var isCheck: Boolean? = null,
)