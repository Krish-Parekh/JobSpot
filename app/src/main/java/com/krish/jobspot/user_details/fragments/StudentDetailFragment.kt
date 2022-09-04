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
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.MaterialDatePicker
import com.krish.jobspot.R
import com.krish.jobspot.auth.fragments.LoginFragmentDirections
import com.krish.jobspot.databinding.FragmentStudentDetailBinding
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher

private const val TAG = "StudentDetailFragment"
class StudentDetailFragment : Fragment() {

    private lateinit var binding: FragmentStudentDetailBinding
    private var imageUri: String = ""
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private var gender : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentDetailBinding.inflate(inflater, container, false)
        setupView()
        return binding.root
    }

    private fun setupView() {
        binding.profileImage.setOnClickListener {
            startCrop()
        }

        binding.etDate.isCursorVisible = false
        binding.etDate.keyListener = null
        binding.etDateContainer.setEndIconOnClickListener {
            showCalendar()
        }

        binding.genderSpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, newText ->
            binding.genderSpinner.error = null
            gender = newText
        }

        binding.etSapIdContainer.addTextWatcher()
        binding.etMobileContainer.addTextWatcher()

        binding.btnNext.setOnClickListener {
            val sapId = binding.etSapId.text.toString().trim { it <= ' '}
            val mobile = binding.etMobile.text.toString().trim { it <= ' ' }
            val dob = binding.etDate.text.toString().trim { it <= ' ' }
            if(detailVerification(sapId, mobile, dob, gender, imageUri)){
                Log.d(TAG, "Error : $sapId, $mobile, $dob, $gender, $imageUri")
                navigateToAddress()
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
                imageUri = data?.data!!.toString()
                binding.profileImage.setImageURI(data.data!!)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
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
        imageUri: String
    ) : Boolean {

        return if(!InputValidation.sapIdValidation(sapId)) {
            binding.etSapIdContainer.error = "Please enter valid Sap Id"
            false
        } else if(!InputValidation.mobileValidation(mobile)){
            binding.etMobileContainer.error = "Please enter valid mobile no."
            false
        } else if(!InputValidation.dobValidation(dob)){
            binding.etDateContainer.apply {
                error = "Please enter valid date"
                setErrorIconOnClickListener {
                    error = null
                }
            }
            false
        }else if(!InputValidation.genderValidation(gender)){
            binding.genderSpinner.error = ""
            false
        }else if(imageUri.isEmpty()){
            Toast.makeText(requireContext(), "Please select Image", Toast.LENGTH_SHORT).show()
            false
        }else{
            true
        }
    }

    private fun navigateToAddress() {
        findNavController().navigate(R.id.action_studentDetailFragment_to_studentAddressFragment)
    }
}