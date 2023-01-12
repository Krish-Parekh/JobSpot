package com.krish.jobspot.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUserId = mAuth.currentUser?.uid!!

    private val _jobs: MutableLiveData<List<Job>> = MutableLiveData(emptyList())
    val jobs: LiveData<List<Job>> = _jobs

    private val _countUpdater: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val countUpdater: MutableLiveData<Pair<Int, Int>> = _countUpdater


    fun fetchMetricForCurrentUser() {
        viewModelScope.launch {
            val companiesCountDeffered = CompletableDeferred<Int>()
            val companiesCountListener = mFireStore.collection(COLLECTION_PATH_COMPANY)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        companiesCountDeffered.completeExceptionally(error)
                        return@addSnapshotListener
                    }
                    val count = value?.documents?.count()!!
                    companiesCountDeffered.complete(count)
                }
            companiesCountDeffered.invokeOnCompletion {
                companiesCountListener.remove()
            }
            val companiesCount = companiesCountDeffered.await()

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

            jobAppliedCountDeffered.invokeOnCompletion {
                jobAppliedRef.removeEventListener(jobAppliedCountListener)
            }

            val jobAppliedCount = jobAppliedCountDeffered.await()
            _countUpdater.postValue(Pair(companiesCount, jobAppliedCount))
        }
    }

    fun fetchJobs() {
        viewModelScope.launch {
            mFireStore
                .collection(COLLECTION_PATH_COMPANY)
                .orderBy("uid", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val documents = value!!.documents
                    val jobList = documents.map { document ->
                        document.toObject(Job::class.java)!!
                    }
                    _jobs.postValue(jobList)
                }
        }
    }
}