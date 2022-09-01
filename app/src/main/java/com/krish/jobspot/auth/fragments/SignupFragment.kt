package com.krish.jobspot.auth.fragments

import android.os.Bundle

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentSignupBinding
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.clearText


private const val TAG = "SIGN_UP_FRAGMENT"
class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        val loginText = SpannableString(getString(R.string.login_prompt))
        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
        val loginColor = ForegroundColorSpan(color)
        loginText.setSpan(UnderlineSpan(), 25, loginText.length, 0)
        loginText.setSpan(loginColor, 25, loginText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.tvLogin.text = loginText

        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack(R.id.loginFragment, false)
        }

        binding.etUsernameContainer.addTextWatcher()
        binding.etEmailContainer.addTextWatcher()
        binding.etPasswordContainer.addTextWatcher()

        binding.btnSignup.setOnClickListener {
            val username = binding.etUsername.text.toString().trim { it <= ' ' }
            val email = binding.etEmail.text.toString().trim { it <= ' ' }
            val password = binding.etPassword.text.toString().trim { it <= ' ' }
            if (detailVerification(username, email, password)) {
                authenticateUser(username, email, password)
                clearField()
            }
        }
    }

    private fun authenticateUser(
        username: String,
        email: String,
        password: String
    ) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = mAuth.currentUser?.uid
                Log.d(TAG, "UID : $uid")
                Toast.makeText(requireContext(), getString(R.string.auth_pass), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "Exception: ${error.message}")
                Toast.makeText(requireContext(), getString(R.string.auth_fail), Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearField() {
        binding.etUsername.clearText()
        binding.etEmail.clearText()
        binding.etPassword.clearText()
    }

    // Verify user details and show message if error
    private fun detailVerification(
        username: String,
        email: String,
        password: String
    ): Boolean {
        return if (!InputValidation.checkNullity(username)) {
            binding.etUsernameContainer.error = getString(R.string.field_error_username)
            false
        } else if (!InputValidation.emailValidation(email)) {
            binding.etEmailContainer.error = getString(R.string.field_error_email)
            false
        } else if (!InputValidation.passwordValidation(password)) {
            binding.etPasswordContainer.error = getString(R.string.field_error_password)
            false
        } else {
            true
        }
    }

}