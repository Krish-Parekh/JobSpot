package com.krish.jobspot.home.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.krish.jobspot.databinding.FragmentQuestionViewBinding
import com.krish.jobspot.model.MockQuestion


private const val TAG = "QuestionViewFragment"

class QuestionViewFragment : Fragment() {
    private lateinit var binding: FragmentQuestionViewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionViewBinding.inflate(inflater, container, false)
        val mockQuestion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("QUESTION", MockQuestion::class.java)
        } else {
            arguments?.getParcelable<MockQuestion>("QUESTION") as MockQuestion
        }

        val mockId = arguments?.getString("QUESTION_ID")

        Log.d(TAG, "MockQuestion : ${mockQuestion?.question}")

        mockQuestion?.let { mockQuestion ->
            binding.tvQuestionHeader.text = "Question $mockId"
            binding.tvQuestion.text = mockQuestion.question
            binding.tvOptionOneAnswer.text = mockQuestion.options[0]
            binding.tvOptionTwoAnswer.text = mockQuestion.options[1]
            binding.tvOptionThreeAnswer.text = mockQuestion.options[2]
            binding.tvOptionFourAnswer.text = mockQuestion.options[3]
        }

        return binding.root
    }
}