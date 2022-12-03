package com.krish.jobspot.user_details.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
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
    private lateinit var binding: FragmentStudentAcademicBinding
    private val args by navArgs<StudentAcademicFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentAcademicBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {

        binding.ivPopOut.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etSemOneContainer.addTextWatcher()
        binding.etSemTwoContainer.addTextWatcher()
        binding.etSemThreeContainer.addTextWatcher()
        binding.etSemFourContainer.addTextWatcher()
        binding.etAvgScoreContainer.addTextWatcher()


        binding.btnNext.setOnClickListener {
            val sem1 = binding.etSemOne.getInputValue()
            val sem2 = binding.etSemTwo.getInputValue()
            val sem3 = binding.etSemThree.getInputValue()
            val sem4 = binding.etSemFour.getInputValue()
            val avgScore = binding.etAvgScore.getInputValue()
            if(detailVerification(sem1, sem2, sem3, sem4, avgScore)){
                Log.d(TAG, "Details : $sem1 , $sem2, $sem3, $sem4, $avgScore")
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
            return if(!InputValidation.scoreValidation(sem1)){
                etSemOneContainer.error = getString(R.string.field_error_score)
                false
            }else if(!InputValidation.scoreValidation(sem2)){
                etSemTwoContainer.error = getString(R.string.field_error_score)
                false
            }else if(!InputValidation.scoreValidation(sem3)){
                etSemThreeContainer.error = getString(R.string.field_error_score)
                false
            }else if(!InputValidation.scoreValidation(sem4)){
                etSemFourContainer.error = getString(R.string.field_error_score)
                false
            }else if(!InputValidation.scoreValidation(avgScore)){
                etAvgScoreContainer.error = getString(R.string.field_error_score)
                false
            }else{
                true
            }
        }

    }
}