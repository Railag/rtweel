package com.rtweel.tweet;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtweel.R;
import com.rtweel.parsers.DateParser;
import com.squareup.picasso.Picasso;

public class TweetAdapter extends BaseAdapter {

    private final List<Status> mData;
    private final Context mContext;

    private static ArrayList<String> sLoadedUris = new ArrayList<String>();

    boolean mIsMediaAvailable;

    public TweetAdapter(List<Status> data, Context context) {
        this.mData = data;
        this.mContext = context;
    }

    public TweetAdapter(Timeline timeline, Context context) {
        this.mData = timeline.getTweets();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Status tweet = mData.get(position);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        String url = null;

        if (preferences.getBoolean("images_shown", false)) {
            MediaEntity[] entities = tweet.getMediaEntities();

            if (entities.length > 0)
                url = entities[0].getMediaURL();

        }

        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item, parent, false);

            TextView text = (TextView) convertView
                    .findViewById(R.id.tweet_text);
            TextView author = (TextView) convertView
                    .findViewById(R.id.tweet_author);
            TextView date = (TextView) convertView
                    .findViewById(R.id.tweet_date);
            ImageView picture = (ImageView) convertView
                    .findViewById(R.id.tweet_author_picture);

            ImageView media = null;

            if (!TextUtils.isEmpty(url)) {
                media = new ImageView(mContext);
                LinearLayout layout = (LinearLayout) convertView;
                layout.addView(media);
            }

            ViewHolder vh = new ViewHolder(author, text, date, picture, media, url);

            convertView.setTag(vh);

        }

        ViewHolder vh = (ViewHolder) convertView.getTag();


        if (!TextUtils.isEmpty(vh.getUrl())) {
            Log.i("DEBUG", vh.getUrl() + " TRUE");
            Picasso.with(mContext).load(url)
                    .placeholder(R.drawable.placeholder).into(vh.getMediaView());
        }

        String imageUri = tweet.getUser().getProfileImageURL();// getMiniProfileImageURL();

        String cacheName = tweet.getUser().getName().replace(' ', '_')
                + "_mini";

        //vh.loadBitmapProfile(imageUri, vh.getPictureView(), cacheName);
        Picasso.with(mContext).load(imageUri)
                .placeholder(R.drawable.rtweel).into(vh.getPictureView());

        vh.getAuthorView().setText(tweet.getUser().getName());

        vh.getTextView().setText(tweet.getText().replace('\n', ' '));

        String date = DateParser.parse(tweet.getCreatedAt().toString());

        vh.getDateView().setText(date);

        return convertView;
    }

    private class ViewHolder {
        private final TextView mAuthorView;
        private final TextView mTextView;
        private final TextView mDateView;
        private final ImageView mPictureView;
        private final ImageView mMediaView;
        private final String mUrl;

        public ViewHolder(TextView user, TextView text, TextView date,
                          ImageView picture, ImageView media, String url) {
            this.mAuthorView = user;
            this.mTextView = text;
            this.mDateView = date;
            this.mPictureView = picture;
            this.mMediaView = media;
            this.mUrl = url;
        }

        public TextView getAuthorView() {
            return mAuthorView;
        }

        public TextView getTextView() {
            return mTextView;
        }

        public TextView getDateView() {
            return mDateView;
        }

        public ImageView getPictureView() {
            return mPictureView;
        }

        public ImageView getMediaView() {
            return mMediaView;
        }

        public String getUrl() {
            return mUrl;
        }
    }

    /*public static boolean isInList(String url) {
        return sLoadedUris.contains(url);
    }

    public static void addToList(String url) {
        sLoadedUris.add(url);
    }
    */

}
