package com.rtweel.tasks.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.rtweel.utils.TwitterUtil;

/**
 * Created by root on 21.3.15.
 */
public class TwitterAuthenticateTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public TwitterAuthenticateTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(TwitterUtil.getInstance().getRequestToken()
                        .getAuthenticationURL()));
        mContext.startActivity(intent);
        return null;
    }
}

