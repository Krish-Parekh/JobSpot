package com.krish.jobspot.util

import java.util.regex.Pattern

class InputValidation {
    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$")
        private val PASSWORD_PATTERN = Pattern.compile("^.*(?=.{4,})(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!&\$%&? \"]).*\$")

        fun checkNullity(input: String): Boolean {
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