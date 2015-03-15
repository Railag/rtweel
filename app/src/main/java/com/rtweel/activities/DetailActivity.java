package com.rtweel.activities;

import java.io.File;

import org.apache.http.protocol.HTTP;

import twitter4j.MediaEntity;
import twitter4j.Status;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import com.rtweel.asynctasks.tweet.RetweetTask;
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
		TextView dateView = (TextView) findViewById(R.id.detail_date);
		final ImageView retweetsButton = (ImageView) findViewById(R.id.detail_retweet_button);
		final ImageView favsButton = (ImageView) findViewById(R.id.detail_favorited_button);
		ImageView deleteButton = (ImageView) findViewById(R.id.detail_delete);
        final TextView retweetsCountView = (TextView) findViewById(R.id.detail_retweet_count);
        final TextView favsCountView = (TextView) findViewById(R.id.detail_favorited_count);
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

		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.detail_layout);
		if (entities.length > 0) {
			Log.i("DEBUG", "Entities length: " + entities.length);
			String cacheName = "entity_" + mTweet.getId();
			for (int i = 0; i < entities.length; i++) {
				urls[i] = entities[i].getMediaURL();


				views[i] = new ImageView(this);

                Picasso.with(getApplicationContext()).load(urls[0]).into(views[i]);
                //views[i].setImageBitmap(bitmap);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);

				p.addRule(RelativeLayout.BELOW, R.id.detail_retweet_count);

				views[i].setLayoutParams(p);
				relativeLayout.addView(views[i]);
			}

		}

		final int position = start.getInt(Extras.POSITION);

		if (mIsRetweeted) {
            retweetsButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
        }
        
		if (mIsFavorited) {
            favsButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
        }

        Picasso.with(getApplicationContext()).load(imageUri).into(profilePictureView);

		nameView.setText(name);
		textView.setText(text);
		dateView.setText(date);
		retweetsCountView.setText(String.valueOf(retweetsCount));
		retweetsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!name.equals(Timeline.getUserName())) {
					new RetweetTask(DetailActivity.this, retweetsButton, retweetsCountView,
							mIsRetweeted).execute(id, mRetweetId);
				} else {
					Toast.makeText(getApplicationContext(),
							"You can't retweet your own tweet",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		favsCountView.setText(String.valueOf(favsCount));
		favsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new FavoriteTask(DetailActivity.this, favsButton, favsCountView,
						mIsFavorited).execute(id);
			}
		});

		if (name.equals(Timeline.getUserName())) {
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
