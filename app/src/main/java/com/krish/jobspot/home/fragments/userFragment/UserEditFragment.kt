package com.krish.jobspot.home.fragments.userFragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.krish.jobspot.R
import com.krish.jobspot.auth.AuthActivity
import com.krish.jobspot.databinding.BottomSheetDeleteStudentBinding
import com.krish.jobspot.databinding.FragmentUserEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.util.*
import com.krish.jobspot.util.Status.*

class UserEditFragment : Fragment() {
    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<UserEditFragmentArgs>()
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private val userEditViewModel by viewModels<UserEditViewModel>()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserEditBinding.inflate(inflater, container, false)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            val studentDetails = args.student.details!!
            profileImage.load(studentDetails.imageUrl)
            etUsername.setText(studentDetails.username)
            etEmail.setText(studentDetails.email)
            etSapId.setText(studentDetails.sapId)
            etMobile.setText(studentDetails.mobile)

            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }

            if (userEditViewModel.getImageUri() != null) {
                val imageUri = userEditViewModel.getImageUri()
                profileImage.setImageURI(imageUri)
            }

            profileImage.setOnClickListener {
                startCrop()
            }

            ivDeleteStudent.setOnClickListener {
                deleteBottomSheet()
            }

            etUsernameContainer.addTextWatcher()
            etEmailContainer.addTextWatcher()
            etSapIdContainer.addTextWatcher()
            etMobileContainer.addTextWatcher()

            btnSaveChange.setOnClickListener {
                btnSaveChange.isEnabled = false
                val username = etUsername.getInputValue()
                val email = etEmail.getInputValue()
                val sapId = etSapId.getInputValue()
                val mobile = etMobile.getInputValue()
                val imageUrl = userEditViewModel.getImageUri() ?: Uri.parse(args.student.details?.imageUrl)

                if (detailVerification(imageUrl, username, email, sapId, mobile)) {
                    studentDetails.username = username
                    studentDetails.email = email
                    studentDetails.sapId = sapId
                    studentDetails.mobile = mobile
                    studentDetails.imageUrl = imageUrl.toString()
                    args.student.details = studentDetails
                    userEditViewModel.updateStudent(student = args.student)
                }

                btnSaveChange.isEnabled = true
            }
        }
    }

    private fun setupObserver() {
        userEditViewModel.updateState.observe(viewLifecycleOwner) { updateState ->
            when (updateState.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    val status = updateState.data!!
                    showToast(requireContext(), status)
                    loadingDialog.dismiss()
                }
                ERROR -> {
                    val errorMessage = updateState.message!!
                    showToast(requireContext(), errorMessage)
                    loadingDialog.dismiss()
                }
            }
        }

        userEditViewModel.deleteState.observe(viewLifecycleOwner) { deleteState ->
            when (deleteState.status) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    loadingDialog.dismiss()
                    val deleteStatus = deleteState.data!!
                    showToast(requireContext(), deleteStatus)
                    navigateToLogin()
                }
                ERROR -> {
                    val errorMessage = deleteState.message!!
                    showToast(requireContext(), errorMessage)
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun navigateToLogin() {
        requireActivity().finishAffinity()
        val authActivity = Intent(requireContext(), AuthActivity::class.java)
        authActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(authActivity)
    }

    private fun deleteBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val studentDeleteSheetBinding = BottomSheetDeleteStudentBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(studentDeleteSheetBinding.root)
        studentDeleteSheetBinding.apply {
            btnNo.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            btnDeleteAccount.setOnClickListener {
                bottomSheetDialog.dismiss()
                userEditViewModel.deleteAccount(args.student)
            }
        }
        bottomSheetDialog.show()
    }

    private fun startCrop() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()
            .compress(1024)
            .maxResultSize(300, 300)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun handleCapturedImage(result: ActivityResult) {
        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            Activity.RESULT_OK -> {
                userEditViewModel.setImageUri(imageUri = data?.data!!)
                binding.profileImage.setImageURI(userEditViewModel.getImageUri())
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                showToast(requireContext(), "Task Cancelled")
            }
        }
    }

    private fun detailVerification(
        imageUrl: Uri?,
        username: String,
        email: String,
        sapId: String,
        mobile: String
    ): Boolean {
        binding.apply {
            if (imageUrl == null) {
                showToast(requireContext(), getString(R.string.field_error_image))
                return false
            }

            val (isUsernameValid, usernameError) = InputValidation.isUsernameValid(username)
            if (isUsernameValid.not()) {
                etUsernameContainer.error = usernameError
                return isUsernameValid
            }

            val (isEmailValid, emailError) = InputValidation.isEmailValid(email)
            if (isEmailValid.not()) {
                etEmailContainer.error = emailError
                return isEmailValid
            }

            val (isSapIdValid, sapIdError) = InputValidation.isSapIdValid(sapId)
            if (isSapIdValid.not()) {
                etSapIdContainer.error = sapIdError
                return isSapIdValid
            }

            val (isMobileNumberValid, mobileNumberError) = InputValidation.isMobileNumberValid(mobile)
            if (isMobileNumberValid.not()) {
                etMobileContainer.error = mobileNumberError
                return isMobileNumberValid
            }

            return true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}