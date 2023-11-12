package com.example.hack.ui.model

import com.example.hack.data.ModelExcel
import java.io.Serializable

data class ModelForResult(
    var test: MutableList<Question>? = null,
    var gameDev: MutableList<ModelExcel>? = null,
):Serializable
