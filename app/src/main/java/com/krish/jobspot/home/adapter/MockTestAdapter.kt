package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.krish.jobspot.R
import com.krish.jobspot.home.fragments.MockTestFragment
import com.krish.jobspot.model.MockTestState

class MockTestAdapter(private val listener : MockTestFragment) : RecyclerView.Adapter<MockTestAdapter.MockTestViewHolder>() {

    private val mockTestState: MutableList<MockTestState> = mutableListOf()

    inner class MockTestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuizName: TextView = itemView.findViewById(R.id.tvMockTestName)
        private val tvQuizAttempted: TextView = itemView.findViewById(R.id.tvMockTestAttemptStatus)
        private val cvQuiz : MaterialCardView = itemView.findViewById(R.id.cvMockTest)

        fun bind(mockTestState: MockTestState) {
            tvQuizName.text = mockTestState.quizName
            if (mockTestState.hasAttempted) {
                tvQuizAttempted.text = "Already Attempted"
            } else {
                tvQuizAttempted.text = "Not Attempted"
            }
            cvQuiz.setOnClickListener {
                if (mockTestState.hasAttempted){
                    listener.navigateToMockResultActivity(mockTestState)
                } else {
                    listener.navigateToMockTestActivity(mockTestState)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockTestViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.mock_test_card_layout, parent, false)
        return MockTestViewHolder(view)
    }

    override fun onBindViewHolder(holder: MockTestViewHolder, position: Int) {
        holder.bind(mockTestState[position])
    }

    override fun getItemCount(): Int = mockTestState.size

    fun setQuizData(newQuizDetail: List<MockTestState>) {
        mockTestState.clear()
        mockTestState.addAll(newQuizDetail)
        notifyDataSetChanged()
    }

}