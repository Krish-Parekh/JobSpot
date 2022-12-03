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
import kotlinx.coroutines.withContext

private const val TAG = "UserDetailViewModel"

class UserDetailViewModel : ViewModel() {

    private val mFireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun uploadStudentData(pdfUri: Uri, imageUri: Uri, student: Student) {
        val studentName = student.details?.username
        val studentUid = student.uid
        val fileName = "$studentName:$studentUid"

        viewModelScope.launch(Dispatchers.IO) {
            val resumeDownloadUrl = withContext(Dispatchers.IO) {
                uploadData(path = "students/resume/${fileName}", fileUri = pdfUri)
            }
            student.academic?.resumeUrl = resumeDownloadUrl

            val imageDownloadUrl = withContext(Dispatchers.IO) {
                uploadData(path = "students/profileImage/${fileName}", fileUri = imageUri)
            }

            student.details?.imageUrl = imageDownloadUrl

            val uploadData = mFireStore
                .collection("students")
                .document(studentUid.toString())
                .set(student)
                .await()

            Log.d(TAG, "Upload Student Data Success: ")

        }
    }

    private suspend fun uploadData(path: String, fileUri: Uri): String {
        val ref = mFireStorage.reference.child(path)
        ref.putFile(fileUri).asDeferred().await()

        val downloadUrl = ref.downloadUrl.asDeferred().await().toString()
        return downloadUrl
    }

}