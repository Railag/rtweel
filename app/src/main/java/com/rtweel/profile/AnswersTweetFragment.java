package com.rtweel.profile;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.fragments.RecyclerViewFragment;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.storage.App;
import com.rtweel.tasks.tweet.MentionsTask;
import com.rtweel.timelines.AnswersTimeline;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by firrael on 6.4.15.
 */
public class AnswersTweetFragment extends TweetFragment {

    private MentionsTask mAdditionalTask;

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
    }

    @Override
    protected void instantiateListData(String username, String screenUserName, long userId) {
        mTimeline = new AnswersTimeline(getActivity().getApplicationContext());
        mTimeline.setUserName(username);
        mTimeline.setScreenUserName(screenUserName);
        mTimeline.setUserId(userId);
    }

    @Override
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP)) {
            return;
        }

        blink();

        Log.i("DEBUG", "SWIPE UP");
        if (mUpTask != null && getUserId() == AppUser.getUserId(getActivity())) {
            if (!mUpTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                mUpTask = new TimelineUpTask(AnswersTweetFragment.this);
                mUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
            }
        }

        if (mAdditionalTask != null)
            if (!mAdditionalTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mAdditionalTask = new MentionsTask(AnswersTweetFragment.this, true);
        mAdditionalTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline.getScreenUserName());
    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        blink();

        Log.i("DEBUG", "SWIPE DOWN");

        if (mDownTask != null && getUserId() == AppUser.getUserId(getActivity())) {
            if (mDownTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                mDownTask = new TimelineDownTask(AnswersTweetFragment.this);
                mDownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
            }
        }

        if (mAdditionalTask != null)
            if (!mAdditionalTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mAdditionalTask = new MentionsTask(AnswersTweetFragment.this, false);
        mAdditionalTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline.getScreenUserName());
    }

    @Override
    protected void startAnim() {
        showProgressBar();
    }

    @Override
    protected void stopAnim() {
        hideProgressBar();
    }

    @Override
    protected String getTitle() {
        return null;
    }

    public long getOldestItemId() {
        ArrayList<Status> tweets = new ArrayList<>(mTimeline.getTweets());
        long id = -1;
        if (tweets.size() > 0)
            id = tweets.get(tweets.size() - 1).getId();

        return id;
    }

    public long getNewestItemId() {
        ArrayList<Status> tweets = new ArrayList<>(mTimeline.getTweets());
        long id = -1;
        if (tweets.size() > 0)
            id = tweets.get(0).getId();

        return id;
    }

    @Override
    protected RecyclerViewFragment.State getState() {
        State state =  super.getState();
        if (mTimeline.getTweets().size() > 0)
            return State.LOADED;

        return state;

    }
}
