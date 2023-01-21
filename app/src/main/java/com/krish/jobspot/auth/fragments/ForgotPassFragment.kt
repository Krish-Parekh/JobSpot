package com.krish.jobspot.auth.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.auth.viewmodel.AuthViewModel
import com.krish.jobspot.databinding.FragmentForgotPassBinding
import com.krish.jobspot.util.*
import com.krish.jobspot.util.Status.*

private const val TAG = "FORGOT_PASSWORD"

class ForgotPassFragment : Fragment() {
    private var _binding: FragmentForgotPassBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by viewModels<AuthViewModel>()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPassBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupObserver() {
         authViewModel.resendPasswordStatus.observe(viewLifecycleOwner){ resendPasswordState ->
            when(resendPasswordState.status){
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    val successMessage = resendPasswordState.data!!
                    showToast(requireContext(), successMessage)
                }
                ERROR -> {
                    val errorMessage = resendPasswordState.message!!
                    showToast(requireContext(), errorMessage)
                }
            }
        }
    }

    private fun setupUI() {

        binding.apply {
            btnBackToLogin.setOnClickListener {
                findNavController().popBackStack()
            }

            etEmailContainer.addTextWatcher()

            btnResetPassword.setOnClickListener {
                val email = etEmail.getInputValue()
                val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
                if (isEmailValid.not()) {
                    authViewModel.resendPassword(email)
                    clearField()
                } else {
                    etEmailContainer.error = emailError
                }
            }
        }
    }

    private fun clearField() {
        binding.etEmail.clearText()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}