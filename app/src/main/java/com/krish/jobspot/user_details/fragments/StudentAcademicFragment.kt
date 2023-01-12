package com.krish.jobspot.user_details.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentAcademicBinding
import com.krish.jobspot.model.Academic
import com.krish.jobspot.model.Student
import com.krish.jobspot.util.InputValidation
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.getInputValue

private const val TAG = "StudentAcademicFragment"
class StudentAcademicFragment : Fragment() {
    private var _binding: FragmentStudentAcademicBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<StudentAcademicFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentAcademicBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.apply {
            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }
            etSemOneContainer.addTextWatcher()
            etSemTwoContainer.addTextWatcher()
            etSemThreeContainer.addTextWatcher()
            etSemFourContainer.addTextWatcher()
            etAvgScoreContainer.addTextWatcher()

            btnNext.setOnClickListener {
                val sem1 = etSemOne.getInputValue()
                val sem2 = etSemTwo.getInputValue()
                val sem3 = etSemThree.getInputValue()
                val sem4 = etSemFour.getInputValue()
                val avgScore = etAvgScore.getInputValue()
                if(detailVerification(sem1, sem2, sem3, sem4, avgScore)){
                    val academic = Academic(
                        sem1 = sem1,
                        sem2 = sem2,
                        sem3 = sem3,
                        sem4 = sem4,
                        avgScore = avgScore,
                    )
                    args.student.academic = academic
                    val student = args.student
                    Log.d(TAG, "Student : ${args.student}")
                    navigateToResume(student)
                }
            }
        }
    }

    private fun navigateToResume(student : Student){
        val direction  = StudentAcademicFragmentDirections.actionStudentAcademicFragmentToStudentResumeFragment(student = student)
        findNavController().navigate(direction)
    }

    private fun detailVerification(
        sem1: String,
        sem2: String,
        sem3: String,
        sem4: String,
        avgScore: String
    ) : Boolean{
        binding.apply {
            val (isSemOneValid, semOneError) = InputValidation.isScoreValid(sem1)
            if (isSemOneValid.not()){
                etSemOneContainer.error = semOneError
                return isSemOneValid
            }

            val (isSemTwoValid, semTwoError) = InputValidation.isScoreValid(sem2)
            if (isSemTwoValid.not()){
                etSemOneContainer.error = semTwoError
                return isSemTwoValid
            }

            val (isSemThreeValid, semThreeError) = InputValidation.isScoreValid(sem3)
            if (isSemThreeValid.not()){
                etSemOneContainer.error = semThreeError
                return isSemThreeValid
            }

            val (isSemFourValid, semFourError) = InputValidation.isScoreValid(sem4)
            if (isSemFourValid.not()){
                etSemOneContainer.error = semFourError
                return isSemFourValid
            }

            val (isAvgScoreValid, avgScoreError) = InputValidation.isScoreValid(avgScore)
            if (isAvgScoreValid.not()){
                etSemOneContainer.error = avgScoreError
                return isAvgScoreValid
            }
            return true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}