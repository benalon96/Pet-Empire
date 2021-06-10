package com.example.socialnetwork.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.socialnetwork.ChatActivity;
import com.example.socialnetwork.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    final String TAG = "MyFirebaseMessaging";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Intent intent = new Intent("message_received");
            intent.putExtra("message",remoteMessage.getData().get("message"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            //if the application is not in forground post notification
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this);

            if(Build.VERSION.SDK_INT>=26) {
                NotificationChannel channel = new NotificationChannel("id_1", "name_1", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
                builder.setChannelId("id_1");
            }
            builder.setContentTitle( "You have new message from: "+remoteMessage.getData().get("name")).setContentText(remoteMessage.getData().get("message")).setSmallIcon(R.drawable.animal);
            if(!remoteMessage.getData().get("sender").equals(ChatActivity.id))
                manager.notify(1,builder.build());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("TOKEN", token);
    }


}
