package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.krish.jobspot.R
import com.krish.jobspot.model.MockQuestion

class MockSolutionAdapter : RecyclerView.Adapter<MockSolutionAdapter.MockSolutionViewHolder>() {

    private val mockQuestion : MutableList<MockQuestion> = mutableListOf()

    inner class MockSolutionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val tvQuestionCount : TextView = itemView.findViewById(R.id.tvQuestionCount)
        private val tvQuestion : TextView = itemView.findViewById(R.id.tvQuestion)
        private val tvOptionOne : TextView = itemView.findViewById(R.id.tvOptionOneAnswer)
        private val tvOptionTwo : TextView = itemView.findViewById(R.id.tvOptionTwoAnswer)
        private val tvOptionThree : TextView = itemView.findViewById(R.id.tvOptionThreeAnswer)
        private val tvOptionFour : TextView = itemView.findViewById(R.id.tvOptionFourAnswer)
        private val tvSolution : TextView = itemView.findViewById(R.id.tvSolution)

        fun bind(mockQuestion: MockQuestion, position: Int){
            tvQuestionCount.text = "Question ${position + 1}"
            tvQuestion.text = mockQuestion.question
            tvOptionOne.text = mockQuestion.options[0]
            tvOptionTwo.text = mockQuestion.options[1]
            tvOptionThree.text = mockQuestion.options[2]
            tvOptionFour.text = mockQuestion.options[3]
            tvSolution.text = mockQuestion.feedback
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockSolutionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mock_solution_card_layout, parent, false)
        return MockSolutionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MockSolutionViewHolder, position: Int) {
        holder.bind(mockQuestion[position], position)
    }

    override fun getItemCount(): Int = mockQuestion.size

    fun setMockQuestions(newMockQuestion : List<MockQuestion>){
        mockQuestion.clear()
        mockQuestion.addAll(newMockQuestion)
        notifyDataSetChanged()
    }
}