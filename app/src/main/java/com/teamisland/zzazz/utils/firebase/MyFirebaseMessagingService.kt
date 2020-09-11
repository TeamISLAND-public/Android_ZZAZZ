package com.teamisland.zzazz.utils.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teamisland.zzazz.R
import com.teamisland.zzazz.ui.SplashActivity


// TODO Get google service json file which is Firebase Android configuration file from firebase.

/**
 * Class for getting message from firebase.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * When get new token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("New asdfasdf", token)
    }

    /**
     * When receive a message from firebase, send to activity.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(
                (remoteMessage.notification ?: return).title ?: return,
                (remoteMessage.notification ?: return).body ?: return
            )
        }
    }

    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, SplashActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_zzazz)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channelName = getString(R.string.default_notification_channel_name)
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }
}