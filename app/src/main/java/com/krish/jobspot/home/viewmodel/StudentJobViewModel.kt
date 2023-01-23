package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.krish.jobspot.model.JobApplication
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "StudentJobViewModelTAG"

class StudentJobViewModel : ViewModel() {

    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val currentStudentId by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    private val _applicationStatus: MutableLiveData<Resource<String>> = MutableLiveData()
    val applicationStatus: LiveData<Resource<String>> = _applicationStatus

    private val _jobMetric: MutableLiveData<Resource<Pair<Int, Boolean>>> = MutableLiveData()
    val jobMetric: LiveData<Resource<Pair<Int, Boolean>>> = _jobMetric

    fun fetchJobMetrics(jobId: String) {
        viewModelScope.launch(IO) {
            try {
                Log.d(TAG, "fetchJobMetrics called: ")
                _jobMetric.postValue(Resource.loading())
                val studentCountDeffered = async { getStudentCount(jobId) }
                val hasJobAppliedDeffered = async { getJobStatus(jobId) }
                val studentCount = studentCountDeffered.await()
                val hasJobApplied = hasJobAppliedDeffered.await()
                val jobMetric = Pair(studentCount, hasJobApplied)
                Log.d(TAG, "JobMetric : $jobMetric")
                _jobMetric.postValue(Resource.success(jobMetric))
            } catch (error: Exception) {
                val errorMessage = error.message ?: ""
                _jobMetric.postValue(Resource.error(errorMessage))
            }
        }
    }

    private suspend fun getStudentCount(jobId: String): Int {
        val appliedCountPath = "$COLLECTION_PATH_COMPANY/$jobId"
        val appliedCountRef = mRealtimeDb.child(appliedCountPath)
        val appliedCountDeffered = CompletableDeferred<Int>()
        val appliedCountListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appliedCount = snapshot.children.count()
                Log.d(TAG, "AppliedCount : $appliedCount")
                appliedCountDeffered.complete(appliedCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                appliedCountDeffered.completeExceptionally(error.toException())
            }
        }
        appliedCountRef.addValueEventListener(appliedCountListener)
        appliedCountDeffered.invokeOnCompletion {
            appliedCountRef.removeEventListener(appliedCountListener)
        }
        return appliedCountDeffered.await()
    }

    private suspend fun getJobStatus(jobId: String): Boolean {
        val jobStatusPath =
            "$COLLECTION_PATH_STUDENT/$currentStudentId/$COLLECTION_PATH_COMPANY/$jobId"
        val jobStatusRef = mRealtimeDb.child(jobStatusPath)
        val jobStatusDeffered = CompletableDeferred<Boolean>()
        val jobStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    jobStatusDeffered.complete(true)
                } else {
                    jobStatusDeffered.complete(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                jobStatusDeffered.completeExceptionally(error.toException())
            }
        }
        jobStatusRef.addValueEventListener(jobStatusListener)
        jobStatusDeffered.invokeOnCompletion {
            jobStatusRef.removeEventListener(jobStatusListener)
        }
        return jobStatusDeffered.await()
    }

    fun applyJob(jobId: String) {
        viewModelScope.launch(IO) {
            try {
                _applicationStatus.postValue(Resource.loading())
                val jobApplication = JobApplication(jobId = jobId, studentId = currentStudentId!!)
                val jobApplicationPath = "$COLLECTION_PATH_COMPANY/$jobId/$currentStudentId"
                val studentJobApplicationPath = "$COLLECTION_PATH_STUDENT/$currentStudentId/$COLLECTION_PATH_COMPANY/$jobId"

                val jobApplicationRef = mRealtimeDb.child(jobApplicationPath)
                jobApplicationRef.setValue(jobApplication).await()

                val studentJobApplicationRef = mRealtimeDb.child(studentJobApplicationPath)
                studentJobApplicationRef.setValue(jobApplication).await()
                _applicationStatus.postValue(Resource.success("Job applied success."))
            } catch (error: Exception) {
                val errorMessage = error.message ?: ""
                _applicationStatus.postValue(Resource.error(errorMessage))
            }
        }
    }
}