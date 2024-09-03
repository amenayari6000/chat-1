package com.walid591.chat.utlilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent


import android.util.Log
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walid591.chat.R
import com.walid591.chat.view.MainActivity


//when user sender send notification it received from  this server as request  to mange the data sending  message and fcm token
// service receive the send notification and data from sender with fcm token receiver and message if app is reinstalled it need a new token receiver
class FirebaseService : FirebaseMessagingService() {

    companion object {

        const val CHANNEL_ID = "CHANNEL_ID"
        var fcmToken: String? = null

    }


    // create specific token in my device when you reinstall  app service need a new token to knew your device
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        Log.d("token", token)
        fcmToken = token


        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
       // sendRegistrationToServer(token)
    }

  

    // ahmed azz explain fcm "https://youtu.be/Vg5NNPuLGRg?si=5wKBlKnWvpHCUjHg"
// recived message(title and body) in this fun
// [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            // Handle incoming FCM messages here from sender
            
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]


            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.d(TAG, "From: ${remoteMessage.from}")
            // You can create and show notifications based on the received message depending on received data
            createNotification(title, body)
        }
    }

    private fun createNotification(title: String?, body: String?) {

        // Create a NotificationChannel before  look: https://www.youtube.com/watch?v=TdH1i4ntxzE&list=PLb6ZzJ93PVwp-1XvfBNrkTlaWeFF4tQx3&index=38
// we need this channel after sdk 0 means sdk 26 oreo
        //  this channel is comppsed by name create and get in string value or default in manifest to check ,channnel_id create companion object,and imaportance  from notifiction manager
        // Register the channel with the system.

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_descriptionText)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        //now given to this description at this channel with apply
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)


        //  after showing notifiction need intent to reach mainactivity when you clic to notification
        // Create an explicit intent for an Activity in your app.
       // an Intent to launch MainActivity when clicked, and displays the notification.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
// Create a pending intent to be triggered when the notification is clicked
     //  The PendingIntent ensures the correct activity is launched even if the app is not currently in the foreground.
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        // after creating channel we use this channel to build notification and show it

        //showNotification

        // Create a NotificationCompat.Builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.round_notifications_none_24)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)


        // Show the notification

        notificationManager.notify(0, builder.build())


    }
}