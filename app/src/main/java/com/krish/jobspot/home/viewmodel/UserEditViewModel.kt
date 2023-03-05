package com.krish.jobspot.home.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.krish.jobspot.model.Student
import com.krish.jobspot.model.Tpo
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_COMPANY
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_MOCK_RESULT
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_ROLE
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_STUDENT
import com.krish.jobspot.util.Constants.Companion.COLLECTION_PATH_TPO
import com.krish.jobspot.util.Constants.Companion.PROFILE_IMAGE_PATH
import com.krish.jobspot.util.Constants.Companion.RESUME_PATH
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "UserEditViewModelTAG"

class UserEditViewModel : ViewModel() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val studentId: String by lazy { mAuth.currentUser?.uid.toString() }
    private val mStorage: StorageReference by lazy { FirebaseStorage.getInstance().reference }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mRealtimeDb: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private var tpoListener : ListenerRegistration? = null

    private var imageUri: Uri? = null

    fun setImageUri(imageUri: Uri) {
        this.imageUri = imageUri
    }

    fun getImageUri(): Uri? {
        return this.imageUri
    }

    private val _student: MutableLiveData<Resource<Student>> = MutableLiveData()
    val student: LiveData<Resource<Student>> = _student

    private val _tpoList: MutableLiveData<List<Tpo>> = MutableLiveData(emptyList())
    val tpoList: LiveData<List<Tpo>> = _tpoList

    private val _updateState: MutableLiveData<Resource<String>> = MutableLiveData()
    val updateState: LiveData<Resource<String>> = _updateState

    private val _resumeState: MutableLiveData<Resource<Triple<String, String, Uri>>> = MutableLiveData()
    val resumeState: LiveData<Resource<Triple<String, String, Uri>>> = _resumeState

    private val _deleteState: MutableLiveData<Resource<String>> = MutableLiveData()
    val deleteState: LiveData<Resource<String>> = _deleteState

    fun fetchStudent() {
        viewModelScope.launch(IO) {
            try {
                _student.postValue(Resource.loading())
                val studentRef = mFirestore.collection(COLLECTION_PATH_STUDENT).document(studentId).get().await()
                val student = studentRef.toObject(Student::class.java)!!
                _student.postValue(Resource.success(student))
            } catch (error: Exception) {
                Log.d(TAG, "Error: ${error.message}")
                _student.postValue(Resource.error(error.message!!))
            }
        }
    }

    fun fetchResume() {
        viewModelScope.launch(IO) {
            try {
                _resumeState.postValue(Resource.loading())
                val resumeRef = mStorage.child(RESUME_PATH).child(studentId)
                val resumeUri = resumeRef.downloadUrl.await()
                val metaData = resumeRef.metadata.await()
                val fileName = metaData.getCustomMetadata("fileName") ?: ""
                val fileMetaData = metaData.getCustomMetadata("fileMetaData") ?: ""
                val resumeData = Triple(fileName, fileMetaData, resumeUri)
                _resumeState.postValue(Resource.success(resumeData))
            } catch (error: Exception) {
                val errorMessage = error.message!!
                _resumeState.postValue(Resource.error(errorMessage))
            }
        }
    }

    fun fetchTpo() {
        viewModelScope.launch(IO) {
            tpoListener = mFirestore.collection(COLLECTION_PATH_TPO)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val documents = value?.documents!!
                    val tpoList = documents.map {
                        it.toObject(Tpo::class.java)!!
                    }
                    _tpoList.postValue(tpoList)
                }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(IO) {
            try {
                _updateState.postValue(Resource.loading())
                val studentDetail = student.details!!
                val firebaseStorageImagePrefix = "https://firebasestorage.googleapis.com/"
                if (studentDetail.imageUrl.startsWith(firebaseStorageImagePrefix).not()){
                    val studentImagePath = "$PROFILE_IMAGE_PATH/$studentId"
                    val studentImageRef = mStorage.child(studentImagePath)
                    studentImageRef.putFile(Uri.parse(studentDetail.imageUrl)).await()
                    studentDetail.imageUrl = studentImageRef.downloadUrl.await().toString()
                    student.details = studentDetail
                }

                val userProfileBuilder = UserProfileChangeRequest.Builder()
                val userProfile = userProfileBuilder
                    .setDisplayName(studentDetail.username)
                    .setPhotoUri(Uri.parse(studentDetail.imageUrl))
                    .build()

                val currentUser = mAuth.currentUser!!
                currentUser.updateProfile(userProfile).await()
                val currentUserEmail = currentUser.email!!
                if ((currentUserEmail != student.details!!.email)){
                    currentUser.updateEmail(student.details!!.email).await()
                }
                val editStudentRef = mFirestore.collection(COLLECTION_PATH_STUDENT).document(studentId)
                editStudentRef.set(student).await()
                _updateState.postValue(Resource.success("Student update success."))
            } catch (error : Exception) {
                val errorMessage = error.message!!
                _updateState.postValue(Resource.error(errorMessage))
            }
        }
    }

    fun deleteAccount(student: Student) {
        viewModelScope.launch {
            try {
                val studentId = student.uid!!
                val studentImagePath = "$PROFILE_IMAGE_PATH/$studentId"
                val studentResumePath = "$RESUME_PATH/$studentId"
                val studentDatabasePath = "$COLLECTION_PATH_STUDENT/$studentId"

                _deleteState.postValue(Resource.loading())
                // delete user from authentication
                mAuth.currentUser?.delete()

                // remove student images
                mStorage.child(studentImagePath).delete().await()

                // remove student resume
                mStorage.child(studentResumePath).delete().await()

                // remove from student collection
                mFirestore.collection(COLLECTION_PATH_STUDENT).document(studentId).delete().await()

                // remove student roles
                mFirestore.collection(COLLECTION_PATH_ROLE).document(studentId).delete().await()

                // remove students from realtime db
                mRealtimeDb.child(studentDatabasePath).removeValue().await()

                // remove student from companies in realtime db
                deleteStudentFromCompany(studentId)
                Log.d(TAG, "Delete Student From Company Called.")
                // remove student from mock test in realtimeDb
                deleteStudentFromMockTest(studentId)

                // remove student from mock result in realtimeDb
                deleteStudentFromMockResult(studentId)
                _deleteState.postValue(Resource.success("Delete success."))
            } catch (error: Exception) {
                Log.d(TAG, "Error : ${error.message} ")
                _deleteState.postValue(Resource.error(error.message!!))
            }
        }
    }

    private suspend fun deleteStudentFromCompany(studentId: String) {
        val companiesRef = mRealtimeDb.child(COLLECTION_PATH_COMPANY)
        val studentDeleteFromCompaniesDeffered = CompletableDeferred<Unit>()
        val studentCompanyDeleteListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { companyNode ->
                    val doStudentExist = companyNode.hasChild(studentId)
                    if (doStudentExist) {
                        companyNode.ref.child(studentId).removeValue()

                    }
                }
                studentDeleteFromCompaniesDeffered.complete(Unit)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Error : ${error.message}")
                studentDeleteFromCompaniesDeffered.completeExceptionally(error.toException())
            }
        }
        companiesRef.addValueEventListener(studentCompanyDeleteListener)
        studentDeleteFromCompaniesDeffered.invokeOnCompletion {
            Log.d(TAG, "deleteStudentFromCompany: ${it?.message}")
            companiesRef.removeEventListener(studentCompanyDeleteListener)
        }
        studentDeleteFromCompaniesDeffered.await()
    }

    private suspend fun deleteStudentFromMockTest(studentId: String) {
        val mockTestRef = mRealtimeDb.child(COLLECTION_PATH_MOCK)
        val studentMockTestDeffered = CompletableDeferred<Unit>()
        val studentMockTestDeleteListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { mockTestNode ->
                    val mockTestNodeRef = mockTestNode.ref
                    val studentIdsRef = mockTestNodeRef.child("studentIds")
                    studentIdsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val studentIds = snapshot.value as List<*>?
                            if (studentIds != null) {
                                val updateStudentIds = studentIds.filter { it != studentId }
                                snapshot.ref.setValue(updateStudentIds)
                                    .addOnSuccessListener {
                                        studentMockTestDeffered.complete(Unit)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d(
                                            TAG,
                                            "deleteStudentFromMockTest: ${exception.message}"
                                        )
                                        studentMockTestDeffered.completeExceptionally(exception)
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "deleteStudentFromMockTest: ${error.message}")
                            studentMockTestDeffered.completeExceptionally(error.toException())
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "deleteStudentFromMockTest: ${error.message}")
                studentMockTestDeffered.completeExceptionally(error.toException())
            }
        }
        mockTestRef.addValueEventListener(studentMockTestDeleteListener)
        studentMockTestDeffered.invokeOnCompletion {
            Log.d(TAG, "deleteStudentFromMockTest: ${it?.message}")
            mockTestRef.removeEventListener(studentMockTestDeleteListener)
        }
        studentMockTestDeffered.await()
    }

    private suspend fun deleteStudentFromMockResult(studentId: String) {
        val mockResultRef = mRealtimeDb.child(COLLECTION_PATH_MOCK_RESULT)
        val studentDeleteFromMockResultDeffered = CompletableDeferred<Unit>()
        val studentMockResultDeleteListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { mockResultNode ->
                    val doStudentExist = mockResultNode.hasChild(studentId)
                    if (doStudentExist) {
                        mockResultNode.child(studentId).ref.removeValue()
                    }
                }
                studentDeleteFromMockResultDeffered.complete(Unit)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "deleteStudentFromMockResult: ${error.message}")
                studentDeleteFromMockResultDeffered.completeExceptionally(error.toException())
            }
        }
        mockResultRef.addValueEventListener(studentMockResultDeleteListener)
        studentDeleteFromMockResultDeffered.invokeOnCompletion {
            Log.d(TAG, "deleteStudentFromMockResult: ${it?.message}")
            mockResultRef.removeEventListener(studentMockResultDeleteListener)
        }
        studentDeleteFromMockResultDeffered.await()
    }

    override fun onCleared() {
        tpoListener?.remove()
        super.onCleared()
    }
}