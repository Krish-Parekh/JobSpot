package com.krish.jobspot.util


import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InputValidationTest {

    // Username Validation
    @Test
    fun `isUsernameValid should return false when username is empty`() {
        val (valid, message) = InputValidation.isUsernameValid("")
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Username cannot be empty.")
    }

    @Test
    fun `isUsernameValid should return false when username is more than 100 characters`() {
        val (valid, message) = InputValidation.isUsernameValid("a".repeat(101))
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Username cannot be more than 100 characters.")
    }

    @Test
    fun `isUsernameValid should return false when username contains non-alphanumeric characters`() {
        val (valid, message) = InputValidation.isUsernameValid("user@name")
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Username can only contain alphabets and numbers.")
    }

    @Test
    fun `isUsernameValid should return true when username is valid`() {
        val (valid, message) = InputValidation.isUsernameValid("username")
        assertThat(valid).isTrue()
        assertThat(message).isEqualTo("")
    }

    // Email Validation
    @Test
    fun `isEmailValid should return false when email is empty`() {
        val (valid, message) = InputValidation.isEmailValid("")
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Email cannot be empty.")
    }

    @Test
    fun `isEmailValid should return false when email is not valid`() {
        val (valid, message) = InputValidation.isEmailValid("notvalidemail")
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Email is not valid.")
    }

    @Test
    fun `isEmailValid should return true when email is valid`() {
        val (valid, message) = InputValidation.isEmailValid("valid@email.com")
        assertThat(valid).isTrue()
        assertThat(message).isEqualTo("")
    }

    // Password Validation
    @Test
    fun `isPasswordValid should return false when password is not valid`() {
        val (valid, message) = InputValidation.isPasswordValid("password")
        assertThat(valid).isFalse()
        assertThat(message).isEqualTo("Password is not valid.")
    }

    @Test
    fun `isPasswordValid should return true when password is valid`() {
        val (valid, message) = InputValidation.isPasswordValid("P@ssw0rd!")
        assertThat(valid).isTrue()
        assertThat(message).isEqualTo("")
    }

    // SAP Id Validation
    @Test
    fun `isSapIdValid should return true for valid sapId`() {
        val (isValid, errorMessage) = InputValidation.isSapIdValid("53003205035")
        assertThat(isValid).isTrue()
        assertThat(errorMessage).isEmpty()
    }

    @Test
    fun `isSapIdValid should return false for empty sapId`() {
        val (isValid, errorMessage) = InputValidation.isSapIdValid("")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("SAP ID cannot be empty.")
    }

    @Test
    fun `isSapIdValid should return false for sapId with length not equal to 11`() {
        val (isValid, errorMessage) = InputValidation.isSapIdValid("123456789")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("SAP ID must be 11 characters.")
    }

    @Test
    fun `isSapIdValid should return false for sapId with non-digit characters`() {
        val (isValid, errorMessage) = InputValidation.isSapIdValid("12345678a11")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("SAP ID can only contain digits.")
    }

    // Mobile Validation
    @Test
    fun `isMobileNumberValid should return true for valid mobile number`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("9876543210")
        assertThat(isValid).isTrue()
        assertThat(errorMessage).isEmpty()
    }

    @Test
    fun `isMobileNumberValid should return false for empty mobile number`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Mobile number cannot be empty.")
    }

    @Test
    fun `isMobileNumberValid should return false for mobile number not being 13 characters long`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("98765432")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Mobile number must be 10 characters.")
    }

    @Test
    fun `isMobileNumberValid should return false for mobile number containing non-digit characters`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("9876543a01")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Mobile number can only contain digits.")
    }

    @Test
    fun `isMobileNumberValid should return false for mobile number starting with 000`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("0007654321")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Mobile number cannot start with 000.")
    }

    @Test
    fun `isMobileNumberValid should return false for mobile number with all digits same`() {
        val (isValid, errorMessage) = InputValidation.isMobileNumberValid("1111111111")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("All the digits in mobile number cannot be same.")
    }

    // DOB Validation
    @Test
    fun `isDOBValid should return true for valid dob`() {
        val (isValid, errorMessage) = InputValidation.isDOBValid("2003-01-01")
        assertThat(isValid).isTrue()
        assertThat(errorMessage).isEmpty()
    }

    @Test
    fun `isDOBValid should return false for empty dob`() {
        val (isValid, errorMessage) = InputValidation.isDOBValid("")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Date of Birth cannot be empty.")
    }

    @Test
    fun `isDOBValid should return false for dob with age less than 18`() {
        val (isValid, errorMessage) = InputValidation.isDOBValid("2022-01-01")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Age must be 18 years or older.")
    }

    // Address Validation
    @Test
    fun `isAddressValid should return true and an empty string for a valid address`() {
        val (isValid, message) = InputValidation.isAddressValid("123 Main St")
        assertThat(isValid).isTrue()
        assertThat(message).isEmpty()
    }

    @Test
    fun `isAddressValid should return false and the correct error message for an empty address`() {
        val (isValid, message) = InputValidation.isAddressValid("")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("Address cannot be empty.")
    }

    @Test
    fun `isAddressValid should return false and the correct error message for an address that is too long`() {
        val (isValid, message) = InputValidation.isAddressValid("a".repeat(201))
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("Address cannot be more than 200 characters.")
    }

    // City Validation
    @Test
    fun `isCityValid should return true and an empty string for a valid city`() {
        val (isValid, message) = InputValidation.isCityValid("New York City")
        assertThat(isValid).isTrue()
        assertThat(message).isEmpty()
    }

    @Test
    fun `isCityValid should return false and the correct error message for an empty city`() {
        val (isValid, message) = InputValidation.isCityValid("")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("City cannot be empty.")
    }

    @Test
    fun `isCityValid should return false and the correct error message for a city that contains non-letter characters`() {
        val (isValid, message) = InputValidation.isCityValid("New York City123")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("City can only contain letters and spaces.")
    }

    @Test
    fun `isCityValid should return false and the correct error message for a city that is too long`() {
        val (isValid, message) = InputValidation.isCityValid("a".repeat(101))
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("City cannot be more than 100 characters.")
    }


    // State Validation
    @Test
    fun `isStateValid should return true and an empty string for a valid state`() {
        val (isValid, message) = InputValidation.isStateValid("California")
        assertThat(isValid).isTrue()
        assertThat(message).isEmpty()
    }

    @Test
    fun `isStateValid should return false and the correct error message for an empty state`() {
        val (isValid, message) = InputValidation.isStateValid("")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("State cannot be empty.")
    }

    @Test
    fun `isStateValid should return false and the correct error message for a state that contains non-letter characters`() {
        val (isValid, message) = InputValidation.isStateValid("California123")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("State can only contain letters and spaces.")
    }

    @Test
    fun `isStateValid should return false and the correct error message for a state that is too long`() {
        val (isValid, message) = InputValidation.isStateValid("a".repeat(101))
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("State cannot be more than 100 characters.")
    }

    // ZipCode Validation
    @Test
    fun `isZipCodeValid should return true and an empty string for a valid state`() {
        val (isValid, message) = InputValidation.isZipCodeValid("400101")
        assertThat(isValid).isTrue()
        assertThat(message).isEmpty()
    }

    @Test
    fun `isZipCodeValid should return false and the correct error message for an empty state`() {
        val (isValid, message) = InputValidation.isZipCodeValid("")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("Zip code cannot be empty.")
    }

    @Test
    fun `isZipCodeValid should return false for sapId with non-digit characters`() {
        val (isValid, errorMessage) = InputValidation.isZipCodeValid("400a01")
        assertThat(isValid).isFalse()
        assertThat(errorMessage).isEqualTo("Zip code can only contain digits.")
    }

    @Test
    fun `isZipCodeValid should return false and the correct error message for a state that is too long`() {
        val (isValid, message) = InputValidation.isZipCodeValid("1234567")
        assertThat(isValid).isFalse()
        assertThat(message).isEqualTo("Zip code must be 6 characters.")
    }

    @Test
    fun `isScoreValid should return false and the correct error message for an empty state`() {
        val result = InputValidation.isScoreValid("")
        assertThat(result.first).isFalse()
        assertThat(result.second).isEqualTo("Score cannot be empty.")
    }

    @Test
    fun `isScoreValid should return false for score with non-digit characters`() {
        val result = InputValidation.isScoreValid("abc")
        assertThat(result.first).isFalse()
        assertThat(result.second).isEqualTo("Score can only contain digits.")
    }

    @Test
    fun `isScoreValid should return false when score is negative`() {
        val result = InputValidation.isScoreValid("-1")
        assertThat(result.first).isFalse()
        assertThat(result.second).isEqualTo("Score cannot be negative.")
    }

    @Test
    fun `isScoreValid should return false when score is more than 10`() {
        val result = InputValidation.isScoreValid("11")
        assertThat(result.first).isFalse()
        assertThat(result.second).isEqualTo("Score cannot be more than 10.")
    }

    @Test
    fun `isScoreValid should return false when score starts with 00`() {
        val result = InputValidation.isScoreValid("00")
        assertThat(result.first).isFalse()
        assertThat(result.second).isEqualTo("Score cannot start with 00.")
    }

    @Test
    fun `isScoreValid should return true and empty message when score is valid`() {
        val result = InputValidation.isScoreValid("5")
        assertThat(result.first).isTrue()
        assertThat(result.second).isEmpty()
    }
}