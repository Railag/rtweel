package com.rtweel.profile;

import android.support.v7.widget.RecyclerView;

import com.rtweel.TweetAdapter;
import com.rtweel.profile.PagerFragment;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.timelines.Timeline;

/**
 * Created by root on 21.3.15.
 */
public abstract class TweetFragment extends PagerFragment {

    protected TimelineUpTask mUpTask;
    protected TimelineDownTask mDownTask;

    protected Timeline mTimeline;

    protected boolean mContentLoaded;

    protected void listDataLoading() {
        loadTweets();
        if (!getTimeline().isHomeTimeline())
            updateUp(Scroll.UPDATE_UP);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new TweetAdapter(mTimeline, getActivity());
    }

    protected abstract void loadTweets();

    public Timeline getTimeline() {
        return mTimeline;
    }

    @Override
    public long getUserId() {
        return getTimeline().getUserId();
    }
}
