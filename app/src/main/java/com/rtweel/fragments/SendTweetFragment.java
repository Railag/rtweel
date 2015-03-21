package com.rtweel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.activities.MainActivity;
import com.rtweel.asynctasks.tweet.TwitterSendTweetTask;
import com.rtweel.cache.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by root on 22.3.15.
 */
public class SendTweetFragment extends BaseFragment {

    public final static int PHOTO_REQUEST_CODE = 1;

    public final static String SAVE_TWEET_ENTRY = "save_tweet_entry";
    public final static String SAVE_TWEET_ENTRY_COUNTER = "save_tweet_entry_counter";

    private EditText mTweetEntry;
    private TextView mTweetLengthCounter;
    private ImageView mTweetPicture;
    private Button mGetPictureButton;
    private Button mFileSelectButton;

    private boolean mIsValidTweetSize = true;

    private Bundle mSavedInstanceState = null;


    @Override
    public void onStart() {
        super.onStart();

        mTweetLengthCounter.setBackgroundColor(Color.GREEN);
        mTweetLengthCounter.setText("0/140");


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


        mGetPictureButton.setOnClickListener(new View.OnClickListener() {

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
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, PHOTO_REQUEST_CODE);
                }
            }
        });

        mFileSelectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getMainActivity().setMainFragment(new FileFragment());
            }
        });

        if (mSavedInstanceState != null) {
            mTweetEntry.setText(mSavedInstanceState.getString(SAVE_TWEET_ENTRY));
            mTweetLengthCounter.setText(mSavedInstanceState
                    .getString(SAVE_TWEET_ENTRY_COUNTER));
        }
        if (getActivity().getIntent().getAction() != null) {
            if (getActivity().getIntent().getAction().equals(Intent.ACTION_SEND)) {
                Intent data = getActivity().getIntent();
                if (data.hasExtra(Intent.EXTRA_TEXT)) {
                    mTweetEntry.setText(data.getExtras().getString(
                            Intent.EXTRA_TEXT));
                } else if (data.hasExtra(Intent.EXTRA_STREAM)) {
                    // String uri =
                    // data.getExtras().getString(Intent.EXTRA_STREAM);
                    Uri uri = (Uri) getActivity().getIntent().getExtras().getParcelable(
                            Intent.EXTRA_STREAM);
                    Cursor cursor = null;
                    String path = null;
                    try {
                        String[] projection = {MediaStore.Images.Media.DATA};
                        cursor = getActivity().getContentResolver().query(uri, projection,
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_send_tweet, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.tweet_send_ok: {
                if (mIsValidTweetSize) {
                    App app = (App) getActivity().getApplication();
                    if (!app.isOnline()) {
                        Log.i("DEBUG",
                                "sendtweetactivity send tweet button onClick NO NETWORK");
                        Toast.makeText(getActivity(),
                                "No network connection, couldn't load tweets!",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                    String tweet = mTweetEntry.getText().toString();
                    if (TextUtils.isEmpty(tweet)) {
                        Toast toast = Toast.makeText(getActivity(),
                                "Tweet must contain some symbols, can't be empty",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return false;
                    }


                    new TwitterSendTweetTask(getActivity())
                            .execute(tweet);
                    mTweetEntry.setText("");
                } else {
                    Toast toast = Toast.makeText(getActivity(),
                            "Too long tweet, must be less than 140 symbols",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    if (getActivity().getIntent().getAction().equals(Intent.ACTION_SEND)) {
                        back();
                    }
                }
                break;
            }
            case R.id.tweet_send_back: {
                back();
                break;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweet_send, null);

        setHasOptionsMenu(true);

        mSavedInstanceState = savedInstanceState;

        mTweetEntry = (EditText) v.findViewById(R.id.tweet_input);
        mTweetLengthCounter = (TextView) v.findViewById(R.id.tweet_input_counter);
        mTweetPicture = (ImageView) v.findViewById(R.id.tweet_photo_imageview);
        mGetPictureButton = (Button) v.findViewById(R.id.tweet_add_photo_button);
        mFileSelectButton = (Button) v.findViewById(R.id.tweet_send_file_choose_button);

        return v;


    }

    public void setImage(Bitmap bitmap) {
        if (bitmap != null)
            mTweetPicture.setImageBitmap(bitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_TWEET_ENTRY, mTweetEntry.getText().toString());
        outState.putString(SAVE_TWEET_ENTRY_COUNTER, mTweetLengthCounter
                .getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(Environment
                        .getExternalStorageDirectory()
                        + App.PHOTO_PATH
                        + ".jpg");
                setImage(bitmap);
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(),
                    "Image capturing failed", Toast.LENGTH_LONG).show();
        }
    }

}
