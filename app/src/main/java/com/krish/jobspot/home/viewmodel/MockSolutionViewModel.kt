package com.krish.jobspot.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.Mock
import com.krish.jobspot.model.MockQuestion
import com.krish.jobspot.model.MockResult
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK_RESULT
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MockSolutionState(
    val mockResult: MockResult = MockResult(),
    val mockQuestions: List<MockQuestion> = emptyList()
)

class MockSolutionViewModel : ViewModel() {
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb by lazy { FirebaseDatabase.getInstance().reference }

    private val _mockResultState : MutableLiveData<Resource<MockSolutionState>> = MutableLiveData()
    val mockResultState : LiveData<Resource<MockSolutionState>> = _mockResultState

    fun fetchMockResult(mockId: String) {
        viewModelScope.launch(IO) {
            try {
                _mockResultState.postValue(Resource.loading())
                val mockResultDeffered = async { getMockResult(mockId) }
                val mockTestDeffered = async { getMockTest(mockId) }
                val mockResult = mockResultDeffered.await()
                val mockTest = mockTestDeffered.await()
                val mockSolutionState = MockSolutionState(
                    mockResult = mockResult,
                    mockQuestions = mockTest.mockQuestion
                )
                _mockResultState.postValue(Resource.success(mockSolutionState))
            } catch (error: Exception) {
                val errorMessage = error.message!!
                _mockResultState.postValue(Resource.error(errorMessage))
            }
        }
    }

    private suspend fun getMockResult(mockId: String) : MockResult {
        val studentId = mAuth.currentUser?.uid!!
        val mockResultPath = "$COLLECTION_PATH_MOCK_RESULT/$mockId/$studentId"
        val mockResultRef = mRealtimeDb.child(mockResultPath)
        val mockResultDeffered = CompletableDeferred<MockResult>()
        val mockResultListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val mockResult = snapshot.getValue(MockResult::class.java)!!
                    mockResultDeffered.complete(mockResult)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mockResultDeffered.completeExceptionally(error.toException())
            }
        }
        mockResultRef.addListenerForSingleValueEvent(mockResultListener)
        mockResultDeffered.invokeOnCompletion {
            mockResultRef.removeEventListener(mockResultListener)
        }
        return mockResultDeffered.await()
    }

    private suspend fun getMockTest(mockId: String) : Mock {
        val mockTestRef = mFirestore.collection(COLLECTION_PATH_MOCK).document(mockId)
        val mockTestDoc = mockTestRef.get().await()
        val mockTest = mockTestDoc.toObject(Mock::class.java)!!
        return mockTest
    }
}