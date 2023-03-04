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
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


private const val TAG = "HomeViewModelTAG"

/**
 * The [HomeViewModel] class is responsible for handling data operations related to the home screen.
 * It extends [ViewModel] and provides [LiveData] objects to observe changes in the data.
 */
class HomeViewModel : ViewModel() {
    // Initialize Firestore, Realtime Database, and Firebase Auth instances using lazy initialization
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Get the ID of the currently logged-in user
    private val currentUserId = mAuth.currentUser?.uid!!
    // ListenerRegistration object to remove the snapshot listener when the ViewModel is cleared
    private var jobListener: ListenerRegistration? = null

    /*
    * MutableLiveData objects to hold changes in the metrics
    * LiveData objects to observe changes in the metrics
    * */
    private val _metrics: MutableLiveData<Resource<Pair<Int, Int>>> = MutableLiveData()
    val metrics: LiveData<Resource<Pair<Int, Int>>> = _metrics

    /*
    * MutableLiveData objects to hold changes in the jobs
    * LiveData objects to observe changes in the jobs
    * */
    private val _jobs: MutableLiveData<Resource<List<Job>>> = MutableLiveData()
    val jobs: LiveData<Resource<List<Job>>> = _jobs

    /**
     * Fetches the metrics related to the home screen (e.g. number of companies, number of jobs applied to).
     * The result is posted to the [_metrics] LiveData object.
     */
    fun fetchMetrics() {
        viewModelScope.launch(IO) {
            try {
                _metrics.postValue(Resource.loading())

                // Get the companies count and jobs applied count using coroutines
                val companiesCountDeffered = async { getCompaniesCount() }
                val jobsAppliedCountDeffered = async { getJobsAppliedCount() }
                val companiesCount = companiesCountDeffered.await()
                val jobsAppliedCount = jobsAppliedCountDeffered.await()
                val metric = Pair(companiesCount, jobsAppliedCount)

                _metrics.postValue(Resource.success(metric))
            } catch (error: Exception) {
                Log.d(TAG, "fetchMetrics Error: ${error.message}")
                _metrics.postValue(Resource.error(error.message!!))
            }
        }
    }

    /**
     * Gets the count of companies in the Firestore database.
     * @return The count of companies.
     */
    private suspend fun getCompaniesCount(): Int {
        val companiesCountDeffered = CompletableDeferred<Int>()
        val companiesRef = mFirestore.collection(COLLECTION_PATH_COMPANY)
        val companiesCountListener = companiesRef
            .addSnapshotListener { value, error ->
                if (error != null) {
                    companiesCountDeffered.completeExceptionally(error)
                    return@addSnapshotListener
                }
                val companies = value?.documents!!
                val count = companies.count()
                companiesCountDeffered.complete(count)
            }

        companiesCountDeffered.invokeOnCompletion { error ->
            Log.d(TAG, "fetchMetrics Error: ${error?.message}")
            companiesCountListener.remove()
        }
        return companiesCountDeffered.await()
    }
    /**
     * Gets the count of jobs applied to by the current user in the Realtime Database.
     * @return The count of jobs applied to.
     */
    private suspend fun getJobsAppliedCount(): Int {
        val jobAppliedPath = "$COLLECTION_PATH_STUDENT/$currentUserId/$COLLECTION_PATH_COMPANY"
        val jobAppliedRef = mRealtimeDb.child(jobAppliedPath)
        val jobAppliedCountDeffered = CompletableDeferred<Int>()
        val jobAppliedCountListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appliedCount = snapshot.childrenCount.toInt()
                jobAppliedCountDeffered.complete(appliedCount)
            }

            override fun onCancelled(error: DatabaseError) {
                jobAppliedCountDeffered.completeExceptionally(error.toException())
            }
        }
        jobAppliedRef.addValueEventListener(jobAppliedCountListener)
        jobAppliedCountDeffered.invokeOnCompletion { error ->
            Log.d(TAG, "fetchMetrics Error: ${error?.message}")
            jobAppliedRef.removeEventListener(jobAppliedCountListener)
        }
        return jobAppliedCountDeffered.await()
    }

    /**
     * Fetches a list of jobs from the Firestore database and updates the [_jobs] live data.
     * If an error occurs during the fetch, the [_jobs] live data is updated with an error resource.
     */
    fun fetchJobs() {
        viewModelScope.launch(IO) {
            _jobs.postValue(Resource.loading())
            val jobsRef = mFirestore.collection(COLLECTION_PATH_COMPANY).orderBy("uid", DESCENDING)
            jobListener = jobsRef.addSnapshotListener { value, error ->
                if (error != null) {
                    _jobs.postValue(Resource.error(error.message!!))
                    return@addSnapshotListener
                }
                val jobs = value!!.documents
                val jobList = jobs.map { documentSnapshot ->
                    documentSnapshot.toObject(Job::class.java)!!
                }
                _jobs.postValue(Resource.success(jobList))
            }
        }
    }

    override fun onCleared() {
        jobListener?.remove()
        super.onCleared()
    }
}