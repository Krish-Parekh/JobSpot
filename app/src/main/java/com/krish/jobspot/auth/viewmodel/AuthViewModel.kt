package com.krish.jobspot.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.util.Constants
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _loginStatus: MutableLiveData<Resource<LoginUiState>> = MutableLiveData()
    val loginStatus: LiveData<Resource<LoginUiState>> = _loginStatus

    private val _signupStatus: MutableLiveData<Resource<Pair<String, String>>> = MutableLiveData()
    val signupStatus: LiveData<Resource<Pair<String, String>>> = _signupStatus

    private val _resendPasswordStatus: MutableLiveData<Resource<String>> = MutableLiveData()
    val resendPasswordStatus: LiveData<Resource<String>> = _resendPasswordStatus

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loginStatus.postValue(Resource.loading())
                mAuth.signInWithEmailAndPassword(email, password).await()
                val currentUserUid = mAuth.currentUser?.uid!!
                val currentUsername = mAuth.currentUser?.displayName!!

                val currentUserRole =
                    mFirestore.collection(Constants.COLLECTION_PATH_ROLE).document(currentUserUid)
                val roleDocument: DocumentSnapshot = currentUserRole.get().await()

                if (roleDocument.exists().not()) {
                    _loginStatus.postValue(Resource.error("Invalid Credentials."))
                    return@launch
                }

                val roleType: String = roleDocument.get("role") as String
                val currentUserRef = mFirestore.collection(Constants.COLLECTION_PATH_STUDENT)
                    .document(currentUserUid)
                val currentUser: DocumentSnapshot = currentUserRef.get().await()
                val currentUserInfoExist = currentUser.exists()

                val loginUiState = LoginUiState(
                    username = currentUsername,
                    email = email,
                    roleType = roleType,
                    userInfoExist = currentUserInfoExist
                )

                _loginStatus.postValue(Resource.success(loginUiState))
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _loginStatus.postValue(Resource.error("Invalid email or password."))
            } catch (e: FirebaseAuthInvalidUserException) {
                _loginStatus.postValue(Resource.error("The specified user does not exist."))
            } catch (e: FirebaseNetworkException) {
                _loginStatus.postValue(Resource.error("A network error has occurred."))
            } catch (e: Exception) {
                _loginStatus.postValue(Resource.error("Account doesn't exist."))
            }
        }
    }

    fun signup(
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _signupStatus.postValue(Resource.loading())
                mAuth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = mAuth.currentUser!!
                val profileBuilder = UserProfileChangeRequest.Builder()
                val currentUserProfile = profileBuilder.setDisplayName(username).build()
                currentUser.updateProfile(currentUserProfile).await()
                val userRole = hashMapOf("role" to Constants.ROLE_TYPE_STUDENT)

                val roleRef =
                    mFirestore.collection(Constants.COLLECTION_PATH_ROLE).document(currentUser.uid)
                roleRef.set(userRole).await()
                val signupState = Pair(username, email)
                _signupStatus.postValue(Resource.success(signupState))
            } catch (error: FirebaseAuthUserCollisionException) {
                _signupStatus.postValue(Resource.error("Email already exists."))
            }
        }
    }

    fun resendPassword(email: String) {
        viewModelScope.launch(IO) {
            _resendPasswordStatus.postValue(Resource.loading())
            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    _resendPasswordStatus.postValue(Resource.success("Resend password send success."))
                }
                .addOnFailureListener { error ->
                    _resendPasswordStatus.postValue(Resource.error("Resend password failed."))
                }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val email: String = "",
    val roleType: String = "",
    val userInfoExist: Boolean = false
)