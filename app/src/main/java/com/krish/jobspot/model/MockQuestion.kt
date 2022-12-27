package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MockQuestion(
    var question: String = "",
    var options: List<String> = emptyList(),
    var correctOption: String = "",
    var feedback: String = ""
) : Parcelable