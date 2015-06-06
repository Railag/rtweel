package com.rtweel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.tasks.tweet.RefreshTweetTask;
import com.rtweel.detail.DetailFragment;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.timelines.Timeline;
import com.rtweel.utils.DateParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private final List<Status> mData;
    private final Context mContext;

    public TweetAdapter(List<Status> data, Context context) {
        mData = data;
        mContext = context;
    }

    public TweetAdapter(Timeline timeline, Context context) {
        mData = timeline.getTweets();
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public TweetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.tweet_item, parent, false);

        TextView text = (TextView) v
                .findViewById(R.id.tweet_text);
        TextView author = (TextView) v
                .findViewById(R.id.tweet_author);
        TextView date = (TextView) v
                .findViewById(R.id.tweet_date);
        RoundedImageView picture = (RoundedImageView) v
                .findViewById(R.id.tweet_author_picture);

        ImageView media = (ImageView) v.findViewById(R.id.tweet_media);

        return new ViewHolder(v, author, text, date, picture, media);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Status tweet = mData.get(position);
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        String url = null;
        if (preferences.getBoolean(SettingsFragment.IMAGES_SHOWN_PREFS, true)) {
            MediaEntity[] entities = tweet.getExtendedMediaEntities();
            if (entities.length > 0)
                url = entities[0].getMediaURL();
        }

        Picasso.with(mContext).load(url)
                .placeholder(R.drawable.placeholder).resize(200, 200).into(holder.getMediaView());

        String imageUri = tweet.getUser().getProfileImageURL();

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.with(mContext).load(imageUri)
                .placeholder(R.drawable.placeholder).transform(transformation).into(holder.getPictureView());

        holder.getAuthorView().setText(tweet.getUser().getName());

        holder.getTextView().setText(tweet.getText().replace("\\n", "\n"));

        String date = DateParser.parse(tweet.getCreatedAt().toString());

        holder.getDateView().setText(date);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mAuthorView;
        private final TextView mTextView;
        private final TextView mDateView;
        private final RoundedImageView mPictureView;
        private final ImageView mMediaView;

        public ViewHolder(final View main, TextView user, TextView text, TextView date,
                          RoundedImageView picture, ImageView media) {
            super(main);
            main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();

                    RecyclerView rv = (RecyclerView) main.getParent();
                    TweetAdapter adapter = (TweetAdapter) rv.getAdapter();

                    Status tweet = adapter.mData.get(position);

                    new RefreshTweetTask(adapter.mContext, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tweet.getId());

                    DetailFragment fragment = new DetailFragment();
                    Bundle args = new Bundle();
                    args.putSerializable(Const.TWEET, tweet);
                    args.putInt(Const.POSITION, position);
                    fragment.setArguments(args);
                    ((MainActivity) adapter.mContext).setMainFragment(fragment);

                }
            });

            this.mAuthorView = user;
            this.mTextView = text;
            this.mDateView = date;
            this.mPictureView = picture;
            this.mMediaView = media;
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
    }
}


