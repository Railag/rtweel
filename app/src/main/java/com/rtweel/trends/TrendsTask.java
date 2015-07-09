package com.rtweel.trends;

import android.os.AsyncTask;

import com.rtweel.storage.Tweets;

import java.util.ArrayList;
import java.util.Arrays;

import twitter4j.Location;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 9.7.15.
 */
public class TrendsTask extends AsyncTask<Void, Void, Void> {

    private TrendsFragment mFragment;

    public TrendsTask(TrendsFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());
        Location location;
        try {

            Location[] locations = twitter.getAccountSettings().getTrendLocations();
            if (locations.length > 0)
                location = locations[0];
            else {
                cancel(true);
                return null;
            }

        } catch (TwitterException e) {
            e.printStackTrace();
            cancel(true);
            return null;
        }

        try {
            Trends trends = twitter.getPlaceTrends(location.getWoeid());
            processTrends(trends);
        } catch (TwitterException e) {
            e.printStackTrace();
            cancel(true);
        }

        return null;
    }

    private void processTrends(Trends trends) {
        if (trends.getTrends() != null && trends.getTrends().length > 0)
            mFragment.update(new ArrayList<>(Arrays.asList(trends.getTrends())));
    }

    @Override
    protected void onPostExecute(Void result) {
        mFragment.getAdapter().notifyDataSetChanged();

        mFragment.stopAnim();
    }
}

