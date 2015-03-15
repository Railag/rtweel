package com.rtweel.activities;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;

import twitter4j.MediaEntity;
import twitter4j.Status;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.asynctasks.tweet.DeleteTweetTask;
import com.rtweel.asynctasks.tweet.FavoriteTask;
import com.rtweel.asynctasks.tweet.LogoTask;
import com.rtweel.asynctasks.tweet.RetweetTask;
import com.rtweel.cache.App;
import com.rtweel.cache.DiskCache;
import com.rtweel.constant.Extras;
import com.rtweel.parsers.DateParser;
import com.rtweel.tweet.Timeline;
import com.squareup.picasso.Picasso;

public class DetailActivity extends ActionBarActivity {

	private Boolean mIsRetweeted;
	private Boolean mIsFavorited;
	private Long mRetweetId;

	private Status mTweet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		TextView nameView = (TextView) findViewById(R.id.detail_name);
		TextView textView = (TextView) findViewById(R.id.detail_text);
		TextView locationView = (TextView) findViewById(R.id.detail_location);
		TextView dateView = (TextView) findViewById(R.id.detail_date);
		final Button retweetsCountButton = (Button) findViewById(R.id.detail_retweet_count);
		final Button favsCountButton = (Button) findViewById(R.id.detail_favorited_count);
		final Button deleteButton = (Button) findViewById(R.id.detail_delete);
		ImageView profilePictureView = (ImageView) findViewById(R.id.detail_profile_picture);

		Bundle start = getIntent().getExtras();
		mTweet = (Status) start.getSerializable(Extras.TWEET);
		final String name = mTweet.getUser().getName();
		String text = mTweet.getText();
		String location = mTweet.getUser().getLocation();
		String date = DateParser.parse(mTweet.getCreatedAt().toString());
		int retweetsCount = mTweet.getRetweetCount();
		int favsCount = mTweet.getFavoriteCount();
		String imageUri = mTweet.getUser().getBiggerProfileImageURL();
		final long id = mTweet.getId();
		mIsFavorited = mTweet.isFavorited();
		mIsRetweeted = mTweet.isRetweetedByMe();
		mRetweetId = mTweet.getCurrentUserRetweetId();
		MediaEntity[] entities = mTweet.getMediaEntities();
		String[] urls = new String[entities.length];
		ImageView[] views = new ImageView[entities.length];
		/*
		 * if (entities != null) { for (int i = 0; i < entities.length; i++) {
		 * url[i] = entities[i].getMediaURL();
		 * 
		 * Log.i("DEBUG", "Dp Url " + entities[i].getDisplayURL() + " URL " +
		 * entities[i].getURL() + " start " + entities[i].getStart() + " end " +
		 * entities[i].getEnd()); } }
		 */
		// AttributeSet attrs = new Attributes();
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.detail_layout);
		if (entities.length > 0) {
			Log.i("DEBUG", "Entities length: " + entities.length);
			String cacheName = "entity_" + mTweet.getId();
			for (int i = 0; i < entities.length; i++) {
				urls[i] = entities[i].getMediaURL();
				Log.i("DEBUG", urls[i]);
				// entities[i].getSizes();
				views[i] = new ImageView(this);
//				Bitmap bitmap = null;
//				bitmap = ((App) getApplication()).getDiskCache().getBitmap(
//						cacheName);
//				if (bitmap == null) {
//					try {
//						bitmap = new LogoTask().execute(urls[i]).get();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					} catch (ExecutionException e) {
//						e.printStackTrace();
//					}
//				}
                Picasso.with(getApplicationContext()).load(urls[0]).into(views[i]);
                //views[i].setImageBitmap(bitmap);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);

				p.addRule(RelativeLayout.BELOW, R.id.detail_location);

				views[i].setLayoutParams(p);
				relativeLayout.addView(views[i]);
				// views[i].setLayoutParams(new LayoutParams(300, 300));
			}
			// LayoutParams params = vh.getMediaView().getLayoutParams();
			// params.height = 175;
			// params.width = 250;
			// vh.getMediaView().setLayoutParams(params);
			// vh.loadBitmapMedia(url, vh.getMediaView(), cacheName);

		}

		final int position = start.getInt(Extras.POSITION);

		if (mIsRetweeted) {
			retweetsCountButton.setBackgroundColor(Color.GREEN);
		} else {
			retweetsCountButton.setBackgroundColor(Color.DKGRAY);
		}

		if (mIsFavorited) {
			favsCountButton.setBackgroundColor(Color.GREEN);
		} else {
			favsCountButton.setBackgroundColor(Color.DKGRAY);
		}

