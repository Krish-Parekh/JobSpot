package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var zipCode: String = "",
) : Parcelable
