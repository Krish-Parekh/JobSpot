package com.krish.jobspot.auth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentForgotPassBinding
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.clearText

private const val TAG = "FORGOT_PASSWORD"
class ForgotPassFragment : Fragment() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
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

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etEmailContainer.addTextWatcher()

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim{it <= ' '}
            if(InputValidation.emailValidation(email)){
                mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), getString(R.string.reset_pass), Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_forgotPassFragment_to_emailFragment)
                    }
                    .addOnFailureListener { error ->
                        Log.d(TAG, "Exception: ${error.message}")
                        Toast.makeText(requireContext(), getString(R.string.reset_fail), Toast.LENGTH_SHORT).show()
                    }
                clearField()
            }else{
                binding.etEmailContainer.error = getString(R.string.field_error_email)
            }
        }
    }
    private fun clearField() {
        binding.etEmail.clearText()
    }
}