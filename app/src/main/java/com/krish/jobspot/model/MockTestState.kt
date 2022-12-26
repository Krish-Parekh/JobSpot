package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MockTestState(
    var quizUid: String = "",
    var hasAttempted: Boolean = false,
    var quizName: String = ""
) : Parcelable
