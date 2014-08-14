package com.rtweel.activities;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import twitter4j.Status;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.rtweel.R;
import com.rtweel.asynctasks.LoadTimelineTask;
import com.rtweel.asynctasks.TimelineDownTask;
import com.rtweel.asynctasks.TimelineUpTask;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;
import com.rtweel.parsers.DateParser;
import com.rtweel.services.TweetService;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;
import com.rtweel.tweet.TweetAdapter;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterGetAccessTokenTask;
import com.rtweel.twitteroauth.TwitterUtil;

public class MainActivity extends ActionBarActivity { // implements
														// ActionBar.OnNavigationListener
														// {

	private static final int EDIT_REQUEST = 0;
	private static final int ADD_DEL_REQUEST = 1;

	private BaseAdapter adapter;

	@SuppressWarnings("rawtypes")
	private AdapterView mAdapter;

	private Timeline mTimeline;

	private ListView list;
	private ProgressBar mLoadingBar;

	private boolean mContentLoaded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		getSupportActionBar().hide();

		/*
		 * Login Check
		 */
		if (loginCheck()) {
			initialize();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int requestResult,
			Intent intent) {

		if (requestCode == EDIT_REQUEST) {
			if (requestResult == RESULT_OK) {
				// Status tweet = mTimeline.get(intent.getExtras().getInt(
				// Extras.TWEET_POSITION));
				/*
				 * tweet.setText(intent.getExtras().getString(Extras.TEXT));
				 * tweet.setName(intent.getExtras().getString(Extras.USER));
				 * tweet
				 * .setLocation(intent.getExtras().getString(Extras.LOCATION));
				 * tweet
				 * .setImagePath(intent.getExtras().getString(Extras.IMAGE_PATH
				 * ));
				 */
				// for (Status tw : newtweets) {

				/*
				 * if (true) tw.getPosition() ==
				 * intent.getExtras().getInt(Extras.TWEET_POSITION)) {
				 * tw.setText(intent.getExtras().getString(Extras.TEXT ));
				 * tw.setName(intent.getExtras().getString(Extras .USER));
				 * tw.setLocation(intent.getExtras().getString
				 * (Extras.LOCATION));
				 */
				// break;
				// }
				// }
				adapter.notifyDataSetChanged();

				adapter.notifyDataSetInvalidated();
			}
		}
		if (requestCode == ADD_DEL_REQUEST) {
			if (requestResult == RESULT_OK) {
				if (intent.getExtras().getBoolean(Extras.ADD_CHECK) == true) {
					/*
					 * Gson gson = new Gson(); String result = null; try {
					 * InputStream stream =
					 * getResources().getAssets().open("0.json");
					 * InputStreamReader reader = new InputStreamReader(stream);
					 * BufferedReader bReader = new BufferedReader(reader);
					 * StringBuilder builder = new StringBuilder(); String
					 * singleLine = null; while ((singleLine =
					 * bReader.readLine()) != null) {
					 * builder.append(singleLine); } bReader.close(); result =
					 * builder.toString(); Tweet tweet = gson.fromJson(result,
					 * Tweet.class); tweet.setPosition(lastPosition++);
					 * tweet.setText(intent.getExtras().getString(Extras.TEXT));
					 * tweet.setName(intent.getExtras().getString(Extras.USER));
					 * tweet
					 * .setLocation(intent.getExtras().getString(Extras.LOCATION
					 * ));
					 * tweet.setImagePath(intent.getExtras().getString(Extras
					 * .IMAGE_PATH)); tweets.add(tweet);
					 * 
					 * if (SPINNER_STATE != 0) { String mName = null; switch
					 * (SPINNER_STATE) { case 1: mName = "BSUIR [UNIVERSITY]";
					 * break; case 2: mName = "ksisportal"; break; case 3: mName
					 * = "БРСМ БГУИР"; break; }
					 * 
					 * if
					 * (mName.equals(intent.getExtras().getString(Extras.USER)))
					 * { newtweets.add(tweet); } } } catch (IOException e) {
					 * e.printStackTrace(); }
					 */
					adapter.notifyDataSetChanged();

					adapter.notifyDataSetInvalidated();
				}

				else if (intent.getExtras().getBoolean(Extras.ADD_CHECK) == false) {
					// mTimeline.remove(intent.getExtras().getInt(
					// Extras.TWEET_POSITION));
					// lastPosition--;

					// newtweets.clear();
					for (Status tw : mTimeline) {
						try {

							// if (tw.getUser().getName().equals(mName))
							// newtweets.add(tw);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					adapter.notifyDataSetChanged();

					adapter.notifyDataSetInvalidated();
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reload_home_timeline: {

			App app = (App) getApplication();
			if (!app.isOnline()) {
				Log.i("DEBUG", "home timeline button onClick NO NETWORK");
				Toast.makeText(getApplicationContext(),
						"No network connection, couldn't load tweets!",
						Toast.LENGTH_LONG).show();
				return true;
			}
			mTimeline.clear();

			mTimeline.setTimelineType(Timeline.HOME_TIMELINE);
			list.setVisibility(View.GONE);
			crossfade();
			Log.i("DEBUG", "Updating home timeline...");
			// LoadTimelineTask task = new LoadTimelineTask(this);
			// task.execute(mTimeline);
			new LoadTimelineTask(this).execute(mTimeline);
			break;
		}
		case R.id.reload_user_timeline: {
			App app = (App) getApplication();
			if (!app.isOnline()) {
				Log.i("DEBUG", "user timeline button onClick NO NETWORK");
				Toast.makeText(getApplicationContext(),
						"No network connection, couldn't load tweets!",
						Toast.LENGTH_LONG).show();
				return true;
			}
			mTimeline.clear();
			mTimeline.setTimelineType(Timeline.USER_TIMELINE);
			list.setVisibility(View.GONE);
			crossfade();
			Log.i("DEBUG", "Updating user timeline...");

			// TimelineUpTask task = new TimelineUpTask(MainActivity.this);
			// LoadTimelineTask task = new LoadTimelineTask(this);
			// task.execute(mTimeline);
			new LoadTimelineTask(this).execute(mTimeline);
			break;
		}
		case R.id.tweet_send_open: {
			Intent intent = new Intent(MainActivity.this,
					SendTweetActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.logout_button: {
			App app = (App) getApplication();

			boolean dbDeleted = deleteDatabase(TweetDatabaseOpenHelper
					.getDbName());
			Log.i("DEBUG", "DB DELETED = " + dbDeleted);

			app.createDb();

			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
			editor.putString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
			editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,
					false);
			editor.commit();

			TwitterUtil.getInstance().reset();
			finish();
			break;
		}
		}
		// Intent intent = new Intent(MainActivity.this, TweetActivity.class);
		// startActivityForResult(intent, ADD_DEL_REQUEST);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (findViewById(R.id.list) != null) {
			getMenuInflater().inflate(R.menu.main, menu);
		} else {
			getMenuInflater().inflate(R.menu.login, menu);
		}
		return true;
	}

	private void initialize() {

		Log.i("DEBUG", "Initializing...");
		// Log.i("DEBUG", "YES AUTH");

		mTimeline = new Timeline(getApplicationContext());

		Timeline.setDefaultTimeline(mTimeline);
		/*
		 * IntentFilter filter = new IntentFilter(Broadcast.BROADCAST_ACTION);
		 * 
		 * mReceiver = new TweetReceiver();
		 * 
		 * LocalBroadcastManager.getInstance(this).registerReceiver( mReceiver,
		 * filter);
		 */
		// Intent intent = new Intent(this, TweetService.class);
		// intent.putExtra("TEST", "Messageeeee!");

		// startService(intent);
		Intent serviceIntent = new Intent(this, TweetService.class);
		PendingIntent alarmIntent = PendingIntent.getService(this, 0,
				serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager
				.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						SystemClock.elapsedRealtime()
								+ AlarmManager.INTERVAL_HALF_HOUR,
						AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);

		new LoadTimelineTask(this).execute(mTimeline);

		// getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().show();

		list = (ListView) findViewById(R.id.list);

		mLoadingBar = (ProgressBar) findViewById(R.id.loading);

		list.setVisibility(View.GONE);
		crossfade();

		list.setOnTouchListener(new OnTouchListener() {

			float x1, x2;

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN: {
					x1 = event.getX();
					break;
				}
				case MotionEvent.ACTION_UP: {
					x2 = event.getX();

					float distance = x2 - x1;

					if (distance > 150) { // Right swipe
						//	AnimationSet set = new AnimationSet(false);
						//	set.
						rotate();
						App app = (App) getApplication();
						if (!app.isOnline()) {
							Log.i("DEBUG", "Right swipe NO NETWORK");
							Toast.makeText(
									getApplicationContext(),
									"No network connection, couldn't load tweets!",
									Toast.LENGTH_LONG).show();
							return false;
						}
						// list.setVisibility(View.GONE);
						// crossfade();
						Log.i("DEBUG", "SWIPE RIGHT");

						new TimelineUpTask(MainActivity.this)
								.execute(mTimeline);

						// TODO Some scrolling up

						// Scroller scroller = new
						// Scroller(getApplicationContext());
						// scroller.startScroll((int)x2, (int)event.getY(), 0,
						// -800);
					}
					if (distance < -150) { // Left Swipe
						//	AnimationSet set = new AnimationSet(false);
						//	set.
						rotate();
						App app = (App) getApplication();
						if (!app.isOnline()) {
							Log.i("DEBUG", "Left swipe NO NETWORK");
							Toast.makeText(
									getApplicationContext(),
									"No network connection, couldn't load tweets!",
									Toast.LENGTH_LONG).show();
							return false;
						}
						// list.setVisibility(View.GONE);
						// crossfade();
						Log.i("DEBUG", "SWIPE LEFT");

						new TimelineDownTask(MainActivity.this)
								.execute(mTimeline);
						// TODO Some scrolling up
					}
					break;
				}
				}
				return false;
			}
		});

		mAdapter = list;

		adapter = new TweetAdapter(mTimeline, getApplicationContext());//

		mAdapter.setAdapter(adapter);

		mAdapter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(MainActivity.this,
						DetailActivity.class);
				twitter4j.Status tweet = (twitter4j.Status) adapter
						.getItem(position);

				intent.putExtra(Extras.USER_NAME, tweet.getUser().getName());
				intent.putExtra(Extras.TEXT, tweet.getText());
				intent.putExtra(Extras.LOCATION, tweet.getUser().getLocation());
				intent.putExtra(Extras.FAVORITES_COUNT,
						tweet.getFavoriteCount());
				intent.putExtra(Extras.RETWEETS_COUNT, tweet.getRetweetCount());
				intent.putExtra(Extras.PICTURE_URL, tweet.getUser()
						.getProfileImageURL());// getMiniProfileImageURL());
				intent.putExtra(Extras.DATE,
						DateParser.parse(tweet.getCreatedAt().toString()));
				startActivityForResult(intent, EDIT_REQUEST);
			}
		});
	}

	private boolean loginCheck() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (!sharedPreferences.getBoolean(
				ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false)) {
			Toast.makeText(getApplicationContext(), "Logging in...",
					Toast.LENGTH_LONG).show();
			try {
				Uri uri = getIntent().getData();
				if (uri != null
						&& uri.toString().startsWith(
								ConstantValues.TWITTER_CALLBACK_URL)) {
					String verifier = uri
							.getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
					Log.i("DEBUG", "Verification..");
					new TwitterGetAccessTokenTask(getApplicationContext())
							.execute(verifier).get();
					initialize();
				} else {
					Log.i("DEBUG", "Browser authentification...");
					new TwitterAuthenticateTask().execute();
				}
			} catch (Exception e) {
				Log.i("DEBUG", e.toString());
				e.printStackTrace();
			}
			return false;

		} else {
			return true;
		}
	}

	class TwitterAuthenticateTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(TwitterUtil.getInstance().getRequestToken()
							.getAuthenticationURL()));
			startActivity(intent);
			return null;
		}
	}

	public void crossfade() {
		mContentLoaded = !mContentLoaded;

		final View showView = mContentLoaded ? mLoadingBar : list;
		final View hideView = mContentLoaded ? list : mLoadingBar;

			// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		ViewHelper.setAlpha(list, 0f);
		// showView.setAlpha(0f);
		showView.setVisibility(View.VISIBLE);


		int mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		// showView.animate()
		animate(showView).alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);
		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		// hideView.animate()
		animate(hideView).alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						hideView.setVisibility(View.GONE);
					}
				});
	}
	
	private void rotate() {
		//	AnimationSet set = new AnimationSet(false);
		//	set.
			RotateAnimation anim = new RotateAnimation(0, 360, 0, 0);
			anim.startNow();
		//	anim.setRepeatCount(0);
			anim.setDuration(4000);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			list.setAnimation(anim);
	/*		Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					RotateAnimation anima = new RotateAnimation(0, -360, 0, 0);
					anima.startNow();
					anima.setRepeatCount(1);
					anima.setDuration();
					anima.setInterpolator(new AccelerateDecelerateInterpolator());
					list.setAnimation(anima);
				}
			}, 4000);
	*/
	}
	
	public ListView getList() {
		return list;
	}

	public BaseAdapter getAdapter() {
		return adapter;
	}

	public AdapterView getAdapterView() {
		return mAdapter;
	}

	public void setAdapterView(ListView list) {
		mAdapter = list;
	}

	public Timeline getTimeline() {
		return mTimeline;
	}

	@Override
	protected void onDestroy() {
		// unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// IntentFilter filter = new IntentFilter(Broadcast.BROADCAST_ACTION);
		// LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
		// filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// IntentFilter filter = new
		// IntentFilter(TweetReceiver.BROADCAST_ACTION);

		// mReceiver = new TweetReceiver();

		// LocalBroadcastManager.getInstance(this).registerReceiver(
		// mReceiver, filter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// IntentFilter filter = new
		// IntentFilter(TweetReceiver.BROADCAST_ACTION);
		// LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
		// filter);
	}
}
