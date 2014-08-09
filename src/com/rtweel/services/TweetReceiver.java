package com.rtweel.services;

import com.rtweel.activities.MainActivity;
import com.rtweel.constant.Broadcast;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.appcompat.R;
import android.util.Log;
import android.widget.Toast;

public class TweetReceiver extends BroadcastReceiver {

	public TweetReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent data) {
		Log.i("DEBUG", "onReceive");
		String message = data.getStringExtra(Broadcast.MESSAGE);
		String title = data.getStringExtra(Broadcast.TITLE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.abc_ab_solid_dark_holo)
				// notification_icon)
				.setContentTitle(title)
				.setContentText(message);
		mBuilder.setAutoCancel(true);
		mBuilder.setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION));
		Intent resultIntent = new Intent(context, MainActivity.class);
		// Intent intent = new Intent();
		// intent.setClass(mActivity, MainActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);

		int mNotificationId = 1;

		NotificationManager mNotifyMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
	//	((MainActivity)context).getAdapter().notifyDataSetChanged();
	}

}
