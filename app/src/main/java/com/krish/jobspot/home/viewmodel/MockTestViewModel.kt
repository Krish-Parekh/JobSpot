package com.krish.jobspot.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.Quiz
import com.krish.jobspot.model.QuizState
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_QUIZ
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MockTestViewModelTAG"

class MockTestViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId by lazy { mFirebaseAuth.currentUser?.uid.toString() }
    private val _quiz: MutableLiveData<MutableList<QuizState>> = MutableLiveData(mutableListOf())
    val quiz: LiveData<MutableList<QuizState>> = _quiz

    fun fetchQuiz() {
        mRealtimeDb
            .child(COLLECTION_PATH_STUDENT)
            .child(studentId)
            .child(COLLECTION_PATH_QUIZ)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attemptedQuizIds =
                        snapshot.children.map { it.getValue(String::class.java) ?: "" }

                    mFirestore.collection(COLLECTION_PATH_QUIZ)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }

                            val quizStates = value!!.documents
                                .map { it.toObject(Quiz::class.java)!! }
                                .map { createQuizState(it, attemptedQuizIds) }
                                .toMutableList()

                            _quiz.postValue(quizStates)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Error : ${error.message}")
                }
            })
    }

    fun createQuizState(quiz: Quiz, attemptedQuizIds: List<String>): QuizState {
        val hasAttempted = attemptedQuizIds.contains(quiz.uid)
        return QuizState(quizUid = quiz.uid, hasAttempted = hasAttempted, quizName = quiz.title)
    }


    fun updateStudentTestStatus(mockId : String) {
        viewModelScope.launch {
            mRealtimeDb
                .child(COLLECTION_PATH_STUDENT)
                .child(studentId)
                .child(COLLECTION_PATH_QUIZ)
                .child(mockId)
                .setValue(mockId)
                .await()
        }
    }


}