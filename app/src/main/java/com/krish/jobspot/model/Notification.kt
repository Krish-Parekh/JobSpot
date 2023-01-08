package com.krish.jobspot.model

import com.google.firebase.Timestamp
import java.util.UUID

//data class BroadcastNotification(
//    val id: String = UUID.randomUUID().toString(),
//    val title: String = "",
//    val body: String = "",
//    val timestamp: Timestamp = Timestamp.now(),
//    val type : String = "BROADCAST"
//)
//
//data class HostNotification(
//    val id: String = UUID.randomUUID().toString(),
//    val title: String = "",
//    val body: String = "",
//    val hostId: String = "",
//    val timestamp: Timestamp = Timestamp.now(),
//    val type : String = "HOST"
//)

interface Notification {
    val id: String
    val title: String
    val body: String
    val timestamp: Timestamp
    val type: String
}

data class BroadcastNotification(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String = "",
    override val body: String = "",
    override val timestamp: Timestamp = Timestamp.now(),
    override val type : String = "BROADCAST"
) : Notification

data class HostNotification(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String = "",
    override val body: String = "",
    val hostId: String = "",
    override val timestamp: Timestamp = Timestamp.now(),
    override val type : String = "HOST"
) : Notification


