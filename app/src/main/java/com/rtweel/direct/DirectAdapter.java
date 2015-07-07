package com.rtweel.direct;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.rtweel.Const;
import com.rtweel.MainActivity;
import com.rtweel.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.DirectMessage;

public class DirectAdapter extends RecyclerView.Adapter<DirectAdapter.ViewHolder> {

    private final List<DirectUser> mUsers;
    private final Context mContext;

    public DirectAdapter(List<DirectUser> data, Context context) {
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
    public DirectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.direct_user_item, parent, false);

        TextView description = (TextView) v
                .findViewById(R.id.direct_last_message);
        TextView name = (TextView) v
                .findViewById(R.id.direct_name);
        RoundedImageView picture = (RoundedImageView) v
                .findViewById(R.id.direct_picture);
        TextView messagesCount = (TextView) v
                .findViewById(R.id.direct_messages_count);

        return new ViewHolder(v, name, description, picture, messagesCount);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DirectUser directUser = mUsers.get(position);

        String imageUri = directUser.user.getProfileImageURL();

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.BLACK)
                .borderWidthDp(1)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.with(mContext).load(imageUri)
                .placeholder(R.drawable.placeholder).transform(transformation).into(holder.getPictureView());

        holder.getNameView().setText(directUser.user.getScreenName());


        DirectMessage message = directUser.getLastMessage();
        //View hidingView = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? holder.getLastMessageView() : (View) holder.getLastMessageView().getParent();
        holder.getLastMessageView().setText(message.getText());

        int messagesCount = directUser.sentMessages.size();
        if (messagesCount > 0) {
            holder.getMessagesCountView().setText(String.valueOf(messagesCount));
            holder.getMessagesCountView().setTextColor(Color.RED);
            holder.getMessagesCountView().setVisibility(View.VISIBLE);
        } else {
            messagesCount = directUser.receivedMessages.size();
            if (messagesCount > 0) {
                holder.getMessagesCountView().setText(String.valueOf(messagesCount));
                holder.getMessagesCountView().setTextColor(Color.GREEN);
                holder.getMessagesCountView().setVisibility(View.VISIBLE);
            } else
                holder.getMessagesCountView().setVisibility(View.GONE);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mNameView;
        private final TextView mLastMessageView;
        private final RoundedImageView mPictureView;
        private final TextView mMessagesCount;

        public ViewHolder(final View main, TextView name, TextView description,
                          RoundedImageView picture, TextView messagesCount) {
            super(main);
            main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();

                    RecyclerView rv = (RecyclerView) main.getParent();
                    DirectAdapter adapter = (DirectAdapter) rv.getAdapter();

                    DirectUser user = adapter.mUsers.get(position);

//                    MainProfileFragment fragment = new MainProfileFragment();
//                    Bundle args = new Bundle();
//                    args.putString(Const.SCREEN_USERNAME, directUser.user.getScreenName());
//                    args.putString(Const.USERNAME, directUser.user.getName());
//                    args.putLong(Const.USER_ID, directUser.user.getId());
//                    fragment.setArguments(args);
//                    ((MainActivity) adapter.mContext).setMainFragment(fragment);

                    Bundle args = new Bundle();
                    ArrayList<ChatMessage> messages = new ArrayList<>();
                    for (DirectMessage dm : user.receivedMessages)
                        messages.add(new ChatMessage(dm));
                    for (DirectMessage dm : user.sentMessages)
                        messages.add (new ChatMessage(dm));
                    Collections.sort(messages);
                    args.putParcelableArrayList(Const.CHAT_MESSAGES, messages);
                    ChatFragment fragment = ChatFragment.newInstance(args);
                    ((MainActivity) adapter.mContext).setMainFragment(fragment);

                }
            });

            this.mNameView = name;
            this.mLastMessageView = description;
            this.mPictureView = picture;
            this.mMessagesCount = messagesCount;
        }

        public TextView getNameView() {
            return mNameView;
        }

        public TextView getLastMessageView() {
            return mLastMessageView;
        }

        public ImageView getPictureView() {
            return mPictureView;
        }

        public TextView getMessagesCountView() {
            return mMessagesCount;
        }
    }
}



