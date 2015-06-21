package com.rtweel.direct;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.User;

/**
 * Created by firrael on 7.5.15.
 */
public class DirectUser {
    public DirectUser() {
        receivedMessages = new ArrayList<>();
        sentMessages = new ArrayList<>();
    }

    public DirectUser(User user) {
        super();
        this.user = user;
        receivedMessages = new ArrayList<>();
        sentMessages = new ArrayList<>();
    }

    public User user;
    public ArrayList<DirectMessage> receivedMessages;
    public ArrayList<DirectMessage> sentMessages;
}
