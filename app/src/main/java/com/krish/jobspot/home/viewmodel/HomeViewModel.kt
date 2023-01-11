package com.krish.jobspot.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val mFireStore : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb : DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val currentUserId = mAuth.currentUser?.uid!!

    private val _jobs: MutableLiveData<List<Job>> = MutableLiveData(emptyList())
    val jobs: LiveData<List<Job>> = _jobs

    private val _countUpdater : MutableLiveData<Pair<Int,Int>> = MutableLiveData()
    val countUpdater : MutableLiveData<Pair<Int, Int>> = _countUpdater


    fun fetchMetricForCurrentUser(){
        viewModelScope.launch {
            val companiesCount = mFireStore.collection(COLLECTION_PATH_COMPANY).get().await().count()
            val jobsAppliedCount = mRealtimeDb
                .child(COLLECTION_PATH_STUDENT)
                .child(currentUserId)
                .child(COLLECTION_PATH_COMPANY)
                .get().await().childrenCount
            _countUpdater.postValue(Pair(companiesCount, jobsAppliedCount.toInt()))
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