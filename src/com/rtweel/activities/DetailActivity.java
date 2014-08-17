package com.rtweel.activities;

import java.util.concurrent.ExecutionException;

import twitter4j.Status;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.asynctasks.FavoriteTask;
import com.rtweel.asynctasks.LogoTask;
import com.rtweel.asynctasks.RetweetTask;
import com.rtweel.cache.App;
import com.rtweel.cache.DiskCache;
import com.rtweel.camera.Photo;
import com.rtweel.constant.Extras;
import com.rtweel.parsers.DateParser;
import com.rtweel.tweet.Timeline;

public class DetailActivity extends ActionBarActivity {

	private ImageView photo;

	private Boolean mIsRetweeted;
	private Boolean mIsFavorited;
	private Long mRetweetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		TextView nameView = (TextView) findViewById(R.id.detail_name);
		TextView textView = (TextView) findViewById(R.id.detail_text);
		TextView locationView = (TextView) findViewById(R.id.detail_location);
		TextView dateView = (TextView) findViewById(R.id.detail_date);
		final Button retweetsCountButton = (Button) findViewById(R.id.detail_retweet_count);
		final Button favsCountButton = (Button) findViewById(R.id.detail_favorited_count);
		ImageView profilePictureView = (ImageView) findViewById(R.id.detail_profile_picture);

		Bundle start = getIntent().getExtras();
		Status tweet = (Status) start.getSerializable(Extras.TWEET);
		final String name = tweet.getUser().getName();
		String text = tweet.getText();
		String location = tweet.getUser().getLocation();
		String date = DateParser.parse(tweet.getCreatedAt().toString());
		int retweetsCount = tweet.getRetweetCount();
		int favsCount = tweet.getFavoriteCount();
		String imageUri = tweet.getUser().getBiggerProfileImageURL();
		final long id = tweet.getId();
		mIsFavorited = tweet.isFavorited();
		mIsRetweeted = tweet.isRetweetedByMe();
		mRetweetId = tweet.getCurrentUserRetweetId();

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

		String cacheName = name.replace(' ', '_') + "_normal";

		App app = (App) getApplication();
		DiskCache cache = app.getDiskCache();
		Bitmap bitmap = cache.getBitmap(cacheName);

		if (bitmap == null) {
			if (!app.isOnline()) {
				Log.i("DEBUG", "picture task tweet adapter NO NETWORK");
				Options opts = new Options();
				opts.outHeight = 24;
				opts.outWidth = 24;
				opts.inScaled = true;

				bitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_launcher, opts);
			} else {
				try {
					bitmap = new LogoTask().execute(imageUri).get();
					cache.put(cacheName, bitmap);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
					Options opts = new Options();
					opts.outHeight = 24;
					opts.outWidth = 24;
					opts.inScaled = true;

					bitmap = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher, opts);
				}
			}
		}

		profilePictureView.setImageBitmap(bitmap);

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
		/*
		 * try { cache.put(name.replace(' ', '_'), task.get()); } catch
		 * (InterruptedException e) { e.printStackTrace(); } catch
		 * (ExecutionException e) { e.printStackTrace(); }
		 */
	}

	/*
	 * @Override public void onClick(View v) {
	 * 
	 * Button button = (Button) v; if (button.getText().toString() ==
	 * getString(R.string.make_photo)) { Intent cameraIntent = new
	 * Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	 * 
	 * Photo photo = new Photo();
	 * photo.setFileUri(Uri.fromFile(Photo.getOutputMediaFile()));
	 * cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo.getFileUri());
	 * startActivityForResult(cameraIntent,
	 * Photo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); }
	 * 
	 * else if (button.getText().toString() == getString(R.string.edit_button))
	 * {
	 * 
	 * setContentView(R.layout.detail_edit);
	 * 
	 * // getMenu().getItem(0).setTitle(R.string.done_button);
	 * 
	 * editName = (EditText) findViewById(R.id.edit_name); editText = (EditText)
	 * findViewById(R.id.edit_text); editLocation = (EditText)
	 * findViewById(R.id.edit_location); photo = (ImageView)
	 * findViewById(R.id.edit_photo_view);
	 * 
	 * Bundle extras = getIntent().getExtras();
	 * 
	 * editName.setText(extras.getString(Extras.USER_NAME));
	 * editText.setText(extras.getString(Extras.TEXT));
	 * editLocation.setText(extras.getString(Extras.LOCATION));
	 * 
	 * Photo.mCurrentPhotoPath = extras.getString(Extras.IMAGE_PATH);
	 * 
	 * Button makePhoto = (Button) findViewById(R.id.edit_make_photo);
	 * 
	 * makePhoto.setOnClickListener(this); } }
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (item.getTitle().toString() == getString(R.string.done_button)) {

				Intent data = getIntent();

				// data.putExtra(Extras.USER_NAME,
				// editName.getText().toString());
				// data.putExtra(Extras.TEXT, editText.getText().toString());
				// data.putExtra(Extras.LOCATION, editLocation.getText()
				// .toString());
				// data.putExtra(Extras.IMAGE_PATH, Photo.mCurrentPhotoPath);

				setResult(RESULT_OK, data);
			} else if (item.getTitle().toString() == getString(R.string.home_button)) {
				setResult(RESULT_CANCELED);
			}

			finish();

			return true;
		default:
			return true;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Photo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Photo.setPicture(photo);
			} else if (resultCode == RESULT_CANCELED) {

			} else {

			}
		}
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

}
