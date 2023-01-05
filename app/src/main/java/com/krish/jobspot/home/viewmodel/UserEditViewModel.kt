package com.krish.jobspot.home.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krish.jobspot.model.Student
import com.krish.jobspot.model.Tpo
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_TPO
import com.krish.jobspot.util.Constants.Companion.PROFILE_IMAGE_PATH
import com.krish.jobspot.util.Constants.Companion.RESUME_PATH
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserEditViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId: String by lazy { firebaseAuth.currentUser?.uid.toString() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val mFirebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var imageUri: Uri? = null

    fun setImageUri(imageUri: Uri) {
        this.imageUri = imageUri
    }

    fun getImageUri(): Uri? {
        return this.imageUri
    }

    private val _student: MutableLiveData<Student> = MutableLiveData()
    val student: LiveData<Student> = _student

    private val _fileData: MutableLiveData<Pair<String, String>> = MutableLiveData()
    val fileData: LiveData<Pair<String, String>> = _fileData

    fun fetchStudent() {
        viewModelScope.launch {
            val studentRef =
                mFirestore.collection(COLLECTION_PATH_STUDENT).document(studentId).get().await()
            val student = studentRef.toObject(Student::class.java)!!
            _student.postValue(student)
        }
    }

    fun fetchStudentResume() {
        viewModelScope.launch {
            val resumeRef = mFirebaseStorage.reference.child(RESUME_PATH).child(studentId)
            val metaData = resumeRef.metadata.await()
            val fileName = metaData.getCustomMetadata("fileName") ?: ""
            val fileMetaData = metaData.getCustomMetadata("fileMetaData") ?: ""
            _fileData.postValue(Pair(fileName, fileMetaData))
        }
    }

    fun fetchTpo(){
        val tempTpoList = mutableListOf<Tpo>()
        viewModelScope.launch {
            mFirestore.collection(COLLECTION_PATH_TPO).addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                val documents = value?.documents!!
                val tpoList = documents.map {
                    it.toObject(Tpo::class.java)!!
                }
                tempTpoList.clear()
                tempTpoList.addAll(tpoList.toMutableList())
            }
        }
    }

    fun uploadStudentData(student : Student){
        viewModelScope.launch {
            val studentDetail = student.details!!
            if (!studentDetail.imageUrl.startsWith("https://firebasestorage.googleapis.com/")){
                val editStudentRef = mFirebaseStorage.getReference(PROFILE_IMAGE_PATH).child(student.uid.toString())
                editStudentRef.putFile(Uri.parse(studentDetail.imageUrl)).await()
                student.details?.imageUrl = editStudentRef.downloadUrl.await().toString()
            }

            val editStudentRef = mFirestore.collection(COLLECTION_PATH_STUDENT).document(student.uid.toString())
            editStudentRef.set(student).await()
        }
    }
}