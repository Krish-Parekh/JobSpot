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
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_ROLE
import com.krish.jobspot.util.Constants.Companion.ROLE_TYPE_STUDENT
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val TAG = "SignupFragment"

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

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

                mFirestore.collection(COLLECTION_PATH_ROLE).document(currentUser.uid).set(currentUserRole).await()
                currentUser.updateProfile(profileUpdates).await()
                showToast(requireContext(), getString(R.string.auth_pass))
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

    private fun detailVerification(
        username: String,
        email: String,
        password: String
    ): Boolean {
        binding.apply {
            val (isUsernameValid, usernameError) = InputValidation.isUsernameValid(username)
            if (isUsernameValid.not()){
                etUsernameContainer.error = usernameError
                return isUsernameValid
            }

            val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
            if (isEmailValid.not()){
                etEmailContainer.error = emailError
                return isEmailValid
            }

            val (isPasswordValid, passwordError) = InputValidation.isPasswordValid(password)
            if (isPasswordValid.not()){
                etPasswordContainer.error = passwordError
                return isPasswordValid
            }
            return true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}