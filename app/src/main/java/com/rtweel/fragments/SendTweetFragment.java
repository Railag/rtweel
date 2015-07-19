package com.rtweel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.storage.App;
import com.rtweel.tasks.tweet.SendTweetTask;
import com.rtweel.utils.Photo;

import java.io.File;
import java.io.IOException;

/**
 * Created by firrael on 22.3.15.
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

    private long mReplyId = -1L;

    private String mPhotoPath;


    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(Const.REPLY_ID))
                mReplyId = args.getLong(Const.REPLY_ID);
            if (args.containsKey(Const.FILE_URI)) {
                mPhotoPath = args.getString(Const.FILE_URI);
                updateImage();

                mCurrentMax = 117;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String tweetText = prefs.getString(Const.TWEET_TEXT, "");
                mTweetEntry.setText(tweetText);
            }
        }

        mTweetProgress.setProgressColor(getResources().getColor(R.color.green_progress));
        mTweetProgress.setBackgroundColor(getResources().getColor(R.color.light_gray_progress));

        mTweetProgress.setMax(mCurrentMax);
        int charsCount = mTweetEntry.getText().length();
        mTweetProgress.setProgress(charsCount);
        mTweetLengthCounter.setText(charsCount + "/" + mCurrentMax);

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

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    mPhotoPath = getActivity().getExternalCacheDir()
                            + Const.PHOTO_PATH;
                else
                    mPhotoPath = getActivity().getCacheDir() + Const.PHOTO_PATH;

                File file = new File(mPhotoPath);

                boolean isCreated = false;

                try {
                    isCreated = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (isCreated && intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, PHOTO_REQUEST_CODE);
                } else
                    Toast.makeText(getActivity(), getString(R.string.send_tweet_image_capture_no_camera), Toast.LENGTH_LONG).show();
            }
        });

        mFileSelectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FileFragment fragment = new FileFragment();
                Bundle args = new Bundle();
                args.putString(Const.TWEET_TEXT, mTweetEntry.getText().toString());
                args.putLong(Const.REPLY_ID, mReplyId);
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
                    Uri uri = getActivity().getIntent().getExtras().getParcelable(
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
                    //Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Photo.setPicture(path, mTweetPicture);
                    //mTweetPicture.setImageBitmap(bitmap);
                    mPhotoPath = path;
                }
            }
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_send_tweet);
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
                        Toast.makeText(getActivity(),
                                getString(R.string.tweet_send_network_problems),
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                    String tweet = mTweetEntry.getText().toString();
                    if (TextUtils.isEmpty(tweet)) {
                        Toast toast = Toast.makeText(getActivity(),
                                getString(R.string.tweet_send_empty),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return false;
                    }


                    new SendTweetTask(getActivity(), mReplyId)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tweet, mPhotoPath);
                    mTweetEntry.setText("");
                } else {
                    Toast toast = Toast.makeText(getActivity(),
                            getString(R.string.tweet_send_too_long),
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
        setRetainInstance(true);

        mSavedInstanceState = savedInstanceState;

        mTweetEntry = (EditText) v.findViewById(R.id.tweet_input);
        mTweetLengthCounter = (TextView) v.findViewById(R.id.tweet_input_counter);
        mTweetPicture = (ImageView) v.findViewById(R.id.tweet_photo_imageview);
        mGetPictureButton = (ImageView) v.findViewById(R.id.tweet_add_photo_button);
        mFileSelectButton = (ImageView) v.findViewById(R.id.tweet_send_file_choose_button);
        mTweetProgress = (RoundCornerProgressBar) v.findViewById(R.id.tweet_progress);

        return v;


    }

    public void updateImage() {
        Photo.setPicture(mPhotoPath, mTweetPicture);
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
                if (mPhotoPath != null) {
                    updateImage();
                    mCurrentMax = 117;
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(),
                    getString(R.string.tweet_send_image_failed), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean(SettingsFragment.SAVE_TWEET_PREFS, true))
            editor.putString(Const.TWEET_TEXT, mTweetEntry.getText().toString());
        else
            editor.putString(Const.TWEET_TEXT, "");
        editor.apply();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean(SettingsFragment.SAVE_TWEET_PREFS, true)) {
            String tweetText = prefs.getString(Const.TWEET_TEXT, "");
            mTweetEntry.setText(tweetText);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        File prefix;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            prefix = getActivity().getExternalCacheDir();
        else
            prefix = getActivity().getCacheDir();

        File file = new File(prefix
                + Const.PHOTO_PATH);
        if (file.exists())
            file.delete();
    }

}

