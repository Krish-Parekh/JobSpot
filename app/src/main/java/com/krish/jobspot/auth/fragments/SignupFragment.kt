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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentSignupBinding
import com.krish.jobspot.user_details.UserDetailActivity
import com.krish.jobspot.util.*
import com.krish.jobspot.util.Constants.Companion.ROLE_TYPE_STUDENT
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val TAG = "SignupFragment"

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
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
        binding.apply {
            tvLogin.text = createLoginText()
            tvLogin.setOnClickListener {
                findNavController().popBackStack(R.id.loginFragment, false)
            }

            etUsernameContainer.addTextWatcher()
            etEmailContainer.addTextWatcher()
            etPasswordContainer.addTextWatcher()

            btnSignup.setOnClickListener {
                val username = binding.etUsername.getInputValue()
                val email = binding.etEmail.getInputValue()
                val password = binding.etPassword.getInputValue()
                if (detailVerification(username, email, password)) {
                    authenticateUser(username, email, password)
                    clearField()
                }
            }
        }
    }

    private fun createLoginText(): SpannableString {
        val loginText = SpannableString(getString(R.string.login_prompt))
        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
        val loginColor = ForegroundColorSpan(color)
        loginText.setSpan(UnderlineSpan(), 25, loginText.length, 0)
        loginText.setSpan(loginColor, 25, loginText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return loginText
    }

    private fun authenticateUser(
        username: String,
        email: String,
        password: String
    ) {
        lifecycleScope.launch {
            try {
                loadingDialog.show()
                mAuth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = mAuth.currentUser!!
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(username).build()
                val currentUserRole = hashMapOf("role" to ROLE_TYPE_STUDENT)
                mFirestore.collection("role").document(currentUser.uid).set(currentUserRole).await()
                currentUser.updateProfile(profileUpdates).await()
                showToast(requireContext(), getString(R.string.auth_pass))
                Log.d(TAG, "Navigate user to UserDetail Activity")
                navigateToUserDetail(username, email)
            }catch (error : FirebaseAuthUserCollisionException){
                showToast(requireContext(), "Email already exists")
            }catch (error : Exception) {
                showToast(requireContext(), getString(R.string.auth_fail))
                Log.d(TAG, "Exception : ${error.message}")
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun navigateToUserDetail(username: String, email: String) {
        val userDetailActivity = Intent(requireContext(), UserDetailActivity::class.java)
        userDetailActivity.putExtra("USERNAME", username)
        userDetailActivity.putExtra("EMAIL", email)
        requireActivity().startActivity(userDetailActivity)
        requireActivity().finish()
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
        binding.apply {
            if (!InputValidation.checkNullity(username)) {
                binding.etUsernameContainer.error = getString(R.string.field_error_username)
                return false
            } else if (!InputValidation.emailValidation(email)) {
                binding.etEmailContainer.error = getString(R.string.field_error_email)
                return false
            } else if (!InputValidation.passwordValidation(password)) {
                binding.etPasswordContainer.error = getString(R.string.field_error_password)
                return false
            } else {
                return true
            }
        }
    }


}