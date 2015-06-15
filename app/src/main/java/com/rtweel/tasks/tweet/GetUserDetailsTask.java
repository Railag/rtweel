package com.rtweel.tasks.tweet;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by firrael on 5.4.15.
 */
public class GetUserDetailsTask extends AsyncTask<Twitter, Void, User> {

    private final Context mContext;
    private final ImageView mBackground;
    private final RoundedImageView mImage;
    private final TextView mUsername;
    private final TextView mUsernameLinked;
    private final TextView mDescription;
    private Long mId;

    public GetUserDetailsTask(Context context, ImageView background, RoundedImageView image, TextView username, TextView usernameLinked, TextView description, Long id ) {
        mContext = context;
        mBackground = background;
        mImage = image;
        mUsername = username;
        mUsernameLinked = usernameLinked;
        mDescription = description;
        mId = id;
    }

    @Override
    protected User doInBackground(Twitter... params) {
        Twitter twitter = params[0];

        if (twitter == null)
            return null;

        User user = null;
        try {
            String screenName = mUsernameLinked.getText().toString();

            user = twitter.showUser(screenName);

        } catch (IllegalStateException | TwitterException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);

        if (mContext != null) {
            if (user != null) {
                Transformation transformation = new RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(2)
                        .cornerRadiusDp(30)
                        .oval(false)
                        .build();

                Picasso.with(mContext).load(user.getProfileBannerURL()).resize(mBackground.getMeasuredWidth(), mBackground.getMeasuredHeight()).into(mBackground);
                Picasso.with(mContext).load(user.getBiggerProfileImageURL()).transform(transformation).into(mImage);

                mId = user.getId();

                mUsername.setText(user.getName());
                mUsername.setTextColor(Color.WHITE);//Color.parseColor("#" + user.getProfileTextColor()));

                mUsernameLinked.setText("@" + user.getScreenName());
                mUsernameLinked.setTextColor(Color.WHITE);//Color.parseColor("#" + user.getProfileTextColor()));

                if(TextUtils.isEmpty(user.getDescription()))
                    mDescription.setVisibility(View.GONE);
                else {
                    mDescription.setText(user.getDescription());
                    mDescription.setBackgroundColor(Color.parseColor("#" + user.getProfileBackgroundColor()));
                }
            } else
                Toast.makeText(mContext, "Network problems", Toast.LENGTH_SHORT).show();
        } else
            Log.e("Exception", "GetUserDetailsTask lost context");

    }
}
