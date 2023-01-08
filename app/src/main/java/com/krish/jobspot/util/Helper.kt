package com.krish.jobspot.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.krish.jobspot.R
import java.util.*

fun TextInputLayout.addTextWatcher() {
    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            error = null
        }
    })
}

fun TextInputEditText.clearText() {
    setText("")
    clearFocus()
}

fun TextInputEditText.getInputValue(): String {
    return text.toString().trim()
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun createSalaryText(salary: String, requireActivity: Activity): SpannableString {
    val shortSalary = convertToShortString(salary.toLong())
    val salaryText = SpannableString("₹$shortSalary/year")
    val orangeColor = ContextCompat.getColor(requireActivity, R.color.on_boarding_span_text_color)
    val greyColor = ContextCompat.getColor(requireActivity, R.color.grey)
    val salaryColor = ForegroundColorSpan(orangeColor)
    val durationColor = ForegroundColorSpan(greyColor)
    salaryText.setSpan(salaryColor, 0, salaryText.length - 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    salaryText.setSpan(
        durationColor,
        salaryText.length - 5,
        salaryText.length,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
    return salaryText
}

fun counterAnimation(start: Int, end: Int, textView: TextView) {
    val animator = ValueAnimator.ofInt(start, end)
    animator.duration = 500
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.addUpdateListener {
        val counter = it.animatedValue as Int
        textView.text = counter.toString()
    }
    animator.repeatCount = 0
    animator.start()
}


fun convertToShortString(value: Long): String {
    if (value < 1000) {
        return value.toString()
    } else if (value < 100000) {
        return "${value / 1000}K"
    } else if (value < 10000000) {
        return "${value / 100000}Lac"
    } else {
        return "${value / 10000000}Cr"
    }
}


fun checkTimeUnit(milliSeconds: Long): String {
    val seconds = milliSeconds / 1000
    val minutes = seconds / 60
    return if (minutes >= 1) {
        "${minutes}m"
    } else {
        "${seconds}s"
    }
}

fun convertTimeStamp(timestamp: Date): String {
    val timestampDate = timestamp
    val currentDate = Date()

    val elapsedTime = currentDate.time - timestampDate.time

    val elapsedDays = (elapsedTime / (1000 * 60 * 60 * 24)).toInt()

    val elapsedHours = ((elapsedTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toInt()

    val elapsedMinutes = ((elapsedTime % (1000 * 60 * 60)) / (1000 * 60)).toInt()

    val elapsedSeconds = ((elapsedTime % (1000 * 60)) / 1000).toInt()

    var elapsedTimeString = ""

    if (elapsedDays > 0) {
        elapsedTimeString = "$elapsedDays days ago"
    } else if (elapsedHours > 0) {
        elapsedTimeString = "$elapsedHours hours ago"
    } else if (elapsedMinutes > 0) {
        elapsedTimeString = "$elapsedMinutes minutes ago"
    } else if (elapsedSeconds > 0) {
        elapsedTimeString = "$elapsedSeconds seconds ago"
    }
    return elapsedTimeString
}