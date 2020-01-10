package com.pca.gomusic.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pca.gomusic.BuildConfig;
import com.pca.gomusic.R;


public class MyMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
              //  scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }if (remoteMessage.getNotification() != null) {
        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification();
    }

    @Override

    public void onNewToken(String token) {
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]



    private void handleNow() {

    }


    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

    }


    private void sendNotification() {
        Uri uri = Uri.parse("market://details?id=" +  BuildConfig.APPLICATION_ID);
        Intent myAppLinkToMarket = new  Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , myAppLinkToMarket,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText("We've made some changes in the app and think it's cool for you to check out the updates")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "APP UPDATE",
                    NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (notificationManager != null) {
            notificationManager.notify(0 , notificationBuilder.build());
        }

    }
}
