package com.krish.jobspot.user_details.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krish.jobspot.model.Student
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Constants.Companion.PROFILE_IMAGE_PATH
import com.krish.jobspot.util.Constants.Companion.RESUME_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UiState(
    var loading: Boolean = false,
    var success: Boolean = false,
    val failed: Boolean = false,
)

private const val TAG = "UserDetailViewModel"

class UserDetailViewModel : ViewModel() {
    private var imageUri: Uri? = null;
    private var pdfUri: Uri? = null
    private val mFireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _uploadDataStatus: MutableLiveData<UiState> = MutableLiveData(UiState())
    val uploadDataStatus: LiveData<UiState> = _uploadDataStatus

    fun setPdfUri(pdfUri: Uri?) {
        this.pdfUri = pdfUri
    }

    fun getPdfUri(): Uri? {
        return this.pdfUri
    }

    fun setImageUri(imageUri: Uri) {
        this.imageUri = imageUri
    }

    fun getImageUri(): Uri? {
        return this.imageUri
    }

    fun uploadStudentData(pdfUri: Uri, imageUri: Uri, student: Student) {
        val studentUid = student.uid.toString()
        val fileName = studentUid

        try {
            _uploadDataStatus.postValue(UiState(loading = true))
            viewModelScope.launch(Dispatchers.IO) {
                val resumeDownloadUrl =
                    uploadData(path = "$RESUME_PATH/$fileName", fileUri = pdfUri)
                student.academic?.resumeUrl = resumeDownloadUrl

                val imageDownloadUrl =
                    uploadData(path = "$PROFILE_IMAGE_PATH/$fileName", fileUri = imageUri)
                student.details?.imageUrl = imageDownloadUrl

                Log.d(TAG, "Final user: $student")
                mFireStore.collection(COLLECTION_PATH_STUDENT).document(studentUid).set(student)
                    .await()
                _uploadDataStatus.postValue(UiState(success = true))
                Log.d(TAG, "Upload Student Data Success: ")
            }
        } catch (error: Exception) {
            Log.d(TAG, "Error : ${error.message}")
            _uploadDataStatus.postValue(UiState(failed = true))
        }
    }

    private suspend fun uploadData(path: String, fileUri: Uri): String {
        val storageReference = mFireStorage.reference.child(path)
        storageReference.putFile(fileUri).await()
        val downloadUrl = storageReference.downloadUrl.await().toString()
        return downloadUrl
    }

}