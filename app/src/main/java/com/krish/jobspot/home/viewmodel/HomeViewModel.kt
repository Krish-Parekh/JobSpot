package com.krish.jobspot.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.Job
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel : ViewModel() {
    private val mFireStore : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _jobs: MutableLiveData<MutableList<Job>> = MutableLiveData(mutableListOf())
    val jobs: LiveData<MutableList<Job>> = _jobs

    fun fetchJobs() {
        viewModelScope.launch {
            mFireStore.collection(COLLECTION_PATH_COMPANY)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    _jobs.value?.clear()
                    val documents = value!!.documents
                    val jobList = documents.map { document ->
                        document.toObject(Job::class.java)!!
                    }
                    _jobs.postValue(jobList.toMutableList())
                }
        }
    }
}