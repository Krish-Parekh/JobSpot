package com.krish.jobspot.model

data class Quiz(
    var uid: String = System.currentTimeMillis().toString(),
    var title: String = "",
    var duration: String = "",
    var question: List<Question> = emptyList()
)