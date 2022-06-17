package com.hcmute.ChatAppApplication.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.Util;
import com.hcmute.ChatAppApplication.login.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Util.updateDeviceTokken(this, s);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String tittle = remoteMessage.getData().get(Constants.NOTIFICATION_TITTLE);
        String message = remoteMessage.getData().get(Constants.NOTIFICATION_MESSAGE);

        Log.d("received_message", message);

        Intent intentChat = new Intent(this, LoginActivity.class);
        intentChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentChat, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultNotificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(Constants.CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID);
            //builder.setChannelId(Constants.CHANNEL_ID);

        } else {
            builder = new NotificationCompat.Builder(this);

        }

        builder.setSmallIcon(R.drawable.chat);
        builder.setContentTitle(tittle);
        builder.setColor(getResources().getColor(R.color.theme_color));
        builder.setAutoCancel(true);
        builder.setSound(defaultNotificationSoundUri);
        builder.setContentIntent(pendingIntent);
        builder.setContentText(message);

        notificationManager.notify(300,
                builder.build());


    }
}