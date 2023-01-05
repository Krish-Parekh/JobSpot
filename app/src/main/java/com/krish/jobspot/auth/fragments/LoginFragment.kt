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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentLoginBinding
import com.krish.jobspot.home.activity.HomeActivity
import com.krish.jobspot.user_details.UserDetailActivity
import com.krish.jobspot.util.*
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_ROLE
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Constants.Companion.ROLE_TYPE_STUDENT
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "LOGIN_FRAGMENT"
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupViews()

//        if (mAuth.currentUser != null){
//            navigateToHomeActivity()
//        }

        return binding.root
    }

    private fun setupViews() {
        binding.apply {
            tvSignup.text = createSignupText()

            tvSignup.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            }

            tvForgetPassword.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_forgotPassFragment)
            }

            etEmailContainer.addTextWatcher()
            etPasswordContainer.addTextWatcher()

            btnLogin.setOnClickListener {
                val email = etEmail.getInputValue()
                val password = etPassword.getInputValue()
                if (detailVerification(email, password)) {
                    authenticateUser(email, password)
                    clearField()
                }
            }
        } 
    }

    private fun createSignupText() : SpannableString{
        val signupText = SpannableString(getString(R.string.sign_up_prompt))
        val color = ContextCompat.getColor(requireActivity(), R.color.on_boarding_span_text_color)
        val signupColor = ForegroundColorSpan(color)
        signupText.setSpan(UnderlineSpan(), 31, signupText.length, 0)
        signupText.setSpan(signupColor, 31, signupText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return signupText
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
        lifecycleScope.launch {
            try{
                loadingDialog.show()
                mAuth.signInWithEmailAndPassword(email, password).await()
                val currentUserUid = mAuth.currentUser?.uid!!
                val currentUsername = mAuth.currentUser?.displayName!!

                val currentUserDoc = mFirestore.collection(COLLECTION_PATH_STUDENT).document(currentUserUid)
                val userDocument: DocumentSnapshot = currentUserDoc.get().await()

                val currentUserRole = mFirestore.collection(COLLECTION_PATH_ROLE).document(currentUserUid)
                val roleDocument: DocumentSnapshot = currentUserRole.get().await()
                val roleType: String = roleDocument.get("role") as String
                // to check if current user is student because RBA
                if(roleType == ROLE_TYPE_STUDENT){
                    // to check if student have entered all his detail
                    if(userDocument.exists()){
                        navigateToHomeActivity()
                    }else{
                        navigateToUserDetail(username = currentUsername, email = email)
                    }
                    showToast(requireContext(),getString(R.string.auth_pass))
                }else{
                    showToast(requireContext(), "Account doesn't exist")
                }

            } catch (e: FirebaseAuthInvalidCredentialsException) {
                showToast(requireContext(), getString(R.string.invalid_credentials))
            } catch (e: FirebaseAuthInvalidUserException) {
                showToast(requireContext(), getString(R.string.invalid_user))
            } catch (e: FirebaseNetworkException) {
                showToast(requireContext(), getString(R.string.network_error))
            } catch (e: Exception) {
                Log.d(TAG, "Error : ${e.message}")
                showToast(requireContext(), e.message.toString())
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun navigateToHomeActivity() {
        val homeActivity = Intent(requireContext(), HomeActivity::class.java)
        startActivity(homeActivity)
        requireActivity().finish()
    }

    private fun navigateToUserDetail(username: String, email: String) {
        val userDetailActivity = Intent(requireContext(), UserDetailActivity::class.java)
        userDetailActivity.putExtra("USERNAME", username)
        userDetailActivity.putExtra("EMAIL", email)
        requireActivity().startActivity(userDetailActivity)
        requireActivity().finish()
    }

    // Verify user details and show message if error
    private fun detailVerification(
        email: String,
        password: String
    ): Boolean {
        binding.apply {
            if (!InputValidation.emailValidation(email)) {
                etEmailContainer.error = getString(R.string.field_error_email)
                return false
            } else if (!InputValidation.passwordValidation(password)) {
                etPasswordContainer.error = getString(R.string.field_error_password)
                return false
            } else {
                return true
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}