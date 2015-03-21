package com.rtweel.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import org.apache.http.protocol.HTTP;

import java.io.File;

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
    private ImageView retweetsButton;
    private ImageView favsButton;
    private ImageView deleteButton;
    private TextView retweetsCountView;
    private TextView favsCountView;
    private ImageView profilePictureView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_detail, null);
        init(mView);
        setHasOptionsMenu(true);
        return mView;
    }

    private void init(View v) {
        nameView = (TextView) v.findViewById(R.id.detail_name);
        textView = (TextView) v.findViewById(R.id.detail_text);
        dateView = (TextView) v.findViewById(R.id.detail_date);
        retweetsButton = (ImageView) v.findViewById(R.id.detail_retweet_button);
        favsButton = (ImageView) v.findViewById(R.id.detail_favorited_button);
        deleteButton = (ImageView) v.findViewById(R.id.detail_delete);
        retweetsCountView = (TextView) v.findViewById(R.id.detail_retweet_count);
        favsCountView = (TextView) v.findViewById(R.id.detail_favorited_count);
        profilePictureView = (ImageView) v.findViewById(R.id.detail_profile_picture);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowTitleEnabled(false);

        Bundle start = getArguments();
        if(start != null) {
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

            RelativeLayout relativeLayout = (RelativeLayout) mView.findViewById(R.id.detail_layout);
            if (entities.length > 0) {
                Log.i("DEBUG", "Entities length: " + entities.length);
                String cacheName = "entity_" + mTweet.getId();
                for (int i = 0; i < entities.length; i++) {
                    urls[i] = entities[i].getMediaURL();


                    views[i] = new ImageView(getActivity());

                    Picasso.with(getActivity()).load(urls[0]).into(views[i]);
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

            Picasso.with(getActivity()).load(imageUri).into(profilePictureView);

            nameView.setText(name);
            textView.setText(text);
            dateView.setText(date);
            retweetsCountView.setText(String.valueOf(retweetsCount));
            retweetsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!name.equals(Timeline.getUserName())) {
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

            if (name.equals(Timeline.getUserName())) {
                deleteButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new DeleteTweetTask(DetailFragment.this,
                                position).execute(id);
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
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
                File file = new File(getActivity().getExternalCacheDir() + " tmp.jpg");
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
