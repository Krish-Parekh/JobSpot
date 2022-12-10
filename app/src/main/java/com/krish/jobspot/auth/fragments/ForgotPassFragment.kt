package com.krish.jobspot.auth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentForgotPassBinding
import com.krish.jobspot.util.*

private const val TAG = "FORGOT_PASSWORD"
class ForgotPassFragment : Fragment() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var binding: FragmentForgotPassBinding
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
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

        binding.apply {
            btnBackToLogin.setOnClickListener {
                findNavController().popBackStack()
            }

            etEmailContainer.addTextWatcher()

            btnResetPassword.setOnClickListener {
                val email = etEmail.getInputValue()
                if(InputValidation.emailValidation(email)){
                    loadingDialog.show()
                    mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            loadingDialog.dismiss()
                            showToast(requireContext(), getString(R.string.reset_pass))
                            findNavController().navigate(R.id.action_forgotPassFragment_to_emailFragment)
                        }
                        .addOnFailureListener { error ->
                            loadingDialog.dismiss()
                            Log.d(TAG, "Exception: ${error.message}")
                            showToast(requireContext(), getString(R.string.reset_fail))
                        }
                    clearField()
                }else{
                    etEmailContainer.error = getString(R.string.field_error_email)
                }
            }
        }
    }
    private fun clearField() {
        binding.etEmail.clearText()
    }
}