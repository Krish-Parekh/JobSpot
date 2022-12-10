package com.krish.jobspot.user_details.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentDetailBinding
import com.krish.jobspot.model.Details
import com.krish.jobspot.model.Student
import com.krish.jobspot.user_details.viewmodel.UserDetailViewModel
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.getInputValue
import com.krish.jobspot.util.showToast

private const val TAG = "StudentDetailFragment"

class StudentDetailFragment : Fragment() {

    private lateinit var binding: FragmentStudentDetailBinding
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val userDetailViewModel : UserDetailViewModel by viewModels()
    private var username: String = ""
    private var email: String = ""
    private var gender: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentDetailBinding.inflate(inflater, container, false)

        val userData: Bundle? = requireActivity().intent.extras
        if (
            userData != null &&
            userData.containsKey("USERNAME") &&
            userData.containsKey("EMAIL")
        ) {
            username = requireActivity().intent.extras?.getString("USERNAME").toString()
            email = requireActivity().intent.extras?.getString("EMAIL").toString()
        }

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.apply {
            if (userDetailViewModel.getImageUri() != null) {
                profileImage.setImageURI(userDetailViewModel.getImageUri())
            }

            profileImage.setOnClickListener {
                startCrop()
            }

            etDate.isCursorVisible = false
            etDate.keyListener = null
            etDateContainer.setEndIconOnClickListener {
                showCalendar()
            }

            genderSpinner.dismissWhenNotifiedItemSelected = true
            genderSpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, selectedGender ->
                binding.genderSpinner.error = null
                gender = selectedGender
            }

            etSapIdContainer.addTextWatcher()
            etMobileContainer.addTextWatcher()

            btnNext.setOnClickListener {
                val sapId = etSapId.getInputValue()
                val mobile = etMobile.getInputValue()
                val dob = etDate.getInputValue()
                val imageUri = userDetailViewModel.getImageUri()
                val uid = mAuth.currentUser?.uid
                if (detailVerification(sapId, mobile, dob, gender, imageUri)) {
                    val detail = Details(
                        username = username,
                        email = email,
                        sapId = sapId,
                        imageUrl = imageUri.toString(),
                        mobile = mobile,
                        dob = dob,
                        gender = gender
                    )
                    val student = Student( uid = uid,details = detail)
                    Log.d(TAG, "Student : $student")
                    navigateToAddress(student)
                }
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
                userDetailViewModel.setImageUri(imageUri = data?.data!!)
                binding.profileImage.setImageURI(userDetailViewModel.getImageUri())
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

    private fun showCalendar() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            binding.etDate.setText(datePicker.headerText)
        }
        datePicker.show(childFragmentManager, "Material DatePicker")
    }

    private fun detailVerification(
        sapId: String,
        mobile: String,
        dob: String,
        gender: String,
        imageUri: Uri?,
    ): Boolean {
        binding.apply {
            if (imageUri == null) {
                showToast(requireContext(), getString(R.string.field_error_image))
                return false
            }
            else if (!InputValidation.sapIdValidation(sapId)) {
                etSapIdContainer.error = getString(R.string.field_error_sap_id)
                return false
            }
            else if (!InputValidation.mobileValidation(mobile)) {
                etMobileContainer.error = getString(R.string.field_error_mobile)
                return false
            }
            else if (!InputValidation.dobValidation(dob)) {
                etDateContainer.apply {
                    error = getString(R.string.field_error_dob)
                    setErrorIconOnClickListener {
                        error = null
                    }
                }
                return false
            }
            else if (!InputValidation.genderValidation(gender)) {
                genderSpinner.error = ""
                return false
            }
            else {
                return true
            }
        }

    }

    private fun navigateToAddress(student: Student) {
        val direction = StudentDetailFragmentDirections.actionStudentDetailFragmentToStudentAddressFragment(student = student)
        findNavController().navigate(direction)
    }
}