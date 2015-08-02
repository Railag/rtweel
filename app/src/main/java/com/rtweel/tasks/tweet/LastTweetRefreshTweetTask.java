package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.Bundle;

import com.rtweel.Const;
import com.rtweel.fragments.SendTweetFragment;

/**
 * Created by firrael on 02.08.2015.
 */
public class LastTweetRefreshTweetTask extends RefreshTweetTask {

    public LastTweetRefreshTweetTask(Context activity) {
        super(activity, -1);
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        super.onPostExecute(status);

        if (mActivity != null) {
            mActivity.hideLoadingBar();

            SendTweetFragment fragment = new SendTweetFragment();
            Bundle args = new Bundle();
            if (status != null) {
                args.putString(Const.TWEET_TEXT, status.getText());
                args.putLong(Const.TWEET_ID, status.getId());
            }
            fragment.setArguments(args);
            mActivity.setMainFragment(fragment);
        }

    }
}
