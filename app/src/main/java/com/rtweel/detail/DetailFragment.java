package com.rtweel.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
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
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.profile.MainProfileFragment;
import com.rtweel.storage.AppUser;
import com.rtweel.tag.TagFragment;
import com.rtweel.tasks.tweet.DeleteTweetTask;
import com.rtweel.tasks.tweet.DetailRefreshTweetTask;
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
import java.util.ArrayList;
import java.util.Arrays;

import twitter4j.MediaEntity;
import twitter4j.Status;

/**
 * Created by firrael on 22.3.15.
 */
public class DetailFragment extends BaseFragment implements Hide {

    private final static String RESTRICTED_SYMBOLS = ":.,";
    private static String sPath;
    private View mView;
    private Boolean mIsRetweeted;
    private Boolean mIsFavorited;
    private Long mRetweetId;
    private Status mTweet;
    private int mPosition;
    private Bundle mSaved;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_detail, null);
        initViews(mView);
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

        getMainActivity().showLoadingBar();

        Bundle start = getArguments();
        mTweet = (Status) start.getSerializable(Const.TWEET);
        mPosition = start.getInt(Const.TWEET_POSITION);

        new DetailRefreshTweetTask(getActivity(), mPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTweet.getId());

        init(start);
    }

    @Nullable
    @Override
    protected String getTitle() {
        return null;
    }

    private void refresh(Bundle args) {
        if (args != null) {

            mTweet = (Status) args.getSerializable(Const.TWEET);
            if (retweetsCountView == null)
                return;

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

            Log.i("FIX", mTweet.getText());
            retweetsCountView.setText(String.valueOf(retweetsCount));
            retweetsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!name.equals(AppUser.getUserName(getActivity()))) {
                        new RetweetTask(DetailFragment.this, retweetsButton, retweetsCountView,
                                mIsRetweeted).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id, mRetweetId);
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
                            mIsFavorited).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
                }
            });
        }

    }

    private void init(Bundle start) {

        if (start != null) {
            sPath = Environment.getExternalStorageDirectory() + "/"
                    + getActivity().getPackageName() + "/tmp.jpg";
            mTweet = (Status) start.getSerializable(Const.TWEET);
            final String name = mTweet.getUser().getName();
            String date = DateParser.parse(mTweet.getCreatedAt().toString());


            mPosition = start.getInt(Const.POSITION);

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
                    MainProfileFragment fragment = new MainProfileFragment();
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
                                        mPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
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
            final String[] urls = new String[entities.length];
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

                    views[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Rect rect = new Rect();
                            v.getGlobalVisibleRect(rect);
                            ArrayList<Integer> points = new ArrayList<>();
                            points.add(rect.left);
                            points.add(rect.top);
                            points.add(rect.right);
                            points.add(rect.bottom);

                            DetailImagePagerFragment fragment = new DetailImagePagerFragment();
                            Bundle args = new Bundle();
                            args.putStringArrayList(Const.MEDIA_LIST, new ArrayList<>(Arrays.asList(urls)));
                            args.putInt(Const.SELECTED_MEDIA, v.getId() - 1);
                            args.putIntegerArrayList(Const.IMAGE_RECT, points);
                            fragment.setArguments(args);
                            getMainActivity().setMainFragment(fragment);
                        }
                    });

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

        ss = findSpannables(ss, '@');

        ss = findSpannables(ss, '#');

        ss = findSpannables(ss, 'h');

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString findSpannables(SpannableString ss, char c) {

        String text = ss.toString();
        int fi;
        int fiEnd = -1;

        while (true) {
            fi = text.indexOf(c, fiEnd + 1);
            if (fi != -1) {
                fiEnd = text.indexOf(' ', fi + 1);
                if (fiEnd == -1)
                    fiEnd = text.length();

                ClickableSpan clickableSpan = null;
                switch (c) {
                    case '@':
                        clickableSpan = getProfileSpan(fi, fiEnd, text);
                        break;
                    case '#':
                        clickableSpan = getTagSpan(fi, fiEnd, text);
                        break;
                    case 'h':
                        int interval = fiEnd - fi;
                        if (interval > ss.length() - fi)
                            continue;
                        String url = ss.subSequence(fi, fiEnd).toString();
                        if (url.startsWith("http"))
                            clickableSpan = getUrlSpan(fi, fiEnd, text);
                        break;
                }

                if (clickableSpan != null) {
                    TextPaint textPaint = new TextPaint();
                    textPaint.baselineShift = 2;
                    clickableSpan.updateDrawState(textPaint);

                    ss.setSpan(clickableSpan, fi, fiEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else
                break;
        }

        return ss;
    }

    private ClickableSpan getTagSpan(int fi, int fiEnd, String text) {
        TagClickableSpan clickableSpan = new TagClickableSpan();
        clickableSpan.mFi = fi + 1; // for char
        if (RESTRICTED_SYMBOLS.contains(text.substring(fiEnd - 1, fiEnd))) // for @name:
            fiEnd--;

        clickableSpan.mFiEnd = fiEnd;
        return clickableSpan;
    }

    private ClickableSpan getProfileSpan(int fi, int fiEnd, String text) {
        ProfileClickableSpan clickableSpan = new ProfileClickableSpan();
        clickableSpan.mFi = fi + 1; // for char
        if (RESTRICTED_SYMBOLS.contains(text.substring(fiEnd - 1, fiEnd))) // for @name:
            fiEnd--;

        clickableSpan.mFiEnd = fiEnd;
        return clickableSpan;
    }

    private ClickableSpan getUrlSpan(int fi, int fiEnd, String text) {
        UrlClickableSpan clickableSpan = new UrlClickableSpan();
        clickableSpan.mFi = fi;
        String substr = text.substring(fiEnd - 1, fiEnd);
        if (RESTRICTED_SYMBOLS.contains(substr))
            fiEnd--;


        clickableSpan.mFiEnd = fiEnd;
        return clickableSpan;
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
        if (getMainActivity() != null)
            getMainActivity().hideLoadingBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSaved != null)
            refresh(mSaved);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSaved = new Bundle();
        mSaved.putSerializable(Const.TWEET, mTweet);
    }

    private class ProfileClickableSpan extends ClickableSpan {
        private int mFi, mFiEnd;

        @Override
        public void onClick(View widget) {
            MainProfileFragment fragment = new MainProfileFragment();
            Bundle args = new Bundle();
            args.putString(Const.SCREEN_USERNAME, ((TextView) widget).getText().subSequence(mFi, mFiEnd).toString());
            fragment.setArguments(args);
            getMainActivity().setMainFragment(fragment);
        }
    }

    private class TagClickableSpan extends ClickableSpan {
        private int mFi, mFiEnd;

        @Override
        public void onClick(View widget) {
            TagFragment tagFragment = new TagFragment();
            Bundle args = new Bundle();
            args.putString(TagFragment.QUERY, ((TextView) widget).getText().subSequence(mFi, mFiEnd).toString());
            tagFragment.setArguments(args);
            getMainActivity().setMainFragment(tagFragment);
        }
    }

    private class UrlClickableSpan extends ClickableSpan {
        private int mFi, mFiEnd;

        @Override
        public void onClick(View widget) {
            String url = ((TextView) widget).getText().subSequence(mFi, mFiEnd).toString();
            getMainActivity().loadUrl(url);
        }
    }

}
