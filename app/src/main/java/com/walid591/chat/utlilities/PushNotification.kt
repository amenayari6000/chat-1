package com.walid591.chat.utlilities

import com.walid591.chat.utlilities.NotificationData

//to create notification
data class PushNotification(
    // from class data notification (tittle,message)
    val data: NotificationData,
    // recipient of notification token recipient

   // val to: String, // Receiver's FCM token
    val to: String

    )




//val notificationData = NotificationData("New Message", "Hello, how are you?")
//val recipientToken = "abcdef123456" // Replace with the actual recipient's token
//
//val pushNotification = PushNotification(notificationData, recipientToken)
//In this example, you create a NotificationData object with a title and a message, and then you create a PushNotification object by providing the NotificationData and the recipient's token. The pushNotification object represents the notification that can be sent to the specified recipient identified by the token