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
import java.time.LocalTime
import java.util.*

/**
 * Adds a text watcher to the TextInputLayout's EditText to clear its error message when its text changes.
 */
fun TextInputLayout.addTextWatcher() {
    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            error = null
        }
    })
}

/**
 * Clears the text of the TextInputEditText and removes its focus.
 */
fun TextInputEditText.clearText() {
    setText("")
    clearFocus()
}

/**
 * Returns the trimmed input value of the TextInputEditText as a String.
 *
 * @return The input value of the TextInputEditText as a String.
 */
fun TextInputEditText.getInputValue(): String {
    return text.toString().trim()
}

/**
 * Shows a toast message with the given message string.
 *
 * @param context The Context to use for creating the toast message.
 * @param message The message string to display in the toast message.
 */
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * Creates and returns a formatted SpannableString that represents a salary value.
 *
 * @param salary The salary value as a String.
 * @param requireActivity The Activity object to use for getting color resources.
 * @return The formatted SpannableString representing the salary value.
 */
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

/**
 * Animates a counter value from the start value to the end value in a TextView.
 *
 * @param start The start value of the counter.
 * @param end The end value of the counter.
 * @param textView The TextView to display the counter value.
 */
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

/**
 * Converts a long value to a short string representation with a suffix.
 *
 * @param value The long value to convert.
 * @return The short string representation of the value with a suffix.
 */
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

/**
 * Returns the duration in minutes or seconds based on the provided milliseconds.
 *
 * @param milliSeconds The duration in milliseconds.
 * @return The duration in minutes (m) or seconds (s).
 */
fun checkTimeUnit(milliSeconds: Long): String {
    val seconds = milliSeconds / 1000
    val minutes = seconds / 60
    return if (minutes >= 1) {
        "${minutes}m"
    } else {
        "${seconds}s"
    }
}

/**
 * Converts a given [timestamp] date into a human-readable elapsed time string.
 *
 * @param timestamp The date to convert to elapsed time.
 * @return A string representing the elapsed time between the current date and the given [timestamp] date.
 */
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

fun getGreeting(): String {
    val currentTime = LocalTime.now() // get the current time in the local timezone
    val hour = currentTime.hour // get the current hour
    return when(hour) {
        in 0..11 -> "Good Morning!" // if the current hour is between midnight and 11am
        in 12..16 -> "Good Afternoon!" // if the current hour is between noon and 4pm
        in 17..23 -> "Good Evening!" // if the current hour is between 5pm and midnight
        else -> "Hello!" // if the current hour is invalid (e.g. negative or greater than 23)
    }
}