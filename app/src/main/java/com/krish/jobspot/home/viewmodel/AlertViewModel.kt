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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {

    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val studentId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.toString() }

    private val _notifications : MutableLiveData<List<Notification>> = MutableLiveData()
    val notifications : LiveData<List<Notification>> = _notifications

    fun fetchNotifications() {
        viewModelScope.launch {
            val notificationRef = mFirestore.collection("notification")
            val broadcastNotificationsDeferred = CompletableDeferred<List<BroadcastNotification>>()
            val query = notificationRef.whereEqualTo("type", "BROADCAST")
            query.addSnapshotListener { value, error ->
                if (error != null){
                    broadcastNotificationsDeferred.completeExceptionally(error)
                    return@addSnapshotListener
                }

                val documents = value?.documents!!
                val broadcastNotifications = documents.map {
                    it.toObject(BroadcastNotification::class.java)!!
                }
                broadcastNotificationsDeferred.complete(broadcastNotifications)
            }

            val broadcastNotifications = broadcastNotificationsDeferred.await()

            val hostSpecificNotificationsDeferred = CompletableDeferred<List<HostNotification>>()
            val hostQuery = notificationRef.whereEqualTo("type", "HOST").whereEqualTo("hostId", studentId)
            hostQuery.addSnapshotListener { value, error ->
                if (error != null){
                    hostSpecificNotificationsDeferred.completeExceptionally(error)
                    return@addSnapshotListener
                }

                val documents = value?.documents!!
                val hostSpecificNotifications = documents.map {
                    it.toObject(HostNotification::class.java)!!
                }
                hostSpecificNotificationsDeferred.complete(hostSpecificNotifications)
            }
            val hostSpecificNotifications = hostSpecificNotificationsDeferred.await()
            val combinedNotification = broadcastNotifications + hostSpecificNotifications
            val latestNotification = combinedNotification.sortedByDescending { it.timestamp }
            _notifications.postValue(latestNotification)
        }
    }
}