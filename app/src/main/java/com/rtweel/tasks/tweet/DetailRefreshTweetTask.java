package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.rtweel.Const;
import com.rtweel.detail.DetailFragment;
import com.rtweel.detail.DetailPagerFragment;
import com.rtweel.fragments.BaseFragment;


/**
 * Created by firrael on 02.08.2015.
 */
public class DetailRefreshTweetTask extends RefreshTweetTask {

    public DetailRefreshTweetTask(Context activity, int position) {
        super(activity, position);
    }

    @Override
    protected void onPostExecute(twitter4j.Status result) {
        super.onPostExecute(result);
        if(mActivity != null) {
            if (result != null) {
                Fragment fragment = mActivity.getCurrentFragment();
                if (fragment instanceof DetailPagerFragment) {
                    DetailFragment detailFragment = ((DetailPagerFragment) fragment).getItem(mPosition);
                    Bundle args = new Bundle();
                    args.putSerializable(Const.TWEET, result);
                    args.putInt(Const.TWEET_POSITION, mPosition);
                    detailFragment.setResult(args);
                }
            } else {
                new DeleteTweetTask((BaseFragment) mActivity.getCurrentFragment(), mPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mId);
            }
        } else
            Log.e("Exception", "RefreshTweetTask lost context");
    }
}
