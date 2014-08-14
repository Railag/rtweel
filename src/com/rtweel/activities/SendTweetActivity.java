package com.rtweel.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.asynctasks.TwitterSendTweetTask;
import com.rtweel.cache.App;

public class SendTweetActivity extends ActionBarActivity {

	private EditText mTweetEntry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_send);

		mTweetEntry = (EditText) findViewById(R.id.tweet_input);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_send_tweet, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.tweet_send_ok: {
			App app = (App) getApplication();
			if (!app.isOnline()) {
				Log.i("DEBUG",
						"sendtweetactivity send tweet button onClick NO NETWORK");
				Toast.makeText(getApplicationContext(),
						"No network connection, couldn't load tweets!",
						Toast.LENGTH_LONG).show();
				return false;
			}
			String tweet = mTweetEntry.getText().toString();
	//		TwitterSendTweetTask task = new TwitterSendTweetTask(
	//				getApplicationContext());
	//		task.execute(tweet);
			new TwitterSendTweetTask(getApplicationContext()).execute(tweet);
			mTweetEntry.setText("");
			break;
		}
		case R.id.tweet_send_back: {
			finish();
			break;
		}
		}
		return true;
	}
}
