package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.Mock
import com.krish.jobspot.model.MockDetail
import com.krish.jobspot.model.MockQuestion
import com.krish.jobspot.model.MockResult
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK_RESULT
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val TAG = "QuestionPageViewModel"

class QuestionPageViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val studentId: String by lazy { mFirebaseAuth.currentUser?.uid.toString() }
    private val selectedAnswers = MutableList(10) { _ -> "" }

    val ruleDisplayed = MutableLiveData(false)

    private val _submitMockStatus: MutableLiveData<Resource<String>> = MutableLiveData()
    val submitMockStatus: LiveData<Resource<String>> = _submitMockStatus

    private val _mock: MutableLiveData<Resource<Mock>> = MutableLiveData()
    val mock: LiveData<Resource<Mock>> = _mock

    fun fetchMockTest(mockTestId: String) {
        viewModelScope.launch(IO) {
            try {
                _mock.postValue(Resource.loading())
                val mockRef = mFirestore.collection(COLLECTION_PATH_MOCK).document(mockTestId)
                val mockSnapshot = mockRef.get().await()
                val mock = mockSnapshot.toObject(Mock::class.java)!!
                _mock.postValue(Resource.success(mock))
            } catch (error: Exception) {
                _mock.postValue(Resource.error(error.message!!))
            }
        }
    }

    fun setSelectedAnswer(questionIdx: Int, answer: String) {
        selectedAnswers[questionIdx] = answer
    }

    fun submitMock(mock: Mock, timeRemaining: Long) {
        viewModelScope.launch(IO) {
            try {
                _submitMockStatus.postValue(Resource.loading())
                val mockQuestion = mock.mockQuestion
                val (correctCount, incorrectCount, unAttemptedCount) = getScores(mockQuestion)
                val totalTime = TimeUnit.MINUTES.toMillis(mock.duration.toLong())
                val timeTaken = totalTime - timeRemaining

                val mockResult = MockResult(
                    mockId = mock.uid,
                    studentId = studentId,
                    correctAns = correctCount,
                    incorrectAns = incorrectCount,
                    unAttempted = unAttemptedCount,
                    timeTaken = timeTaken,
                    totalQuestion = mockQuestion.size.toString()
                )

                val mockResultPath = "$COLLECTION_PATH_MOCK_RESULT/${mock.uid}/$studentId"
                val mockResultRef = mRealtimeDb.child(mockResultPath)
                mockResultRef.setValue(mockResult).await()
                _submitMockStatus.postValue(Resource.success("Mock submitted success."))
            } catch (error: Exception) {
                val errorMessage = error.message ?: ""
                _submitMockStatus.postValue(Resource.error(errorMessage))
            }
        }
    }

    private fun getScores(mockQuestion: List<MockQuestion>): Triple<String, String, String> {
        val correctOptions = mockQuestion.map { question ->
            val correctIndex = getCurrentIndex(question.correctOption)
            question.options[correctIndex]
        }
        val answerCounts = correctOptions.mapIndexed { index, correctOption ->
            val userOption = selectedAnswers[index]
            if (userOption.isNotEmpty()) {
                if (userOption == correctOption) "correct" else "incorrect"
            } else {
                "unattempted"
            }
        }.groupBy { it }

        val correctCount = answerCounts["correct"]?.size ?: 0
        val incorrectCount = answerCounts["incorrect"]?.size ?: 0
        val unAttemptedCount = answerCounts["unattempted"]?.size ?: 0
        return Triple(
            correctCount.toString(),
            incorrectCount.toString(),
            unAttemptedCount.toString()
        )
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

    fun updateStudentTestStatus(mockId: String) {
        viewModelScope.launch(IO) {
            try {
                val mockResultPath =
                    "${COLLECTION_PATH_STUDENT}/$studentId/${COLLECTION_PATH_MOCK}/$mockId"
                mRealtimeDb.child(mockResultPath).setValue(mockId).await()
                val mockDetailRef =
                    mRealtimeDb.child(COLLECTION_PATH_MOCK).child(mockId).get().await()
                val mockDetail = mockDetailRef.getValue(MockDetail::class.java)!!
                mockDetail.studentCount = mockDetail.studentIds.size.toString()
                mockDetail.studentIds.add(studentId)
                val mockTestPath = "$COLLECTION_PATH_MOCK/$mockId"
                val mockTestRef = mRealtimeDb.child(mockTestPath)
                mockTestRef.setValue(mockDetail).await()
            } catch (error: Exception) {
                Log.d(TAG, "Error: ${error.message} ")
            }
        }
    }


}