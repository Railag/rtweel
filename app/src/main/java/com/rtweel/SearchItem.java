package com.rtweel;

import twitter4j.Status;
import twitter4j.User;

/**
 * Created by firrael on 21.6.15.
 */
public class SearchItem {

    private Status tweet;
    private User user;

    public SearchItem(Status tweet) {
        this.setTweet(tweet);
    }

    public SearchItem(User user) {
        this.setUser(user);
    }

    public boolean isUser() {
        return getUser() != null;
    }

    public Status getTweet() {
        return tweet;
    }

    public void setTweet(Status tweet) {
        this.tweet = tweet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
