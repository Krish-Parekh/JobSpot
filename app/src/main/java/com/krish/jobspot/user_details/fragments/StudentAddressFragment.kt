package com.krish.jobspot.user_details.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentAddressBinding
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.model.Address
import com.krish.jobspot.model.Student
import com.krish.jobspot.util.getInputValue

private const val TAG = "StudentAddressFragment"
class StudentAddressFragment : Fragment() {

    private lateinit var binding: FragmentStudentAddressBinding
    private val args by navArgs<StudentAddressFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentAddressBinding.inflate(inflater, container, false)
        setupView()
        return binding.root
    }

    private fun setupView() {

        binding.ivPopOut.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etAddressOneContainer.addTextWatcher()
        binding.etCityContainer.addTextWatcher()
        binding.etStateContainer.addTextWatcher()
        binding.etZipCodeContainer.addTextWatcher()

        binding.btnNext.setOnClickListener {
            val addressOne = binding.etAddressOne.getInputValue()
            val addressTwo = binding.etAddressTwo.getInputValue()
            val finalAddress = "$addressOne $addressTwo"
            val city = binding.etCity.getInputValue()
            val state = binding.etState.getInputValue()
            val zipCode = binding.etZipCode.getInputValue()
            if(detailVerification(addressOne, city, state, zipCode)){
                Log.d(TAG, "$finalAddress ,$city ,$state, $zipCode")
                val address = Address(
                    address = finalAddress,
                    city = city,
                    state = state,
                    zipCode = zipCode
                )
                args.student.address = address
                val student = args.student
                Log.d(TAG, "Student : ${args.student}")
                navigateToAcademic(student)
            }
        }
    }

    private fun detailVerification(
        address: String,
        city: String,
        state: String,
        zipCode: String
    ) : Boolean {
        binding.apply {
            return if(!InputValidation.addressValidation(address)){
                etAddressOneContainer.error = getString(R.string.field_error_address)
                false
            }else if(!InputValidation.cityValidation(city)){
                etCityContainer.error = getString(R.string.field_error_city)
                false
            }else if(!InputValidation.stateValidation(state)){
                etStateContainer.error = getString(R.string.field_error_state)
                false
            }else if(!InputValidation.zipCodeValidation(zipCode)){
                etZipCodeContainer.error = getString(R.string.field_error_zip_code)
                false
            }else{
                true
            }
        }

    }

    private fun navigateToAcademic(student: Student) {
        val direction = StudentAddressFragmentDirections.actionStudentAddressFragmentToStudentAcademicFragment(student = student)
        findNavController().navigate(direction)
    }

}