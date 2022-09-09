package com.krish.jobspot.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    var details: Details? = null,
    var address: Address? = null,
    var academic: Academic? = null,
):Parcelable
