package com.rtweel.direct;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.MainActivity;
import com.rtweel.R;
import com.rtweel.storage.Tweets;

import java.io.InputStream;
import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 23.6.15.
 */
public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ChatMessage> messages;

    public ChatAdapter(Context context, ArrayList<ChatMessage> messages) {
        this.mContext = context;
        this.messages = messages;
    }

    public ChatAdapter(Context context) {
        this.mContext = context;
        messages = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final ChatMessage message = messages.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);

            TextView text = (TextView) convertView.findViewById(R.id.chat_text);

            ViewHolder vh = new ViewHolder(text);
            convertView.setTag(vh);
        }

        final ViewHolder vh = (ViewHolder) convertView.getTag();

        TextView tv = vh.getTextView();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();

        tv.setText(message.getText());
        if (message.isLeft(mContext)) {
            tv.setGravity(Gravity.START);
            tv.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.chat_received));
            params.gravity = Gravity.START;
        } else {
            tv.setGravity(Gravity.END);
            tv.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.chat_sent));
            params.gravity = Gravity.END;
        }

        tv.setLayoutParams(params);

        return convertView;
    }

    static class ViewHolder {
        private TextView mText;

        ViewHolder(TextView text) {
            mText = text;
        }

        public TextView getTextView() {
            return mText;
        }

    }

}