//		String cacheName = name.replace(' ', '_') + "_normal";
//
//		App app = (App) getApplication();
//		DiskCache cache = app.getDiskCache();
//		Bitmap bitmap = cache.getBitmap(cacheName);
//
//		if (bitmap == null) {
//			if (!app.isOnline()) {
//				Log.i("DEBUG", "picture task tweet adapter NO NETWORK");
//				Options opts = new Options();
//				opts.outHeight = 24;
//				opts.outWidth = 24;
//				opts.inScaled = true;
//
//				bitmap = BitmapFactory.decodeResource(getResources(),
//						R.drawable.rtweel, opts);
//			} else {
//				try {
//					bitmap = new LogoTask().execute(imageUri).get();// url).get();
//					cache.put(cacheName, bitmap);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} catch (ExecutionException e) {
//					e.printStackTrace();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//					Options opts = new Options();
//					opts.outHeight = 24;
//					opts.outWidth = 24;
//					opts.inScaled = true;
//
//					bitmap = BitmapFactory.decodeResource(getResources(),
//							R.drawable.rtweel, opts);
//				}
//			}
//		}
//
//		profilePictureView.setImageBitmap(bitmap);

        Picasso.with(getApplicationContext()).load(imageUri).into(profilePictureView);

		/*
		 * FileOutputStream stream = null; File file = new
		 * File(getExternalCacheDir() + " tmp.jpg"); try { stream = new
		 * FileOutputStream(file); } catch (FileNotFoundException e) {
		 * e.printStackTrace(); } bitmap.compress(Bitmap.CompressFormat.JPEG,
		 * 100, stream); try { stream.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */
		nameView.setText(name);
		textView.setText(text);
		locationView.setText(location);
		dateView.setText(date);
		retweetsCountButton.setText(String.valueOf(retweetsCount));
		retweetsCountButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!name.equals(Timeline.getUserName())) {
					new RetweetTask(DetailActivity.this, retweetsCountButton,
							mIsRetweeted).execute(id, mRetweetId);
				} else {
					Toast.makeText(getApplicationContext(),
							"You can't retweet your own tweet",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		favsCountButton.setText(String.valueOf(favsCount));
		favsCountButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new FavoriteTask(DetailActivity.this, favsCountButton,
						mIsFavorited).execute(id);
			}
		});

		if (name.equals(Timeline.getUserName())) {
			deleteButton.setText(getString(R.string.delete_button));
			deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new DeleteTweetTask(DetailActivity.this,
							DeleteTweetTask.DETAIL, position).execute(id);
				}
			});
		} else {
			deleteButton.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.detail_tweet_share: {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);// _MULTIPLE);
			shareIntent.setType(HTTP.PLAIN_TEXT_TYPE);
			// shareIntent.setType("image/*");
			// shareIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
			// shareIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
			// ArrayList<CharSequence> text = new ArrayList<CharSequence>();
			// text.add(mTweet.getText());
			shareIntent
					.putExtra(Intent.EXTRA_TITLE, mTweet.getUser().getName());
			// shareIntent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT,
			// text);
			shareIntent.putExtra(Intent.EXTRA_TEXT, mTweet.getText());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTweet.getUser()
					.getName() + "'s tweet");
			File file = new File(getExternalCacheDir() + " tmp.jpg");
			// Log.i("DEBUG", getExternalCacheDir() + " tmp.jpg");
			// ArrayList<Uri> imageUris = new ArrayList<Uri>();
			// imageUris.add(Uri.fromFile(file));
			// shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
			// imageUris);// Uri.fromFile(file));//("content://" +
			// getExternalCacheDir() + "tmp.jpg"));
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			String title = "Choose an app to share the tweet";
			Intent chooser = Intent.createChooser(shareIntent, title);
			startActivity(chooser);
			break;
		}
		default:
		}
		return true;
	}

	public void changeIsRetweeted() {
		mIsRetweeted = !mIsRetweeted;
	}

	public void changeIsFavorited() {
		mIsFavorited = !mIsFavorited;
	}

	public void setRetweetId(Long id) {
		mRetweetId = id;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
