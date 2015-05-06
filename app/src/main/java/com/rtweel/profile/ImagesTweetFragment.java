package com.rtweel.profile;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.storage.App;
import com.rtweel.timelines.ImagesTimeline;

/**
 * Created by root on 6.4.15.
 */
public class ImagesTweetFragment extends TweetFragment {

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).execute(mTimeline);
    }

    @Override
    protected void instantiateListData(String username, String screenUserName, long userId) {
        mTimeline = new ImagesTimeline(getActivity().getApplicationContext());
        mTimeline.setUserName(username);
        mTimeline.setScreenUserName(screenUserName);
        mTimeline.setUserId(userId);
    }

    @Override
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Up swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE UP");
        if (mUpTask != null)
            if (!mUpTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mUpTask = new TimelineUpTask(ImagesTweetFragment.this);
        mUpTask.execute(mTimeline);

    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Down swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE DOWN");

        if (mDownTask != null)
            if (!mDownTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mDownTask = new TimelineDownTask(ImagesTweetFragment.this);
        mDownTask.execute(mTimeline);
    }

    @Override
    protected void loadingAnim() {
        //TODO
    }

}
