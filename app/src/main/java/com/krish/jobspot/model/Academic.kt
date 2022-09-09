package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Academic(
    var sem1: String = "",
    var sem2: String = "",
    var sem3: String = "",
    var sem4: String = "",
    var avgScore: String = "",
    var resumeUrl: String = "",
) : Parcelable
