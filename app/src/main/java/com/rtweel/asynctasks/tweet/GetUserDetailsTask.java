package com.rtweel.asynctasks.tweet;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.tweet.Timeline;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by root on 5.4.15.
 */
public class GetUserDetailsTask extends AsyncTask<Twitter, Void, User> {

    private final Context mContext;
    private final ImageView mBackground;
    private final RoundedImageView mImage;
    private final TextView mUsername;
    private final TextView mUsernameLinked;
    private final TextView mDescription;

    public GetUserDetailsTask(Context context, ImageView background, RoundedImageView image, TextView username, TextView usernameLinked, TextView description) {
        mContext = context;
        mBackground = background;
        mImage = image;
        mUsername = username;
        mUsernameLinked = usernameLinked;
        mDescription = description;
    }

    @Override
    protected User doInBackground(Twitter... params) {
        Twitter twitter = params[0];
        User user = null;
        try {
            String screenName = twitter.getScreenName();
            Timeline.setScreenUserName(screenName);

            user = twitter.showUser(screenName);

            Timeline.setUserName(user.getName());

        } catch (IllegalStateException | TwitterException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);

        //Picasso.with(mContext).load(user.getProfileBackgroundColor()).into(mBackground);
        //Picasso.with(mContext).load(user.getProfileBackgroundImageURL()).into(mImage);
        if(user != null) {
            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(1)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.with(mContext).load(user.getProfileBannerURL()).resize(mBackground.getMeasuredWidth(), mBackground.getMeasuredHeight()).into(mBackground);
            Picasso.with(mContext).load(user.getBiggerProfileImageURL()).transform(transformation).into(mImage);

            mUsername.setText(user.getName());
            mUsername.setTextColor(Color.WHITE);//Color.parseColor("#" + user.getProfileTextColor()));

            mUsernameLinked.setText("@" + user.getScreenName());
            mUsernameLinked.setTextColor(Color.WHITE);//Color.parseColor("#" + user.getProfileTextColor()));

            mDescription.setText(user.getDescription());
            mDescription.setBackgroundColor(Color.parseColor("#" + user.getProfileBackgroundColor()));
        } else
            Toast.makeText(mContext, "Network problems", Toast.LENGTH_SHORT).show();
    }
}
