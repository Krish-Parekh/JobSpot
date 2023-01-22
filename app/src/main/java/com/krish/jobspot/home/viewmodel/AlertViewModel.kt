package com.krish.jobspot.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.jobspot.model.BroadcastNotification
import com.krish.jobspot.model.HostNotification
import com.krish.jobspot.model.Notification
import com.krish.jobspot.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val studentId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }

    private val _notificationStatus: MutableLiveData<Resource<List<Notification>>> = MutableLiveData()
    val notificationStatus: LiveData<Resource<List<Notification>>> = _notificationStatus

    fun fetchNotifications() {
        viewModelScope.launch(IO) {
            try {
                _notificationStatus.postValue(Resource.loading())
                val broadcastDeffered = async { getBroadcastNotifications() }
                val hostSpecificDeffered = async { getHostSpecificNotifications() }

                val broadcastNotification = broadcastDeffered.await()
                val hostSpecificNotification = hostSpecificDeffered.await()
                val combinedNotification = broadcastNotification + hostSpecificNotification
                val latestNotification = combinedNotification.sortedByDescending { it.timestamp }
                _notificationStatus.postValue(Resource.success(latestNotification))
            } catch (error: Exception) {
                _notificationStatus.postValue(Resource.error(error.message.toString()))
            }
        }
    }

    private suspend fun getBroadcastNotifications(): List<BroadcastNotification> {
        val notificationRef = mFirestore.collection("notification")
        val broadcastNotificationsDeferred = CompletableDeferred<List<BroadcastNotification>>()
        val query = notificationRef.whereEqualTo("type", "BROADCAST")
        query.addSnapshotListener { value, error ->
            if (error != null) {
                broadcastNotificationsDeferred.completeExceptionally(error)
                return@addSnapshotListener
            }

            val notifications = value?.documents!!
            val broadcastNotifications = notifications.map {
                it.toObject(BroadcastNotification::class.java)!!
            }
            broadcastNotificationsDeferred.complete(broadcastNotifications)
        }
        return broadcastNotificationsDeferred.await()
    }

    private suspend fun getHostSpecificNotifications(): List<HostNotification> {
        val notificationRef = mFirestore.collection("notification")
        val hostSpecificNotificationsDeferred = CompletableDeferred<List<HostNotification>>()
        val hostQuery =
            notificationRef.whereEqualTo("type", "HOST").whereEqualTo("hostId", studentId)
        hostQuery.addSnapshotListener { value, error ->
            if (error != null) {
                hostSpecificNotificationsDeferred.completeExceptionally(error)
                return@addSnapshotListener
            }

            val notifications = value?.documents!!
            val hostSpecificNotifications = notifications.map {
                it.toObject(HostNotification::class.java)!!
            }
            hostSpecificNotificationsDeferred.complete(hostSpecificNotifications)
        }
        return hostSpecificNotificationsDeferred.await()
    }
}