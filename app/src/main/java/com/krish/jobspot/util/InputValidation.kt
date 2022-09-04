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

        fun sapIdValidation(sapId: String) : Boolean{
            return checkNullity(sapId) && sapId.length == 11
        }

        fun mobileValidation(mobile: String) : Boolean{
            return checkNullity(mobile) && mobile.length == 10
        }

        fun dobValidation(dob: String) : Boolean{
            return checkNullity(dob)
        }

        fun genderValidation(gender: String) : Boolean{
            return checkNullity(gender)
        }

        fun addressValidation(address: String) : Boolean{
            return checkNullity(address)
        }

        fun cityValidation(city: String) : Boolean{
            return checkNullity(city)
        }

        fun stateValidation(state: String): Boolean{
            return checkNullity(state)
        }

        fun zipCodeValidation(zipCode : String) : Boolean{
            return checkNullity(zipCode)
        }

        fun scoreValidation(score : String) : Boolean{
            return checkNullity(score) && (score.toInt() in 1..10)
        }
    }
}