package com.krish.jobspot.user_details.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.krish.jobspot.model.Student
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Constants.Companion.PROFILE_IMAGE_PATH
import com.krish.jobspot.util.Constants.Companion.RESUME_PATH
import com.krish.jobspot.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val TAG = "UserDetailViewModel"

class UserDetailViewModel : ViewModel() {
    private var imageUri: Uri? = null
    private var pdfUri: Uri? = null
    var resumeFileName: String? = null
    var fileMetaData: String? = null
    private val mFireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mAuth : FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val _uploadDataStatus: MutableLiveData<UiState> = MutableLiveData(UiState.LOADING)
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
            _uploadDataStatus.postValue(UiState.LOADING)
            viewModelScope.launch(Dispatchers.IO) {
                val metaData = StorageMetadata.Builder()
                    .setCustomMetadata("fileName", resumeFileName)
                    .setCustomMetadata("fileMetaData", fileMetaData)
                    .build()
                val resumeDownloadUrl = uploadData(path = "$RESUME_PATH/$fileName", fileUri = pdfUri, metadata = metaData)
                student.academic?.resumeUrl = resumeDownloadUrl

                val imageDownloadUrl = uploadData(path = "$PROFILE_IMAGE_PATH/$fileName", fileUri = imageUri, metadata = null)
                student.details?.imageUrl = imageDownloadUrl

                val profileUpdate = UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(imageDownloadUrl)).build()
                val currentUser = mAuth.currentUser!!
                currentUser.updateProfile(profileUpdate).await()

                Log.d(TAG, "Final user: $student")
                mFireStore.collection(COLLECTION_PATH_STUDENT).document(studentUid).set(student)
                    .await()
                _uploadDataStatus.postValue(UiState.SUCCESS)
                Log.d(TAG, "Upload Student Data Success: ")
            }
        } catch (error: Exception) {
            Log.d(TAG, "Error : ${error.message}")
            _uploadDataStatus.postValue(UiState.FAILURE)
        }
    }

    private suspend fun uploadData(path: String, fileUri: Uri, metadata: StorageMetadata?): String {
        val storageReference = mFireStorage.reference.child(path)
        storageReference.putFile(fileUri).await()
        if (metadata != null){
            storageReference.updateMetadata(metadata).await()
        }
        val downloadUrl = storageReference.downloadUrl.await().toString()
        return downloadUrl
    }

}