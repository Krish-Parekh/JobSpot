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
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.auth.AuthActivity
import com.krish.jobspot.databinding.FragmentUserEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.util.*
import com.krish.jobspot.util.UiState.*

class UserEditFragment : Fragment() {
    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    private val args: UserEditFragmentArgs by navArgs()
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private val userEditViewModel: UserEditViewModel by viewModels()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserEditBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.apply {
            profileImage.load(args.student.details?.imageUrl)
            etUsername.setText(args.student.details?.username)
            etEmail.setText(args.student.details?.email)
            etSapId.setText(args.student.details?.sapId)
            etMobile.setText(args.student.details?.mobile)

            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }

            if (userEditViewModel.getImageUri() != null) {
                profileImage.setImageURI(userEditViewModel.getImageUri())
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
                val imageUrl: Uri =
                    userEditViewModel.getImageUri() ?: Uri.parse(args.student.details?.imageUrl)

                if (detailVerification(imageUrl, username, email, sapId, mobile)) {
                    args.student.details?.username = username
                    args.student.details?.email = email
                    args.student.details?.sapId = sapId
                    args.student.details?.mobile = mobile
                    args.student.details?.imageUrl = imageUrl.toString()

                    userEditViewModel.uploadStudentData(student = args.student)
                    handleUploadResponse()
                }

                btnSaveChange.isEnabled = true
            }

        }
    }

    private fun deleteBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_delete_student, null)
        val btnNot: MaterialButton = bottomSheet.findViewById(R.id.btnNo)
        val btnDeleteAccount: MaterialButton = bottomSheet.findViewById(R.id.btnDeleteAccount)
        btnNot.setOnClickListener {
            dialog.dismiss()
        }
        btnDeleteAccount.setOnClickListener {
            dialog.dismiss()
            userEditViewModel.deleteAccount(args.student)
            userEditViewModel.deleteStatus.observe(viewLifecycleOwner){ uiState ->
                when(uiState){
                    LOADING -> {
                        loadingDialog.show()
                    }
                    SUCCESS -> {
                        loadingDialog.dismiss()
                        showToast(requireContext(), "Delete Account Success.")
                        requireActivity().finishAffinity()
                        val loginIntent = Intent(requireContext(), AuthActivity::class.java)
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(loginIntent)
                    }
                    FAILURE -> {
                        loadingDialog.dismiss()
                        showToast(requireContext(), "Error while deleting.")
                    }
                }
            }
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
    }

    private fun handleUploadResponse() {
        userEditViewModel.operationStatus.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                LOADING -> {
                    loadingDialog.show()
                }
                SUCCESS -> {
                    showToast(requireContext(), "Update Success")
                    loadingDialog.dismiss()
                }
                FAILURE -> {
                    loadingDialog.dismiss()
                }

                else -> Unit
            }
        }
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

            val (isMobileNumberValid, mobileNumberError) = InputValidation.isMobileNumberValid(
                mobile
            )
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