package com.rtweel.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.profile.ProfileFragment;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.tweet.DeleteTweetTask;
import com.rtweel.tasks.tweet.FavoriteTask;
import com.rtweel.tasks.tweet.RetweetTask;
import com.rtweel.utils.DateParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.http.protocol.HTTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import twitter4j.MediaEntity;
import twitter4j.Status;

/**
 * Created by root on 22.3.15.
 */
public class DetailFragment extends BaseFragment {

    private View mView;

    private Boolean mIsRetweeted;
    private Boolean mIsFavorited;
    private Long mRetweetId;

    private Status mTweet;

    private TextView nameView;
    private TextView textView;
    private TextView dateView;
    private ImageButton retweetsButton;
    private ImageButton favsButton;
    private ImageButton shareButton;
    private ImageButton replyButton;
    private ImageButton deleteButton;
    private TextView retweetsCountView;
    private TextView favsCountView;
    private RoundedImageView profilePictureView;

    private int mediaIds[] = {1, 2, 3, 4, 5};

    private static String sPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_detail, null);
        initViews(mView);
        setHasOptionsMenu(true);
        return mView;
    }

    private void initViews(View v) {
        getMainActivity().hide();
        nameView = (TextView) v.findViewById(R.id.detail_name);
        textView = (TextView) v.findViewById(R.id.detail_text);
        dateView = (TextView) v.findViewById(R.id.detail_date);
        retweetsButton = (ImageButton) v.findViewById(R.id.detail_retweet_button);
        favsButton = (ImageButton) v.findViewById(R.id.detail_favorited_button);
        shareButton = (ImageButton) v.findViewById(R.id.detail_share_button);
        replyButton = (ImageButton) v.findViewById(R.id.detail_reply_button);
        deleteButton = (ImageButton) v.findViewById(R.id.detail_delete);
        retweetsCountView = (TextView) v.findViewById(R.id.detail_retweet_count);
        favsCountView = (TextView) v.findViewById(R.id.detail_favorited_count);
        profilePictureView = (RoundedImageView) v.findViewById(R.id.detail_profile_picture);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle start = getArguments();
        init(start);

    }

    private void refresh(Bundle args) {
        if (args != null) {

            mTweet = (Status) args.getSerializable(Const.TWEET);
            final String name = mTweet.getUser().getName();
            final long id = mTweet.getId();

            int retweetsCount = mTweet.getRetweetCount();
            int favsCount = mTweet.getFavoriteCount();

            mIsFavorited = mTweet.isFavorited();
            mIsRetweeted = mTweet.isRetweetedByMe();

            if (mIsRetweeted) {
                retweetsButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
            }

            if (mIsFavorited) {
                favsButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
            }

            retweetsCountView.setText(String.valueOf(retweetsCount));
            retweetsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!name.equals(AppUser.getUserName(getActivity()))) {
                        new RetweetTask(DetailFragment.this, retweetsButton, retweetsCountView,
                                mIsRetweeted).execute(id, mRetweetId);
                    } else {
                        Toast.makeText(getActivity(),
                                "You can't retweet your own tweet",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

            favsCountView.setText(String.valueOf(favsCount));
            favsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    new FavoriteTask(DetailFragment.this, favsButton, favsCountView,
                            mIsFavorited).execute(id);
                }
            });
        }

    }

    private void init(Bundle start) {

        if (start != null) {
            sPath = Environment.getExternalStorageDirectory() + "/"
                    + getActivity().getPackageName() + "/" + "tmp" + ".jpg";
            mTweet = (Status) start.getSerializable(Const.TWEET);
            final String name = mTweet.getUser().getName();
            String date = DateParser.parse(mTweet.getCreatedAt().toString());


            final int position = start.getInt(Const.POSITION);

            String imageUri = mTweet.getUser().getBiggerProfileImageURL();
            final long id = mTweet.getId();

            mRetweetId = mTweet.getCurrentUserRetweetId();

            shareButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);// _MULTIPLE);
                    shareIntent.setType(HTTP.PLAIN_TEXT_TYPE);
                    shareIntent.setType("*/*");
                    shareIntent
                            .putExtra(Intent.EXTRA_TITLE, mTweet.getUser().getName());

                    shareIntent.putExtra(Intent.EXTRA_TEXT, mTweet.getText());
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTweet.getUser()
                            .getName() + "'s tweet");

                    ImageView mediaView = (ImageView) mView.findViewById(mediaIds[0]);
                    if (mediaView != null)
                        saveToFile(mediaView);
                    File file = new File(sPath);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    String title = "Choose an app to share the tweet";
                    Intent chooser = Intent.createChooser(shareIntent, title);

                    startActivity(chooser);
                }
            });

            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    prefs.edit().putString(Const.TWEET_TEXT, "@" + mTweet.getUser().getScreenName()).commit();
                    Bundle args = new Bundle();
                    args.putLong(Const.REPLY_ID, mTweet.getId());
                    SendTweetFragment fragment = new SendTweetFragment();
                    fragment.setArguments(args);
                    getMainActivity().setMainFragment(fragment);
                }
            });

            profilePictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment fragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString(Const.USERNAME, mTweet.getUser().getName());
                    args.putString(Const.SCREEN_USERNAME, mTweet.getUser().getScreenName());
                    args.putLong(Const.USER_ID, mTweet.getUser().getId());
                    fragment.setArguments(args);
                    getMainActivity().setMainFragment(fragment);
                }
            });

            if (name.equals(AppUser.getUserName(getActivity()))) {
                deleteButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.delete_message));
                        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteTweetTask(DetailFragment.this,
                                        position).execute(id);
                            }
                        });
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.cancel();
                            }
                        });
                        builder.create().show();
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }

            MediaEntity[] entities = mTweet.getExtendedMediaEntities();
            String[] urls = new String[entities.length];
            ImageView[] views = new ImageView[entities.length];

            RelativeLayout relativeLayout = (RelativeLayout) mView.findViewById(R.id.detail_layout);
            if (entities.length > 0) {
                for (int i = 0; i < entities.length; i++) {
                    urls[i] = entities[i].getMediaURL();

                    views[i] = new ImageView(getActivity());
                    views[i].setId(mediaIds[i]);

                    Picasso.with(getActivity()).load(urls[i]).into(views[i]);

                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    p.topMargin = 10;

                    if (i == 0)
                        p.addRule(RelativeLayout.BELOW, R.id.detail_retweet_count);
                    else {
                        p.addRule(RelativeLayout.BELOW, mediaIds[i - 1]);
                    }

                    views[i].setLayoutParams(p);
                    relativeLayout.addView(views[i]);
                }

            }

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(3)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.with(getActivity()).load(imageUri).transform(transformation).into(profilePictureView);

            nameView.setText(name);
            makeSpannableText(mTweet.getText());
            dateView.setText(date);

        }
    }

    private void makeSpannableText(final String text) {
        SpannableString ss = new SpannableString(text);
        int fi = 0;
        int fiEnd = 0;
        while (true) {
            fi = text.indexOf('@', fiEnd + 1);
            if (fi != -1) {
                fiEnd = text.indexOf(' ', fi + 1);
                if (fiEnd == -1)
                    fiEnd = text.length() - 1;
                final ConsistentClickableSpan clickableSpan = new ConsistentClickableSpan();
                clickableSpan.mFi = fi + 1; // for @
                clickableSpan.mFiEnd = fiEnd;

                TextPaint textPaint = new TextPaint();
                textPaint.baselineShift = 2;
                clickableSpan.updateDrawState(textPaint);

                ss.setSpan(clickableSpan, fi, fiEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else
                break;
        }

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void saveToFile(ImageView v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        v.invalidate();
        String tempFilePath = sPath;
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
        }

        tempFile.delete();

        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int quality = 100;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
        b.compress(Bitmap.CompressFormat.JPEG, quality, bos);

        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        b.recycle();
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

    public void setResult(Bundle args) {
        refresh(args);
    }


    private class ConsistentClickableSpan extends ClickableSpan {
        private int mFi, mFiEnd;

        @Override
        public void onClick(View widget) {
            ProfileFragment fragment = new ProfileFragment();
            Bundle args = new Bundle();
            args.putString(Const.SCREEN_USERNAME, ((TextView) widget).getText().subSequence(mFi, mFiEnd).toString());
            fragment.setArguments(args);
            getMainActivity().setMainFragment(fragment);
        }
    }

}
