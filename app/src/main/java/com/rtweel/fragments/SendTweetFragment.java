package com.rtweel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.rtweel.R;
import com.rtweel.asynctasks.tweet.SendTweetTask;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;

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
    public final static String SAVE_TWEET_PROGRESS = "save_tweet_progress";

    private EditText mTweetEntry;
    private TextView mTweetLengthCounter;
    private ImageView mTweetPicture;
    private ImageView mGetPictureButton;
    private ImageView mFileSelectButton;
    private RoundCornerProgressBar mTweetProgress;

    private boolean mIsValidTweetSize = true;

    private Bundle mSavedInstanceState = null;

    private int mCurrentMax = 140;


    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();

        if(args != null) {
            String path = args.getString(Extras.FILE_URI);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            setImage(bitmap);

            mCurrentMax = 117;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String tweetText = prefs.getString(Extras.TWEET_TEXT, "");
            mTweetEntry.setText(tweetText);
        }

        mTweetProgress.setProgressColor(getResources().getColor(R.color.green_progress));
        mTweetProgress.setBackgroundColor(getResources().getColor(R.color.light_gray_progress));

//        mTweetProgress.setHeaderColor(Color.parseColor("#38c0ae"));
        mTweetProgress.setMax(mCurrentMax);
        int charsCount = mTweetEntry.getText().length();
        mTweetProgress.setProgress(charsCount);
        mTweetLengthCounter.setText(charsCount + "/" + mCurrentMax);
//        mTweetProgress.setIconImageDrawable(getResources().getDrawable(R.drawable.rtweel));


        mTweetEntry.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!"".equals(s)) {
                    mTweetLengthCounter.setText(s.length() + "/" + mCurrentMax);
                    mTweetProgress.setProgress(s.length());
                    if (s.length() > mCurrentMax) {
                        mTweetProgress.setProgressColor(Color.RED);
                        mIsValidTweetSize = false;
                    } else {
                        mIsValidTweetSize = true;
                        mTweetProgress.setProgressColor(getResources().getColor(R.color.green_progress));
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
                FileFragment fragment = new FileFragment();
                Bundle args = new Bundle();
                args.putString(Extras.TWEET_TEXT, mTweetEntry.getText().toString());
                fragment.setArguments(args);
                getMainActivity().setMainFragment(fragment);
            }
        });

        if (mSavedInstanceState != null) {
            mTweetEntry.setText(mSavedInstanceState.getString(SAVE_TWEET_ENTRY));
            mTweetLengthCounter.setText(mSavedInstanceState
                    .getString(SAVE_TWEET_ENTRY_COUNTER));
            mTweetProgress.setProgress(mSavedInstanceState.getFloat(SAVE_TWEET_PROGRESS));
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
                    if (!App.isOnline(getActivity())) {
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


                    new SendTweetTask(getActivity())
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

        setTitle(getString(R.string.title_send_tweet));

        mTweetEntry = (EditText) v.findViewById(R.id.tweet_input);
        mTweetLengthCounter = (TextView) v.findViewById(R.id.tweet_input_counter);
        mTweetPicture = (ImageView) v.findViewById(R.id.tweet_photo_imageview);
        mGetPictureButton = (ImageView) v.findViewById(R.id.tweet_add_photo_button);
        mFileSelectButton = (ImageView) v.findViewById(R.id.tweet_send_file_choose_button);
        mTweetProgress = (RoundCornerProgressBar) v.findViewById(R.id.tweet_progress);

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
        outState.putFloat(SAVE_TWEET_PROGRESS, mTweetProgress.getProgress());
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
                mCurrentMax = 117;
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(),
                    "Image capturing failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean(SettingsFragment.SAVE_TWEET_PREFS, true))
            editor.putString(Extras.TWEET_TEXT, mTweetEntry.getText().toString());
        else
            editor.putString(Extras.TWEET_TEXT, "");
        editor.apply();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean(SettingsFragment.SAVE_TWEET_PREFS, true)) {
            String tweetText = prefs.getString(Extras.TWEET_TEXT, "");
            mTweetEntry.setText(tweetText);
        }
    }

    @Override
    public void onDestroy() {
        File file = new File(Environment.getExternalStorageDirectory()
                + App.PHOTO_PATH + ".jpg");
        if(file.exists())
            file.delete();
        super.onDestroy();
    }
}

