package com.rtweel.activities;

import com.rtweel.R;
import com.rtweel.camera.Photo;
import com.rtweel.constant.Extras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity implements
		OnClickListener {

	private Menu _menu = null;

	private Intent intent;

	private EditText editName;
	private EditText editText;
	private EditText editLocation;
	private ImageView photo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		// getSupportFragmentManager().beginTransaction().add(new
		// MyFirstFragment(),
		// getString(R.string.abc_action_bar_up_description)).commit();
		Log.i("DEBUG", "debugg");
		findViewById(R.id.edit_button).setOnClickListener(this);

		TextView Name = (TextView) findViewById(R.id.detail_name);
		TextView Text = (TextView) findViewById(R.id.detail_text);
		TextView Location = (TextView) findViewById(R.id.detail_location);

		// Intent startIntent = getIntent();
		Bundle start = getIntent().getExtras();

		String name = start.getString(Extras.USER);
		String text = start.getString(Extras.TEXT);
		String location = start.getString(Extras.LOCATION);

		Name.setText(name);
		Text.setText(text);
		Location.setText(location);

		Photo.mCurrentPhotoPath = getIntent().getExtras().getString(
				Extras.IMAGE_PATH);

		photo = (ImageView) findViewById(R.id.photo_view);
		if (Photo.mCurrentPhotoPath != null)
			Photo.setPicture(photo);
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_action, menu);

//		intent = new Intent(DetailActivity.this, MainActivity.class);
//		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

	//	menu.addIntentOptions(R.id.group_intent, 0, 0, this.getComponentName(),
	//			null, intent, 0, null);

		menu.getItem(0).setTitle(R.string.home_button);
		menu.getItem(0).setIcon(null);
		MenuItemCompat.setShowAsAction(menu.getItem(0),
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		_menu = menu;
		return true;
	}
*/
	@Override
	public void onClick(View v) {

		Button button = (Button) v;
		if (button.getText().toString() == getString(R.string.make_photo)) {
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			Photo photo = new Photo();
			photo.setFileUri(Uri.fromFile(Photo.getOutputMediaFile()));
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo.getFileUri());
			startActivityForResult(cameraIntent,
					Photo.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		}

		else if (button.getText().toString() == getString(R.string.edit_button)) {

			setContentView(R.layout.detail_edit);

	//		getMenu().getItem(0).setTitle(R.string.done_button);

			editName = (EditText) findViewById(R.id.edit_name);
			editText = (EditText) findViewById(R.id.edit_text);
			editLocation = (EditText) findViewById(R.id.edit_location);
			photo = (ImageView) findViewById(R.id.edit_photo_view);

			Bundle extras = getIntent().getExtras();

			editName.setText(extras.getString(Extras.USER));
			editText.setText(extras.getString(Extras.TEXT));
			editLocation.setText(extras.getString(Extras.LOCATION));

			Photo.mCurrentPhotoPath = extras.getString(Extras.IMAGE_PATH);

			Button makePhoto = (Button) findViewById(R.id.edit_make_photo);

			makePhoto.setOnClickListener(this);
		}
	}

	private Menu getMenu() {
		return _menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (item.getTitle().toString() == getString(R.string.done_button)) {

				Intent data = getIntent();

				data.putExtra(Extras.USER, editName.getText().toString());
				data.putExtra(Extras.TEXT, editText.getText().toString());
				data.putExtra(Extras.LOCATION, editLocation.getText()
						.toString());
				data.putExtra(Extras.IMAGE_PATH, Photo.mCurrentPhotoPath);

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

}
