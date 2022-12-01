package com.krish.jobspot.user_details.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krish.jobspot.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

private const val TAG = "UserDetailViewModel"
class UserDetailViewModel : ViewModel() {

    private val mFireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun uploadStudentData(pdfUri: Uri, imageUri: Uri, student: Student){
        val studentName = student.details?.username
        val studentUid = student.uid
        val fileName = "$studentName:$studentUid"

        viewModelScope.launch(Dispatchers.IO) {
            val ref = mFireStorage.reference
            val resumeUploadTask = ref
                .child("students/resume/${fileName}")
                .putFile(pdfUri)
                .asDeferred()
                .await()

            Log.d(TAG, "Resume Upload Success")
            val resumeDownloadUrl = ref
                .child("students/resume/${fileName}")
                .downloadUrl.asDeferred().await()
            student.academic?.resumeUrl = resumeDownloadUrl.toString()
            Log.d(TAG, "Resume Download Url Success")

            val imageUploadTask = ref
                .child("students/profileImage/${fileName}")
                .putFile(imageUri)
                .asDeferred()
                .await()
            Log.d(TAG, "Image Upload Success")

            val imageDownloadUrl = ref
                .child("students/profileImage/${fileName}")
                .downloadUrl.asDeferred().await()
            student.details?.imageUrl = imageDownloadUrl.toString()
            Log.d(TAG, "Image Download Url Success")

            val uploadData = mFireStore
                .collection("students")
                .document(studentUid.toString())
                .set(student)
                .await()

            Log.d(TAG, "Upload Student Data Success: ")
        }
    }

}