package com.krish.jobspot.model

data class Question(
    var question: String = "",
    var options: List<String> = emptyList(),
    var correctOption: String = "",
    var feedback: String = ""
)