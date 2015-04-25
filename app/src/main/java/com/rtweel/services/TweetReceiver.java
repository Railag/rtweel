package com.rtweel.services;

import com.rtweel.MainActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TweetReceiver extends BroadcastReceiver {

    public static final String BROADCAST_ACTION = "com.rtweel.services.TweetReceiver.BROADCAST_ACTION";
    public static final String CANCEL_NOTIFICATION_ACTION = "com.rtweel.services.TweetReceiver.CANCEL_NOTIFICATION_ACTION";
    public static final String BOOT_WAKEUP_ACTION = "android.intent.action.BOOT_COMPLETED";

    public TweetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent data) {
        Log.i("DEBUG", "onReceive");
        String actionString = data.getAction();

        switch (actionString) {

            case BROADCAST_ACTION:
                String message = data.getStringExtra(TweetService.MESSAGE);
                String title = data.getStringExtra(TweetService.TITLE);


                Intent resultIntent = new Intent(context, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent
                        .getActivity(context, 0, resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);


                Intent intent = new Intent(CANCEL_NOTIFICATION_ACTION);
                PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0,
                        intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context).setSmallIcon(com.rtweel.R.drawable.rtweel)
                        .setContentTitle(title).setContentText(message)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(resultPendingIntent)
                        .setLights(Color.CYAN, 1000, 4000)
                        .setDeleteIntent(deleteIntent);


                int mNotificationId = 1;
                NotificationManager mNotifyMgr = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                Notification notification = builder.build();
                mNotifyMgr.notify(mNotificationId, notification);
                break;

            case CANCEL_NOTIFICATION_ACTION:
                Log.i("DEBUG", "Action 2");
                TweetService.setNewTweets(0);
                break;

            case BOOT_WAKEUP_ACTION:
                Intent serviceIntent = new Intent(context, TweetService.class);
                PendingIntent alarmIntent = PendingIntent.getService(context, 0,
                        serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager
                        .setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime()
                                        + AlarmManager.INTERVAL_HALF_HOUR,
                                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);

        }

    }

}
