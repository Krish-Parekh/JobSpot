package com.krish.jobspot.util

import java.util.regex.Pattern

class InputValidation {
    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}\$", Pattern.CASE_INSENSITIVE)
        private val PASSWORD_PATTERN = Pattern.compile("^(?=.*[@#$%^&+=])(?=\\S+$).{4,}$")

        private fun checkNullity(input: String): Boolean {
            return input.isNotEmpty();
        }

        fun emailValidation(email: String): Boolean {
            return checkNullity(email) && EMAIL_ADDRESS_PATTERN.matcher(email).matches()
        }

        fun passwordValidation(password: String): Boolean {
            return checkNullity(password) && PASSWORD_PATTERN.matcher(password).matches()
        }
    }
}