package com.rtweel.direct;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.Const;
import com.rtweel.MainActivity;
import com.rtweel.R;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.storage.Tweets;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 23.6.15.
 */
public class ChatFragment extends BaseFragment {

    private ListView list;
    private FloatingActionButton fab;
    private EditText edit;
    private ImageView media;

    private Bitmap bitmap;

    private ChatAdapter adapter;

    public static ChatFragment getInstance(Bundle args) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.chat_fragment, container, false);

        fab = (FloatingActionButton) v.findViewById(R.id.chat_fav);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage();
            }
        });
        list = (ListView) v.findViewById(R.id.chat_list);
        list.setDivider(null);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage message = (ChatMessage) adapter.getItem(position);
                if (message.hasMedia()) {
                    String url = message.getMediaUrl();

                    new LoadDMMediaTask(getActivity(), media).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

                    list.setVisibility(View.GONE);

                    getMainActivity().showLoadingBar();

                    fab.setColorNormal(Color.CYAN);

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveImageToGallery(bitmap);
                            showList();
                        }
                    });
                }
            }
        });

        edit = (EditText) v.findViewById(R.id.chat_new_message);
        media = (ImageView) v.findViewById(R.id.chat_media);

        return v;
    }

    private void saveImageToGallery(Bitmap bitmap) {
        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, new Date().toString(), "");
    }

    private void newMessage() {
        edit.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showList();
            }
        });
    }

    public void showList() {
        list.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);
        media.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage();
            }
        });
    }

    private void initList(ArrayList<ChatMessage> messages) {
        adapter = new ChatAdapter(getActivity(), messages);
        list.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            ArrayList<ChatMessage> messages = args.getParcelableArrayList(Const.CHAT_MESSAGES);
            initList(messages);
        }
    }

    public boolean isListShown() {
        return list.getVisibility() == View.VISIBLE;
    }

    private class LoadDMMediaTask extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private ImageView mImage;

        LoadDMMediaTask(Context context, ImageView image) {
            mContext = context;
            mImage = image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Twitter twitter = Tweets.getTwitter(mContext);
            InputStream is = null;

            try {
                is = twitter.directMessages().getDMImageAsStream(url);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return BitmapFactory.decodeStream(is);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mContext != null) {

                bitmap = result;

                mImage.setVisibility(View.VISIBLE);
                mImage.setImageBitmap(bitmap);

                ((MainActivity) mContext).hideLoadingBar();
            }

            super.onPostExecute(bitmap);
        }

    }

}


