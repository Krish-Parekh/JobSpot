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
    private val mockAnswers = arrayOfNulls<String>(10)
    private val uiEventChannel = Channel<UiState>()
    val uiEventFlow = uiEventChannel.receiveAsFlow()

    fun setMockAnswers(questionIdx: Int, answer: String) {
        mockAnswers[questionIdx] = answer
    }

    fun submitQuiz(mock: Mock, timeRemaining: Long) {
        viewModelScope.launch {
            try {
                uiEventChannel.trySend(UiState.LOADING)
                val correctOptions = mock.mockQuestion.map { mockQuestion ->
                    val correctIndex = getCurrentIndex(mockQuestion.correctOption)
                    mockQuestion.options[correctIndex]
                }
                val correct =
                    correctOptions.zip(mockAnswers).count { it.first == it.second }.toString()
                val incorrect =
                    correctOptions.zip(mockAnswers).count { it.first != it.second }.toString()
                val unAttempted =
                    correctOptions.zip(mockAnswers).count { it.second == null }.toString()

                val totalTime = TimeUnit.MINUTES.toMillis(mock.duration.toLong())
                val timeTaken = totalTime - timeRemaining

                val mockResult = MockResult(
                    mockId = mock.uid,
                    studentId = studentId,
                    correctAns = correct,
                    incorrectAns = incorrect,
                    unAttempted = unAttempted,
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