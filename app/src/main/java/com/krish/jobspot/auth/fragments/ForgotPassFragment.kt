package com.krish.jobspot.auth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentForgotPassBinding
import com.krish.jobspot.util.InputValidation

private const val TAG = "FORGOT_PASSWORD"
class ForgotPassFragment : Fragment() {

    private lateinit var binding: FragmentForgotPassBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgotPassBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.btnResetPassword.setOnClickListener {
            val password = binding.etEmail.text.toString().trim{it <= ' '}
            findNavController().navigate(R.id.action_forgotPassFragment_to_emailFragment)
        }
    }


}