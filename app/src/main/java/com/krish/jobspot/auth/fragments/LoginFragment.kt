package com.krish.jobspot.auth.fragments


import android.content.Intent
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
import com.krish.jobspot.databinding.FragmentLoginBinding
import com.krish.jobspot.user_details.UserDetailActivity
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.clearText

private const val TAG = "LOGIN_FRAGMENT"
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        val signupText = SpannableString(getString(R.string.sign_up_prompt))
        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
        val signupColor = ForegroundColorSpan(color)
        signupText.setSpan(UnderlineSpan(), 31, signupText.length, 0)
        signupText.setSpan(signupColor, 31, signupText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.tvSignup.text = signupText

        binding.tvSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        binding.tvForgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPassFragment)
        }

        binding.etEmailContainer.addTextWatcher()
        binding.etPasswordContainer.addTextWatcher()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim { it <= ' ' }
            val password = binding.etPassword.text.toString().trim { it <= ' ' }
            if (detailVerification(email, password)) {
                authenticateUser(email, password)
                clearField()
            }
        }
    }

    private fun clearField() {
        binding.etEmail.clearText()
        binding.etPassword.clearText()
    }

    // Authenticate user for login
    private fun authenticateUser(
        email: String,
        password: String
    ) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = mAuth.currentUser?.uid
                Log.d(TAG, "UID : $uid")
                Toast.makeText(requireContext(), getString(R.string.auth_pass), Toast.LENGTH_SHORT).show()
                navigateToUserDetail()
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "Exception: ${error.message}")
                Toast.makeText(requireContext(), getString(R.string.auth_fail), Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToUserDetail() {
        val userDetailActivity = Intent(requireContext(), UserDetailActivity::class.java)
        activity?.startActivity(userDetailActivity)
        activity?.finish()
    }

    // Verify user details and show message if error
    private fun detailVerification(
        email: String,
        password: String
    ): Boolean {
        return if (!InputValidation.emailValidation(email)) {
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