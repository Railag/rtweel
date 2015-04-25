package com.rtweel.tasks.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.rtweel.storage.App;
import com.rtweel.utils.MediaParser;
import com.rtweel.storage.TweetDatabase;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.UserMentionEntity;

public class DbWriteTask extends AsyncTask<Void, Void, Void> {

    private final Context sContext;

    private List<twitter4j.Status> mList = new ArrayList<twitter4j.Status>();

    private boolean mIsHomeTimeline;

    public DbWriteTask(Context context, List<twitter4j.Status> list,
                       boolean isHomeTimeline) {
        sContext = context;
        mList.addAll(list);
        mIsHomeTimeline = isHomeTimeline;
    }

    @Override
    protected Void doInBackground(Void... params) {

        ContentResolver resolver = sContext.getContentResolver();
        ContentValues values = new ContentValues();
        for (twitter4j.Status s : mList) {
            values.put(TweetDatabase.Tweets.COLUMN_AUTHOR, s
                    .getUser().getName().replace("'", "\'"));
            String text = s.getText().replace('\'', ' ')
                    .replace("'", "\'");
            values.put(TweetDatabase.Tweets.COLUMN_TEXT, text);
            values.put(TweetDatabase.Tweets.COLUMN_PICTURE, s
                    .getUser().getProfileImageURL());

            values.put(TweetDatabase.Tweets.COLUMN_DATE, s
                    .getCreatedAt().toString());

            values.put(TweetDatabase.Tweets._ID, s.getId());
            MediaEntity[] entities = s.getExtendedMediaEntities();

            if (entities.length > 0) {
                ArrayList<String> medias = new ArrayList<>();
                for(MediaEntity me : entities)
                    medias.add(me.getMediaURL());

                String media = MediaParser.serialize(medias.toArray(new String[medias.size()]));

                values.put(TweetDatabase.Tweets.COLUMN_MEDIA, media);
            } else {
                values.put(TweetDatabase.Tweets.COLUMN_MEDIA, "");
            }

            if (mIsHomeTimeline) {
                resolver.insert(
                        TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                        values);
            } else {
                values.put(TweetDatabase.Tweets.COLUMN_IS_FAVORITE, s.isFavorited() ? 1 : 0);
                values.put(TweetDatabase.Tweets.COLUMN_USER_ID, s.getUser().getId());
                values.put(TweetDatabase.Tweets.COLUMN_IS_RETWEET, s.isRetweet() ? 1 : 0);


                UserMentionEntity[] mentions = s.getUserMentionEntities();
                boolean hasMentions = mentions != null && mentions.length > 0;
                values.put(TweetDatabase.Tweets.COLUMN_MENTIONS, hasMentions ? mentions[0].getText() : "");
                //TODO ALL MENTIONS PROCESSING
                if (!App.existsInDb(s.getId(), TweetDatabase.Tweets.TABLE_NAME_TWEET))
                    resolver.insert(
                            TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                            values);
                else
                    resolver.update(TweetDatabase.Tweets.CONTENT_URI_TWEET_DB, values, "_ID=?", new String[] {String.valueOf(s.getId())});

            }
        }

        return null;
    }

}