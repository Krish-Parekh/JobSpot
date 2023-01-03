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
import com.krish.jobspot.model.MockQuestion
import com.krish.jobspot.model.MockResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val TAG = "MockSolutionViewModelTAG"

class MockSolutionViewModel : ViewModel() {
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private var listener: ValueEventListener? = null
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val studentId = mFirebaseAuth.currentUser?.uid.toString()

    private val _mockScore: MutableLiveData<MockResult> = MutableLiveData()
    val mockScore: LiveData<MockResult> = _mockScore

    private val _mockSolution: MutableLiveData<List<MockQuestion>> = MutableLiveData()
    val mockSolution: LiveData<List<MockQuestion>> = _mockSolution

    fun fetchMockResult(mockId: String) {
        viewModelScope.launch {
            try {

                listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val mockResult = snapshot.getValue(MockResult::class.java)!!
                            _mockScore.postValue(mockResult)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Error: ${error.message}")
                    }
                }
                mRealtimeDb.child("mock_result").child(mockId).child(studentId)
                    .addListenerForSingleValueEvent(listener!!)

                val mockTestRef = mFirestore.collection("mock_test").document(mockId).get().await()
                val mock = mockTestRef.toObject(Mock::class.java)!!
                _mockSolution.postValue(mock.mockQuestion)
                Log.d(TAG, "Wor")

            } catch (e: Exception) {
                Log.d(TAG, "Error : ${e.message}")
            } finally {
                listener = null
            }
        }
    }

    override fun onCleared() {
        if (listener != null){
            mRealtimeDb.removeEventListener(listener!!)
            listener = null
        }
        super.onCleared()
    }
}