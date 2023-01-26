package com.krish.jobspot.user_details.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.krish.jobspot.databinding.FragmentStudentAddressBinding
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.model.Address
import com.krish.jobspot.model.Student
import com.krish.jobspot.util.getInputValue

private const val TAG = "StudentAddressFragment"
class StudentAddressFragment : Fragment() {

    private var _binding: FragmentStudentAddressBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<StudentAddressFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentAddressBinding.inflate(inflater, container, false)

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }

            etAddressOneContainer.addTextWatcher()
            etCityContainer.addTextWatcher()
            etStateContainer.addTextWatcher()
            etZipCodeContainer.addTextWatcher()

            btnNext.setOnClickListener {
                val addressOne = etAddressOne.getInputValue()
                val addressTwo = etAddressTwo.getInputValue()
                val finalAddress = "$addressOne $addressTwo"
                val city = etCity.getInputValue()
                val state = etState.getInputValue()
                val zipCode = etZipCode.getInputValue()
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
    }

    private fun detailVerification(
        address: String,
        city: String,
        state: String,
        zipCode: String
    ) : Boolean {
        binding.apply {
            val (isAddressValid, addressError) = InputValidation.isAddressValid(address)
            if (isAddressValid.not()){
                etAddressOneContainer.error = addressError
                return isAddressValid
            }

            val (isCityValid, cityError) = InputValidation.isCityValid(city)
            if (isCityValid.not()){
                etCityContainer.error = cityError
                return isCityValid
            }

            val (isStateValid, stateError) = InputValidation.isStateValid(state)
            if (isStateValid.not()){
                etStateContainer.error = stateError
                return isStateValid
            }

            val (isZipCodeValid, zipError) = InputValidation.isZipCodeValid(zipCode)
            if (isZipCodeValid.not()){
                etZipCodeContainer.error = zipError
                return isZipCodeValid
            }

            return true
        }
    }

    private fun navigateToAcademic(student: Student) {
        val direction = StudentAddressFragmentDirections.actionStudentAddressFragmentToStudentAcademicFragment(student = student)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}