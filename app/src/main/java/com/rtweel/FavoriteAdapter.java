package com.rtweel;

/**
 * Created by root on 28.4.15.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.profile.ProfileFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import twitter4j.User;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final List<User> mUsers;
    private final Context mContext;

    public FavoriteAdapter(List<User> data, Context context) {
        mUsers = data;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.follower_item, parent, false);

        TextView description = (TextView) v
                .findViewById(R.id.follower_description);
        TextView name = (TextView) v
                .findViewById(R.id.follower_name);
        RoundedImageView picture = (RoundedImageView) v
                .findViewById(R.id.follower_picture);

        return new ViewHolder(v, name, description, picture);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        User user = mUsers.get(position);

        String imageUri = user.getProfileImageURL();

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.with(mContext).load(imageUri)
                .placeholder(R.drawable.placeholder).transform(transformation).into(holder.getPictureView());

        holder.getNameView().setText(user.getScreenName());


        String description = user.getDescription();
        View hidingView = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? holder.getDescView() : (View) holder.getDescView().getParent();
        if (!TextUtils.isEmpty(description)) {
            holder.getDescView().setText(user.getDescription());
            hidingView.setVisibility(View.VISIBLE);
        } else
            hidingView.setVisibility(View.GONE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mNameView;
        private final TextView mDescriptionView;
        private final RoundedImageView mPictureView;

        public ViewHolder(final View main, TextView name, TextView description,
                          RoundedImageView picture) {
            super(main);
            main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();

                    RecyclerView rv = (RecyclerView) main.getParent();
                    FavoriteAdapter adapter = (FavoriteAdapter) rv.getAdapter();

                    User user = adapter.mUsers.get(position);

                    ProfileFragment fragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString(Const.SCREEN_USERNAME, user.getScreenName());
                    args.putString(Const.USERNAME, user.getName());
                    args.putLong(Const.USER_ID, user.getId());
                    fragment.setArguments(args);
                    ((MainActivity) adapter.mContext).setMainFragment(fragment);

                }
            });

            this.mNameView = name;
            this.mDescriptionView = description;
            this.mPictureView = picture;
        }

        public TextView getNameView() {
            return mNameView;
        }

        public TextView getDescView() {
            return mDescriptionView;
        }

        public ImageView getPictureView() {
            return mPictureView;
        }
    }
}



