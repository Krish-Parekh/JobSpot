package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Details(
    var username: String = "",
    var email: String = "",
    var imageUrl: String = "",
    var sapId: String = "",
    var mobile: String = "",
    var dob: String = "",
    var gender: String = ""
) : Parcelable
