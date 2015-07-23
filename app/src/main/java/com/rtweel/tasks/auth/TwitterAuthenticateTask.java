package com.rtweel.tasks.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.rtweel.utils.TwitterUtil;

import twitter4j.auth.RequestToken;

/**
 * Created by firrael on 21.3.15.
 */
public class TwitterAuthenticateTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public TwitterAuthenticateTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        RequestToken token = TwitterUtil.getInstance().getRequestToken();
        if (token != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(token
                            .getAuthenticationURL()));
            mContext.startActivity(intent);
        }

        return null;
    }
}

