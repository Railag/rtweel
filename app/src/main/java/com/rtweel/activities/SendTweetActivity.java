package com.rtweel.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.asynctasks.tweet.TwitterSendTweetTask;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;
import com.rtweel.filechooser.FileActivity;

public class SendTweetActivity extends ActionBarActivity {

	public final static String SAVE_TWEET_ENTRY = "save_tweet_entry";
	public final static String SAVE_TWEET_ENTRY_COUNTER = "save_tweet_entry_counter";

	public final static int PHOTO_REQUEST_CODE = 1;
	public final static int REQUEST_FILE_SELECT = 2;

	private EditText mTweetEntry;
	private TextView mTweetLengthCounter;
	private ImageView mTweetPicture;
	private Button mGetPictureButton;
	private Button mFileSelectButton;

	private boolean mIsValidTweetSize = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_send);

		mTweetEntry = (EditText) findViewById(R.id.tweet_input);
		mTweetLengthCounter = (TextView) findViewById(R.id.tweet_input_counter);
		mTweetLengthCounter.setBackgroundColor(Color.GREEN);
		mTweetLengthCounter.setText("0/140");

		mTweetPicture = (ImageView) findViewById(R.id.tweet_photo_imageview);

		mTweetEntry.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!"".equals(s)) {
					mTweetLengthCounter.setText(s.length() + "/140");
					if (s.length() > 140) {
						mTweetLengthCounter.setBackgroundColor(Color.RED);
						mIsValidTweetSize = false;
					} else {
						mTweetLengthCounter.setBackgroundColor(Color.GREEN);
						mIsValidTweetSize = true;
					}
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mGetPictureButton = (Button) findViewById(R.id.tweet_add_photo_button);
		mGetPictureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				String uri = Environment.getExternalStorageDirectory()
						+ App.PHOTO_PATH + ".jpg";
				File file = new File(uri);
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivityForResult(intent, PHOTO_REQUEST_CODE);
				}
			}
		});

		mFileSelectButton = (Button) findViewById(R.id.tweet_send_file_choose_button);
		mFileSelectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SendTweetActivity.this,
						FileActivity.class);
				startActivityForResult(intent, REQUEST_FILE_SELECT);
			}
		});

		if (savedInstanceState != null) {
			mTweetEntry.setText(savedInstanceState.getString(SAVE_TWEET_ENTRY));
			mTweetLengthCounter.setText(savedInstanceState
					.getString(SAVE_TWEET_ENTRY_COUNTER));
		}
		if (getIntent().getAction() != null) {
			if (getIntent().getAction().equals(Intent.ACTION_SEND)) {
				Intent data = getIntent();
				if (data.hasExtra(Intent.EXTRA_TEXT)) {
					mTweetEntry.setText(data.getExtras().getString(
							Intent.EXTRA_TEXT));
				} else if (data.hasExtra(Intent.EXTRA_STREAM)) {
					// String uri =
					// data.getExtras().getString(Intent.EXTRA_STREAM);
					Uri uri = (Uri) getIntent().getExtras().getParcelable(
							Intent.EXTRA_STREAM);
					Cursor cursor = null;
					String path = null;
					try {
						String[] projection = { MediaStore.Images.Media.DATA };
						cursor = getContentResolver().query(uri, projection,
								null, null, null);
						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						cursor.moveToFirst();
						path = cursor.getString(column_index);
					} finally {
						if (cursor != null) {
							cursor.close();
						}
					}
					Bitmap bitmap = BitmapFactory.decodeFile(path);
					mTweetPicture.setImageBitmap(bitmap);
					FileOutputStream stream = null;
					File file = new File(
							Environment.getExternalStorageDirectory()
									+ App.PHOTO_PATH + ".jpg");
					try {
						stream = new FileOutputStream(file);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
			if (mIsValidTweetSize) {
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

				new TwitterSendTweetTask(getApplicationContext())
						.execute(tweet);
				mTweetEntry.setText("");
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Too long tweet, must be less than 140 symbols",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				if (getIntent().getAction().equals(Intent.ACTION_SEND)) {
					finish();
				}
			}
			break;
		}
		case R.id.tweet_send_back: {
			finish();
			break;
		}
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVE_TWEET_ENTRY, mTweetEntry.getText().toString());
		outState.putString(SAVE_TWEET_ENTRY_COUNTER, mTweetLengthCounter
				.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bitmap bitmap = BitmapFactory.decodeFile(Environment
						.getExternalStorageDirectory()
						+ App.PHOTO_PATH
						+ ".jpg");
				mTweetPicture.setImageBitmap(bitmap);

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(),
						"Image capturing failed", Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == REQUEST_FILE_SELECT) {
			if (resultCode == RESULT_OK) {
				Uri file = Uri.fromFile(new File(data
						.getStringExtra(Extras.FILE_URI)));
				Log.i("DEBUG", file.toString());
				Bitmap bitmap = BitmapFactory.decodeFile(data
						.getStringExtra(Extras.FILE_URI));
				Log.i("DEBUG", data.getStringExtra(Extras.FILE_URI));
				mTweetPicture.setImageBitmap(bitmap);
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "File choosing failed",
						Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
