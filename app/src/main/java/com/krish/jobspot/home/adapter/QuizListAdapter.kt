package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.krish.jobspot.R
import com.krish.jobspot.home.fragments.MockTestFragment
import com.krish.jobspot.model.QuizState

class QuizListAdapter(private val listener : MockTestFragment) : RecyclerView.Adapter<QuizListAdapter.QuizViewHolder>() {

    private val quizDetail: MutableList<QuizState> = mutableListOf()

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuizName: TextView = itemView.findViewById(R.id.tvQuizName)
        private val tvQuizAttempted: TextView = itemView.findViewById(R.id.tvQuizAttempted)
        private val cvQuiz : MaterialCardView = itemView.findViewById(R.id.cvQuiz)

        fun bind(quizState: QuizState) {
            tvQuizName.text = quizState.quizName
            if (quizState.hasAttempted) {
                tvQuizAttempted.text = "Already Attempted"
            } else {
                tvQuizAttempted.text = "Not Attempted"
            }
            cvQuiz.setOnClickListener {
                listener.navigateToMockTestActivity(quizState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.quiz_card_layout, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(quizDetail[position])
    }

    override fun getItemCount(): Int = quizDetail.size

    fun setQuizData(newQuizDetail: List<QuizState>) {
        quizDetail.clear()
        quizDetail.addAll(newQuizDetail)
        notifyDataSetChanged()
    }

}