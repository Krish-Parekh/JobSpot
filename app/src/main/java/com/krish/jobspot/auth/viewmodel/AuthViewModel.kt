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

/**
 * ViewModel for authentication functionality.
 */
class AuthViewModel : ViewModel() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    /**
     * MutableLiveData to hold login status.
     * LiveData to observe login status.
     */
    private val _loginStatus: MutableLiveData<Resource<LoginUiState>> = MutableLiveData()
    val loginStatus: LiveData<Resource<LoginUiState>> = _loginStatus

    /**
     * MutableLiveData to hold signup status.
     * LiveData to observe signup status.
     */
    private val _signupStatus: MutableLiveData<Resource<Pair<String, String>>> = MutableLiveData()
    val signupStatus: LiveData<Resource<Pair<String, String>>> = _signupStatus

    /**
     * MutableLiveData to hold resend password status.
     * LiveData to observe resend password status.
     */
    private val _resendPasswordStatus: MutableLiveData<Resource<String>> = MutableLiveData()
    val resendPasswordStatus: LiveData<Resource<String>> = _resendPasswordStatus

    /**
     * Function to authenticate user with email and password.
     * @param email User's email address.
     * @param password User's password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loginStatus.postValue(Resource.loading())

                // Authenticate user with Firebase Authentication.
                mAuth.signInWithEmailAndPassword(email, password).await()

                // Get current user's ID and username.
                val currentUserUid = mAuth.currentUser?.uid!!
                val currentUsername = mAuth.currentUser?.displayName!!

                // Get current user's role type from Firestore.
                val currentUserRole =
                    mFirestore.collection(Constants.COLLECTION_PATH_ROLE).document(currentUserUid)
                val roleDocument: DocumentSnapshot = currentUserRole.get().await()

                // If role document doesn't exist, return error.
                if (roleDocument.exists().not()) {
                    _loginStatus.postValue(Resource.error("Invalid Credentials."))
                    return@launch
                }

                // Get current user's role type and check if user info exists in Firestore.
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

    /**
     * Function to create new user account.
     * @param username User's username.
     * @param email User's email address.
     * @param password User's password.
     */
    fun signup(
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch(IO) {
            try {
                _signupStatus.postValue(Resource.loading())

                // Register the new user with the provided email and password.
                mAuth.createUserWithEmailAndPassword(email, password).await()

                // Update the user's display name to the provided username.
                val currentUser = mAuth.currentUser!!
                val profileBuilder = UserProfileChangeRequest.Builder()
                val currentUserProfile = profileBuilder.setDisplayName(username).build()
                currentUser.updateProfile(currentUserProfile).await()

                // Assign the user the role of a student.
                val userRole = hashMapOf("role" to Constants.ROLE_TYPE_STUDENT)

                val roleRef =
                    mFirestore.collection(Constants.COLLECTION_PATH_ROLE).document(currentUser.uid)
                roleRef.set(userRole).await()

                // Post the success status along with the username and email of the newly registered user.
                val signupState = Pair(username, email)
                _signupStatus.postValue(Resource.success(signupState))
            } catch (error: FirebaseAuthUserCollisionException) {
                // Post an error status if the provided email already exists in the system.
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
                .addOnFailureListener {
                    _resendPasswordStatus.postValue(Resource.error("Resend password failed."))
                }
        }
    }
}

/**
 * Data class representing the login UI state.
 *
 * @property username the username of the logged in user.
 * @property email the email of the logged in user.
 * @property roleType the role type of the logged in user.
 * @property userInfoExist a flag indicating whether the user's information exists.
 */
data class LoginUiState(
    val username: String = "",
    val email: String = "",
    val roleType: String = "",
    val userInfoExist: Boolean = false
)