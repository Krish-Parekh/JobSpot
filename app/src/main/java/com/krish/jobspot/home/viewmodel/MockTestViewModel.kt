package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krish.jobspot.model.Mock
import com.krish.jobspot.model.MockDetail
import com.krish.jobspot.model.MockTestState
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MockTestViewModelTAG"

class MockTestViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId by lazy { mAuth.currentUser?.uid.toString() }

    private val _mockTestStatus: MutableLiveData<Resource<List<MockTestState>>> = MutableLiveData()
    val mockTestStatus: LiveData<Resource<List<MockTestState>>> = _mockTestStatus

    private val _mock: MutableLiveData<Resource<Mock>> = MutableLiveData()
    val mock: LiveData<Resource<Mock>> = _mock

    private val mockPath = "$COLLECTION_PATH_STUDENT/$studentId/$COLLECTION_PATH_MOCK"
    private val mockRef = mRealtimeDb.child(mockPath)
    var attemptedQuizIdsListener: ValueEventListener? = null
    var mockStateListener: ListenerRegistration? = null

    var mockAnswer: Array<String?> = arrayOfNulls(10)

    fun fetchMockTestStatus() {
        viewModelScope.launch(IO) {
            try {
                _mockTestStatus.postValue(Resource.loading())
                val attemptedQuizIds = getAttemptedMockIds()
                val mockStates = getMockStates(attemptedQuizIds)
                Log.d(TAG, "MockStates : ${mockStates}")
                _mockTestStatus.postValue(Resource.success(mockStates))
            } catch (error: Exception) {
                _mockTestStatus.postValue(Resource.error(error.message!!))
            }
        }
    }

    private suspend fun getAttemptedMockIds(): List<String> {
        val attemptedQuizIdsDeffered = CompletableDeferred<List<String>>()
        attemptedQuizIdsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attemptedQuizIds = snapshot.children.map { it.key!! }
                attemptedQuizIdsDeffered.complete(attemptedQuizIds)
            }

            override fun onCancelled(error: DatabaseError) {
                attemptedQuizIdsDeffered.completeExceptionally(error.toException())
            }
        }
        mockRef.addValueEventListener(attemptedQuizIdsListener!!)
        return attemptedQuizIdsDeffered.await()
    }

    private suspend fun getMockStates(
        attemptedQuizIds: List<String>
    ): List<MockTestState> {
        val mockRef = mFirestore.collection(COLLECTION_PATH_MOCK)
        val mockStateDeffered = CompletableDeferred<List<MockTestState>>()
        mockStateListener = mockRef.addSnapshotListener { value, error ->
            if (error != null) {
                mockStateDeffered.completeExceptionally(error)
                return@addSnapshotListener
            }
            val mockStates = value!!.documents
                .map { it.toObject(Mock::class.java)!! }
                .map { createMockState(it, attemptedQuizIds) }
            mockStateDeffered.complete(mockStates)
        }
        return mockStateDeffered.await()
    }

    private fun createMockState(mock: Mock, attemptedQuizIds: List<String>): MockTestState {
        val hasAttempted = attemptedQuizIds.contains(mock.uid)
        return MockTestState(quizUid = mock.uid, hasAttempted = hasAttempted, quizName = mock.title)
    }

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

    override fun onCleared() {
        attemptedQuizIdsListener?.let { eventListener ->
            mockRef.removeEventListener(eventListener)
        }
        mockStateListener?.remove()
        attemptedQuizIdsListener = null
        mockStateListener = null
        super.onCleared()
    }
}