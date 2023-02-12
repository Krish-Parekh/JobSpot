package com.krish.jobspot.home.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentQuestionViewBinding

import com.krish.jobspot.home.viewmodel.QuestionPageViewModel
import com.krish.jobspot.model.MockQuestion


private const val TAG = "QuestionViewFragment"

class QuestionViewFragment : Fragment() {
    private var _binding: FragmentQuestionViewBinding? = null
    private val binding get() = _binding!!
    private var questionId: Int = 0
    private var selectedOption: Int = -1
    private val questionPageViewModel by activityViewModels<QuestionPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionViewBinding.inflate(inflater, container, false)
        val mockQuestion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("QUESTION", MockQuestion::class.java)!!
        } else {
            arguments?.getParcelable<MockQuestion>("QUESTION") as MockQuestion
        }
        questionId = arguments?.getInt("QUESTION_ID")!!
        if (savedInstanceState != null) {
            val selectCardBackgroundColor = ContextCompat.getColor(requireContext() ,R.color.orange)
            selectedOption = savedInstanceState.getInt("SELECTED_OPTION")
            binding.apply {
                when (selectedOption) {
                    1 -> applyCardBackgroundColor(cvOptionOne, selectCardBackgroundColor)
                    2 -> applyCardBackgroundColor(cvOptionTwo, selectCardBackgroundColor)
                    3 -> applyCardBackgroundColor(cvOptionThree, selectCardBackgroundColor)
                    4 -> applyCardBackgroundColor(cvOptionFour, selectCardBackgroundColor)
                }
            }

        }

        setupViews(mockQuestion)

        return binding.root
    }

    private fun setupViews(mockQuestion : MockQuestion) {
        binding.apply {
            tvQuestionHeader.text = "Question ${questionId + 1}"
            tvQuestion.text = mockQuestion.question

            val options = mockQuestion.options
            tvOptionOneAnswer.text = options[0]
            tvOptionTwoAnswer.text = options[1]
            tvOptionThreeAnswer.text = options[2]
            tvOptionFourAnswer.text = options[3]

            cvOptionOne.setOnClickListener { cardView ->
                selectedOption = 1
                handleAnswerClick(questionId, options[0], cardView)
            }
            cvOptionTwo.setOnClickListener { cardView ->
                selectedOption = 2
                handleAnswerClick(questionId, options[1], cardView)
            }
            cvOptionThree.setOnClickListener { cardView ->
                selectedOption = 3
                handleAnswerClick(questionId, options[2], cardView)
            }
            cvOptionFour.setOnClickListener { cardView ->
                selectedOption = 4
                handleAnswerClick(questionId, options[3], cardView)
            }
        }
    }

    private fun handleAnswerClick(questionId: Int, answer: String, cardView: View) {
        questionPageViewModel.setSelectedAnswer(questionId, answer)
        val cardBackgroundColor = ContextCompat.getColor(requireContext() ,R.color.card_background)
        val selectCardBackgroundColor = ContextCompat.getColor(requireContext(), R.color.orange)

        binding.apply {
            applyCardBackgroundColor(cvOptionOne, cardBackgroundColor)
            applyCardBackgroundColor(cvOptionTwo, cardBackgroundColor)
            applyCardBackgroundColor(cvOptionThree, cardBackgroundColor)
            applyCardBackgroundColor(cvOptionFour, cardBackgroundColor)
        }
        val materialCardView = cardView as MaterialCardView
        applyCardBackgroundColor(materialCardView, selectCardBackgroundColor)
    }

    private fun applyCardBackgroundColor(cardView: MaterialCardView, color: Int) {
        cardView.setCardBackgroundColor(color)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_OPTION", selectedOption)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}