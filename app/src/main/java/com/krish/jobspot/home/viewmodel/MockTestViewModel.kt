package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.Mock
import com.krish.jobspot.model.MockDetail
import com.krish.jobspot.model.MockTestState
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MockTestViewModelTAG"

class MockTestViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId by lazy { mFirebaseAuth.currentUser?.uid.toString() }

    private val _mockTestStatus: MutableLiveData<MutableList<MockTestState>> =
        MutableLiveData(mutableListOf())
    val mockTestStatus: LiveData<MutableList<MockTestState>> = _mockTestStatus

    var mock: Mock = Mock()

    private val eventChannel = Channel<UiState>()
    val eventFlow = eventChannel.receiveAsFlow()

    fun fetchMockTestStatus() {
        mRealtimeDb
            .child(COLLECTION_PATH_STUDENT)
            .child(studentId)
            .child(COLLECTION_PATH_MOCK)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attemptedQuizIds =
                        snapshot.children.map { it.getValue(String::class.java) ?: "" }

                    mFirestore.collection(COLLECTION_PATH_MOCK)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }

                            val mockStates = value!!.documents
                                .map { it.toObject(Mock::class.java)!! }
                                .map { createQuizState(it, attemptedQuizIds) }
                                .toMutableList()

                            _mockTestStatus.postValue(mockStates)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Error : ${error.message}")
                }
            })
    }

    fun createQuizState(mock: Mock, attemptedQuizIds: List<String>): MockTestState {
        val hasAttempted = attemptedQuizIds.contains(mock.uid)
        return MockTestState(quizUid = mock.uid, hasAttempted = hasAttempted, quizName = mock.title)
    }


    fun updateStudentTestStatus(mockId: String) {
        viewModelScope.launch {
            mRealtimeDb
                .child(COLLECTION_PATH_STUDENT)
                .child(studentId)
                .child(COLLECTION_PATH_MOCK)
                .child(mockId)
                .setValue(mockId)
                .await()

            val mockDetailRef = mRealtimeDb.child(COLLECTION_PATH_MOCK).child(mockId).get().await()
            val mockDetail = mockDetailRef.getValue(MockDetail::class.java)!!
            mockDetail.studentIds.add(studentId)

            mRealtimeDb.child(COLLECTION_PATH_MOCK).child(mockId).setValue(mockDetail).await()
        }
    }

    fun fetchMockTest(mockTestId: String) {
        viewModelScope.launch {
            eventChannel.trySend(UiState.LOADING)
            val mockRef = mFirestore.collection(COLLECTION_PATH_MOCK).document(mockTestId).get().await()
            val mockTest = mockRef.toObject(Mock::class.java)!!
            mock = mockTest
            eventChannel.trySend(UiState.SUCCESS)
        }
    }
}