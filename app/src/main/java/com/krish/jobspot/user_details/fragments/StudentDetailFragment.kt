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
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "StudentDetailFragment"

class StudentDetailFragment : Fragment() {

    private var _binding: FragmentStudentDetailBinding? = null
    private val binding get() = _binding!!
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val userDetailViewModel: UserDetailViewModel by viewModels()
    private var username: String = ""
    private var email: String = ""
    private var gender: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentDetailBinding.inflate(inflater, container, false)

        val userData: Bundle? = requireActivity().intent.extras
        if (
            userData != null &&
            userData.containsKey("USERNAME") &&
            userData.containsKey("EMAIL")
        ) {
            username = requireActivity().intent.extras?.getString("USERNAME").toString()
            email = requireActivity().intent.extras?.getString("EMAIL").toString()
        }

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            if (userDetailViewModel.getImageUri() != null) {
                val imageUri = userDetailViewModel.getImageUri()
                profileImage.setImageURI(imageUri)
            }

            profileImage.setOnClickListener {
                startCrop()
            }

            etDate.isCursorVisible = false
            etDate.keyListener = null
            etDate.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus){
                    showCalendar()
                }
            }

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
                    val student = Student(uid = uid, details = detail)
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
            val date = Date(it)
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val dateString = formatter.format(date)
            binding.etDate.setText(dateString)
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

            val (isSapIdValid, sapIdError) = InputValidation.isSapIdValid(sapId)
            if (isSapIdValid.not()){
                etSapIdContainer.error = sapIdError
                return isSapIdValid
            }

            val (isMobileNumberValid, mobileNumberError) = InputValidation.isMobileNumberValid(mobile)
            if (isMobileNumberValid.not()){
                etMobileContainer.error = mobileNumberError
                return isMobileNumberValid
            }

            val (isDOBValid, dobError) = InputValidation.isDOBValid(dob)
            if (isDOBValid.not()){
                etDateContainer.apply {
                    error = dobError
                    setErrorIconOnClickListener {
                        error = null
                    }
                }
                return isDOBValid
            }
            if (!InputValidation.genderValidation(gender)) {
                genderSpinner.error = ""
                return false
            }
            return true
        }
    }

    private fun navigateToAddress(student: Student) {
        val direction = StudentDetailFragmentDirections.actionStudentDetailFragmentToStudentAddressFragment(student = student)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}