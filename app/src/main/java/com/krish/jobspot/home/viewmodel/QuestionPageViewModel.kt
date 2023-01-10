package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.krish.jobspot.model.Mock
import com.krish.jobspot.model.MockResult
import com.krish.jobspot.util.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val TAG = "QuestionPageViewModel"

class QuestionPageViewModel : ViewModel() {
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val studentId: String by lazy { mFirebaseAuth.currentUser?.uid.toString() }
    private val selectedAnswers = MutableList(10){ _ -> ""}

    private val uiEventChannel = Channel<UiState>()
    val uiEventFlow = uiEventChannel.receiveAsFlow()

    fun setSelectedAnswer(questionIdx: Int, answer: String) {
        selectedAnswers[questionIdx] = answer
    }

    fun submitQuiz(mock: Mock, timeRemaining: Long) {
        viewModelScope.launch {
            try {
                uiEventChannel.trySend(UiState.LOADING)
                val correctAnswers = mock.mockQuestion.map { mockQuestion ->
                    val correctIndex = getCurrentIndex(mockQuestion.correctOption)
                    mockQuestion.options[correctIndex]
                }

                val answerCounts = correctAnswers.mapIndexed { index, correctAnswer ->
                    val userAnswer = selectedAnswers[index]
                    if (userAnswer.isNotEmpty()){
                        if (userAnswer == correctAnswer) "correct" else "incorrect"
                    } else {
                        "unattempted"
                    }
                }.groupBy { it }

                val correctAnswerCount = answerCounts["correct"]?.size ?: 0
                val incorrectAnswerCount = answerCounts["incorrect"]?.size ?: 0
                val unAttemptedCount = answerCounts["unattempted"]?.size ?: 0

                val totalTime = TimeUnit.MINUTES.toMillis(mock.duration.toLong())
                val timeTaken = totalTime - timeRemaining

                val mockResult = MockResult(
                    mockId = mock.uid,
                    studentId = studentId,
                    correctAns = correctAnswerCount.toString(),
                    incorrectAns = incorrectAnswerCount.toString(),
                    unAttempted = unAttemptedCount.toString(),
                    timeTaken = timeTaken,
                    totalQuestion = mock.mockQuestion.size.toString()
                )
                mRealtimeDb.child("mock_result").child(mock.uid).child(studentId)
                    .setValue(mockResult).await()

                uiEventChannel.trySend(UiState.SUCCESS)
            } catch (error: Exception) {
                Log.d(TAG, "Error : ${error.message}")
                uiEventChannel.trySend(UiState.FAILURE)
            }
        }
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