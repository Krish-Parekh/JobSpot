package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krish.jobspot.R
import com.krish.jobspot.databinding.MockSolutionCardLayoutBinding
import com.krish.jobspot.model.MockQuestion

class MockSolutionAdapter : RecyclerView.Adapter<MockSolutionAdapter.MockSolutionViewHolder>() {

    private val mockQuestion: MutableList<MockQuestion> = mutableListOf()

    inner class MockSolutionViewHolder(private val binding: MockSolutionCardLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mockQuestion: MockQuestion, position: Int) {
            binding.apply {
                tvQuestionCount.text = "Question ${position + 1}"
                tvQuestion.text = mockQuestion.question
                tvOptionOneAnswer.text = mockQuestion.options[0]
                tvOptionTwoAnswer.text = mockQuestion.options[1]
                tvOptionThreeAnswer.text = mockQuestion.options[2]
                tvOptionFourAnswer.text = mockQuestion.options[3]
                tvSolution.text = mockQuestion.feedback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockSolutionViewHolder {
        val view = MockSolutionCardLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MockSolutionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MockSolutionViewHolder, position: Int) {
        holder.bind(mockQuestion[position], position)
    }

    override fun getItemCount(): Int = mockQuestion.size

    fun setMockQuestions(newMockQuestion: List<MockQuestion>) {
        mockQuestion.clear()
        mockQuestion.addAll(newMockQuestion)
        notifyDataSetChanged()
    }
}