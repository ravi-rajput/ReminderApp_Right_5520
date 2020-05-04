package com.reminder.reminderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import static android.content.Context.NOTIFICATION_SERVICE;

public class NotifierAlarm extends BroadcastReceiver {

    boolean isLockScreen = true;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {



        Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Intent intent1 = new Intent(context,MainPage.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainPage.class);
        taskStackBuilder.addNextIntent(intent1);

        PendingIntent intent2 = taskStackBuilder.getPendingIntent(1,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_channel_01","hello", NotificationManager.IMPORTANCE_HIGH);
        }
        Bitmap image = convert_to_bitmap(intent.getStringExtra("image"));
        NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();
        bigPicture.bigPicture(image);

        Notification notification = builder
                .setContentText(intent.getStringExtra("Message")).setAutoCancel(true)
                .setSound(alarmsound).setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(intent2)
                .setChannelId("my_channel_01")
                .setFullScreenIntent(getFullScreenIntent(isLockScreen,context,
                        intent.getStringExtra("Message"),
                        intent.getStringExtra("image")), true)
                .setStyle(bigPicture)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);

    }

    private PendingIntent getFullScreenIntent(Boolean isLockScreen,Context context,
                                              String message,String image) {
        Intent intent=new Intent(context,LockScreenActivity.class);
        if (isLockScreen)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message",message);
            intent.putExtra("image",image);
            context.startActivity(intent);
        }
        // flags and request code are 0 for the purpose of demonstration
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public Bitmap convert_to_bitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
