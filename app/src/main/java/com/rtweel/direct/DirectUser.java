package com.rtweel.direct;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.User;

/**
 * Created by root on 7.5.15.
 */
public class DirectUser {
    public DirectUser() {
        receivedMessages = new ArrayList<>();
        sentMessages = new ArrayList<>();
    }

    public DirectUser(User user) {
        super();
        this.user = user;
    }

    public User user;
    public ArrayList<DirectMessage> receivedMessages;
    public ArrayList<DirectMessage> sentMessages;
}
