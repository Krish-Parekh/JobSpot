package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.krish.jobspot.model.Mock

private const val TAG = "MockPagesViewModel"

class MockPagesViewModel : ViewModel() {
    var mock: Mock = Mock()

    private val mockAnswers = arrayOfNulls<String>(10)

    fun setMockAnswers(questionIdx: Int, answer: String) {
        mockAnswers[questionIdx] = answer
    }

    fun submitQuiz(mock: Mock) {
        val correctOptions = mock.mockQuestion.map {
            val correctIndex = getCurrentIndex(it.correctOption)
            it.options[correctIndex]
        }
        val correct = correctOptions.zip(mockAnswers).count { it.first == it.second }
        val incorrect = correctOptions.zip(mockAnswers).count { it.first != it.second }
        val notAttempted = correctOptions.zip(mockAnswers).count { it.second == null }
    }

    private fun getCurrentIndex(correctOption: String): Int {
        return when (correctOption) {
            "A" -> 0
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }
    }

}