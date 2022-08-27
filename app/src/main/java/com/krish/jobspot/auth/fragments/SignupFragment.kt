package com.krish.jobspot.auth.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentSignupBinding
import com.krish.jobspot.util.InputValidation


private const val TAG = "SIGN_UP_FRAGMENT"
class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
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

        binding.btnSignup.setOnClickListener {
            val username = binding.etUsername.text.toString().trim { it <= ' '}
            val email = binding.etEmail.text.toString().trim{ it <= ' '}
            val password = binding.etPassword.text.toString().trim { it <= ' '}
        }
    }
}