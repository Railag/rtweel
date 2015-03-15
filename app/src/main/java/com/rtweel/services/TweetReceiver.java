package com.rtweel.services;

import com.rtweel.activities.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.appcompat.R;
import android.util.Log;

public class TweetReceiver extends BroadcastReceiver {

	public static final String BROADCAST_ACTION = "com.rtweel.services.TweetReceiver.BROADCAST_ACTION";
	public static final String CANCEL_NOTIFICATION_ACTION = "com.rtweel.services.TweetReceiver.CANCEL_NOTIFICATION_ACTION";

	public static final int ACTION_BROADCAST = 1;
	public static final int ACTION_CANCEL = 2;
	
	public TweetReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent data) {
		Log.i("DEBUG", "onReceive");
		int action = 0;
		String actionString = data.getAction();
		if (actionString.equals(BROADCAST_ACTION)) {
			action = ACTION_BROADCAST;
		} else if (actionString.equals(CANCEL_NOTIFICATION_ACTION)) {
			action = ACTION_CANCEL;
		}

		switch (action) {
		case ACTION_BROADCAST: {
			String message = data.getStringExtra(TweetService.MESSAGE);
			String title = data.getStringExtra(TweetService.TITLE);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context).setSmallIcon(com.rtweel.R.drawable.rtweel)
					.setContentTitle(title).setContentText(message);
			builder.setAutoCancel(true);
			builder.setSound(RingtoneManager.getActualDefaultRingtoneUri(
					context, RingtoneManager.TYPE_NOTIFICATION));
			Intent resultIntent = new Intent(context, MainActivity.class);

			PendingIntent resultPendingIntent = PendingIntent
					.getActivity(context, 0, resultIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

			builder.setContentIntent(resultPendingIntent);
			builder.setLights(Color.CYAN, 1000, 4000);
			Intent intent = new Intent(CANCEL_NOTIFICATION_ACTION);
			PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0,
					intent, 0);
			builder.setDeleteIntent(deleteIntent);

			int mNotificationId = 1;
			NotificationManager mNotifyMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = builder.build();
			mNotifyMgr.notify(mNotificationId, notification);
			break;
		}
		case ACTION_CANCEL: {
			Log.i("DEBUG", "Action 2");
			TweetService.setNewTweets(0);
			break;
		}
		}
	}

}
