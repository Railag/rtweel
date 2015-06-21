package com.rtweel;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.detail.DetailFragment;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.profile.MainProfileFragment;
import com.rtweel.profile.TweetFragment;
import com.rtweel.tasks.tweet.RefreshTweetTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.User;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private final List<SearchItem> mData;
    private final Context mContext;

    public SearchAdapter(List<SearchItem> data, Context context) {
        mData = data;
        mContext = context;
    }

    public SearchAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        String imageUri;
        String author;
        String text;

        final SearchItem item = mData.get(position);
        if (item.isUser()) {
            User user = item.getUser();
            imageUri = user.getProfileImageURL();
            author = user.getName();
            //text = user.getDescription();
            text = "";
        } else {
            Status tweet = item.getTweet();
            imageUri = tweet.getUser().getProfileImageURL();
            author = tweet.getUser().getName();
            text = tweet.getText();
        }

        if (convertView == null) {
            convertView =  LayoutInflater.from(mContext)
                    .inflate(R.layout.search_item, parent, false);

            TextView textView = (TextView) convertView
                    .findViewById(R.id.tweet_text);
            TextView authorView = (TextView) convertView
                    .findViewById(R.id.tweet_author);
            RoundedImageView pictureView = (RoundedImageView) convertView
                    .findViewById(R.id.tweet_author_picture);

            ViewHolder holder = new ViewHolder(authorView, textView, pictureView);

            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        setupClick(convertView, item, position);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.with(mContext).load(imageUri)
                .placeholder(R.drawable.placeholder).transform(transformation).into(holder.getPictureView());


        holder.getAuthorView().setText(author);

        if (TextUtils.isEmpty(text))
            ((View)holder.getTextView().getParent()).setVisibility(View.GONE);
        else
            ((View)holder.getTextView().getParent()).setVisibility(View.VISIBLE);

        holder.getTextView().setText(text);

        return convertView;
    }

    private void setupClick(View v, final SearchItem item, final int position) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MainActivity act = (MainActivity) mContext;

                act.hideNav();
                act.hideKeyboard();
                act.getCurrentFragment().getView().setVisibility(View.GONE);

                act.showLoadingBar();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        act.hideLoadingBar();

                        if (item.isUser()) {
                            User user = item.getUser();

                            MainProfileFragment fragment = new MainProfileFragment();
                            Bundle args = new Bundle();
                            args.putString(Const.USERNAME, user.getName());
                            args.putString(Const.SCREEN_USERNAME, user.getScreenName());
                            args.putLong(Const.USER_ID, user.getId());
                            fragment.setArguments(args);
                            act.setMainFragment(fragment);

                        } else {
                            Status tweet = item.getTweet();

                            new RefreshTweetTask(mContext, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tweet.getId());

                            DetailFragment fragment = new DetailFragment();
                            Bundle args = new Bundle();
                            args.putSerializable(Const.TWEET, tweet);
                            args.putInt(Const.POSITION, position);
                            fragment.setArguments(args);
                            act.setMainFragment(fragment);
                        }
                    }
                }, 1500);

            }
        });
    }

    @Override
    public Filter getFilter() {

        Filter filter = new  Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = mData;
                if (mData != null)
                    results.count = mData.size();
                else results.count = 0;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };

        return filter;
    }


    public static class ViewHolder {

        private final TextView mAuthorView;
        private final TextView mTextView;
        private final RoundedImageView mPictureView;

        public ViewHolder(TextView user, TextView text,
                          RoundedImageView picture) {

            this.mAuthorView = user;
            this.mTextView = text;
            this.mPictureView = picture;
        }

        public TextView getAuthorView() {
            return mAuthorView;
        }

        public TextView getTextView() {
            return mTextView;
        }


        public ImageView getPictureView() {
            return mPictureView;
        }

    }
}


