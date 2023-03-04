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

/**
 * Represents the state of the mock solution.
 *
 * @property mockResult The mock result object.
 * @property mockQuestions The list of mock questions.
 */
data class MockSolutionState(
    val mockResult: MockResult = MockResult(),
    val mockQuestions: List<MockQuestion> = emptyList()
)

/**
 * ViewModel class for the mock solution screen.
 */
class MockSolutionViewModel : ViewModel() {
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb by lazy { FirebaseDatabase.getInstance().reference }

    private val _mockResultState : MutableLiveData<Resource<MockSolutionState>> = MutableLiveData()
    val mockResultState : LiveData<Resource<MockSolutionState>> = _mockResultState

    /**
     * Fetches the mock result and mock test for a given mock id.
     *
     * @param mockId The id of the mock.
     */
    fun fetchMockResult(mockId: String) {
        viewModelScope.launch(IO) {
            try {
                _mockResultState.postValue(Resource.loading())
                // Get mock result and mock test in parallel using coroutines
                val mockResultDeffered = async { getMockResult(mockId) }
                val mockTestDeffered = async { getMockTest(mockId) }

                // Wait for both deferred values to be available and create a MockSolutionState object
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

    /**
     * Retrieves the mock result from Firebase Realtime Database for the current student and mock ID
     *
     * @param mockId The ID of the mock test for which to retrieve the result
     * @return The [MockResult] object representing the result of the mock test
     * @throws Exception if an error occurs while retrieving the mock result from Firebase Realtime Database
     */
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

    /**
     * Fetches the [Mock] associated with the given [mockId] from the Firestore database.
     *
     * @param mockId The ID of the [Mock] to fetch.
     * @return The fetched [Mock] object.
     * @throws Exception If an error occurs while fetching the [Mock] object from the Firestore database.
     */
    private suspend fun getMockTest(mockId: String) : Mock {
        val mockTestRef = mFirestore.collection(COLLECTION_PATH_MOCK).document(mockId)
        val mockTestDoc = mockTestRef.get().await()
        val mockTest = mockTestDoc.toObject(Mock::class.java)!!
        return mockTest
    }
}