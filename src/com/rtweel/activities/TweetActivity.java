package com.rtweel.activities;

import com.rtweel.R;
import com.rtweel.camera.Photo;
import com.rtweel.constant.Extras;
import com.rtweel.tweet.TweetAdapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TweetActivity extends ActionBarActivity implements OnClickListener {

	private Menu _menu = null;

	private Intent intent;

	private ImageView imagePhoto;

	private BaseAdapter adapter;

	@SuppressWarnings("rawtypes")
	private AdapterView mAdapter;

	private EditText editName;
	private EditText editText;
	private EditText editLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		findViewById(R.id.add_button).setOnClickListener(this);
		findViewById(R.id.delete_button).setOnClickListener(this);
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_action, menu);

		intent = new Intent(TweetActivity.this, MainActivity.class);
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

	//	menu.addIntentOptions(R.id.group_intent, // Menu group to which new
													// items will be added
	//			0, // Unique item ID (none)
	//			0, // Order for the items (none)
	//			this.getComponentName(), // The current activity name
//				null, // Specific items to place first (none)
	//			intent, // Intent created above that describes our requirements
		//		0, // Additional flags to control items (none)
	//			null); // Array of MenuItems that correlate to specific items
						// (none)
		menu.getItem(0).setTitle(R.string.home_button);
		menu.getItem(0).setIcon(null);
		MenuItemCompat.setShowAsAction(menu.getItem(0),
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		_menu = menu;
		return true;
	}
*/
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		Button button = (Button) v;
		if (button.getText().toString() == getString(R.string.add_button)) {
			setContentView(R.layout.detail_edit);
	//		getMenu().getItem(0).setTitle(R.string.add_button);
//			getMenu().add(R.id.group_intent, 1, 200, R.string.home_button);

	//		MenuItemCompat.setShowAsAction(getMenu().getItem(1),
	//				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

			editName = (EditText) findViewById(R.id.edit_name);
			editText = (EditText) findViewById(R.id.edit_text);
			editLocation = (EditText) findViewById(R.id.edit_location);

			imagePhoto = (ImageView) findViewById(R.id.edit_photo_view);

			findViewById(R.id.edit_make_photo).setOnClickListener(this);
		} else if (button.getText().toString() == getString(R.string.make_photo)) {
			Photo photo = new Photo();
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			photo.setFileUri(Uri.fromFile(Photo.getOutputMediaFile()));
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo.getFileUri());
			startActivityForResult(cameraIntent,
					Photo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		}

		else if (button.getText().toString() == getString(R.string.delete_button)) {
			setContentView(R.layout.tweet_delete);
	//		getMenu().getItem(0).setTitle(R.string.home_button);

			// adapter = new TweetAdapter(MainActivity.tweets,
			// getBaseContext());

			ListView list = (ListView) findViewById(R.id.list_delete);

			mAdapter = list;

			mAdapter.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Intent intent = new Intent(TweetActivity.this,
							MainActivity.class);

					intent.putExtra(Extras.TWEET_POSITION, position);
					intent.putExtra(Extras.ADD_CHECK, false);

					setResult(RESULT_OK, intent);
					finish();
				}
			});
			mAdapter.setAdapter(adapter);

		}
	}

//	private Menu getMenu() {
//		return _menu;
//	}

/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (item.getTitle().toString() == getString(R.string.home_button)) {
				setResult(RESULT_CANCELED);
			} else if (item.getTitle().toString() == getString(R.string.add_button)) {
				Intent data = getIntent();

				data.putExtra(Extras.ADD_CHECK, true);
				data.putExtra(Extras.USER, editName.getText().toString());
				data.putExtra(Extras.TEXT, editText.getText().toString());
				data.putExtra(Extras.LOCATION, editLocation.getText()
						.toString());
				data.putExtra(Extras.IMAGE_PATH, Photo.mCurrentPhotoPath);
				// data.putExtra(MainActivity.EXTRA_TWEET_POSITION,
				// tweets.size()+1);
				setResult(RESULT_OK, data);
			}

			finish();

			return true;
		case 1:
			setResult(RESULT_CANCELED);
			finish();
		default:
			return true;

		}

	}
*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Photo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Photo.setPicture(imagePhoto);
			} else if (resultCode == RESULT_CANCELED) {

			} else {

			}
		}
	}

}