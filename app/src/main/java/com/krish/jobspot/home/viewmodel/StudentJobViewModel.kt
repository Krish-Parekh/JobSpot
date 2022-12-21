package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.krish.jobspot.model.JobApplication
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "StudentJobViewModelTAG"

class StudentJobViewModel : ViewModel() {

    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }

    private val _applicationStatus: MutableLiveData<UiState> = MutableLiveData(UiState.LOADING)
    val applicationStatus: LiveData<UiState> = _applicationStatus

    private val _checkJobStatus: MutableLiveData<UiState> = MutableLiveData(UiState.LOADING)
    val checkJobStatus: LiveData<UiState> = _checkJobStatus

    var isJobApplicationSubmitted: Boolean = false

    fun applyJob(jobApplication: JobApplication) {
        viewModelScope.launch {
            try {
                _applicationStatus.postValue(UiState.LOADING)
                val jobId = jobApplication.jobId
                val studentId = jobApplication.studentId
                val jobApplicationRef =
                    mRealtimeDb.child(COLLECTION_PATH_COMPANY).child(jobId).child(studentId)
                jobApplicationRef.setValue(jobApplication).await()

                val studentJobRef = mRealtimeDb.child(COLLECTION_PATH_STUDENT).child(studentId)
                    .child(COLLECTION_PATH_COMPANY).child(jobId)
                studentJobRef.setValue(jobId).await()
                _applicationStatus.postValue(UiState.SUCCESS)
            } catch (e: Exception) {
                Log.d(TAG, "applyJob: ${e.message}")
                _applicationStatus.postValue(UiState.FAILURE)
            }
        }
    }

    fun checkJobStatus(jobId: String, studentId: String) {
        _checkJobStatus.postValue(UiState.LOADING)
        val studentJobRef = mRealtimeDb.child(COLLECTION_PATH_STUDENT).child(studentId)
            .child(COLLECTION_PATH_COMPANY).child(jobId)
        studentJobRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isJobApplicationSubmitted = true
                }
                _checkJobStatus.postValue(UiState.SUCCESS)
            }

            override fun onCancelled(e: DatabaseError) {
                Log.d(TAG, "applyJob: ${e.message}")
                _checkJobStatus.postValue(UiState.FAILURE)
            }
        })
    }
}