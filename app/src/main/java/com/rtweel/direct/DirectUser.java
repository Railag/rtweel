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

    private boolean isReceivedMessages() {
        return !receivedMessages.isEmpty();
    }

    private boolean isSentMessages() {
        return !sentMessages.isEmpty();
    }

    public DirectMessage getLastMessage() {

        DirectMessage lastMessage;

        if (isReceivedMessages())
            lastMessage = receivedMessages.get(0);
        else if (isSentMessages())
            lastMessage = sentMessages.get(0);
        else
            return null;


        if (isReceivedMessages())
            for (DirectMessage dm : receivedMessages) {
                if (dm.getCreatedAt().after(lastMessage.getCreatedAt()))
                    lastMessage = dm;
            }

        if (isSentMessages())
            for (DirectMessage dm : sentMessages) {
                if (dm.getCreatedAt().after(lastMessage.getCreatedAt()))
                    lastMessage = dm;
            }


        return lastMessage;
    }
}
