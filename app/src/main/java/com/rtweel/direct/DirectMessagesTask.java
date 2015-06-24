package com.rtweel.direct;

import android.os.AsyncTask;

import com.rtweel.storage.Tweets;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class DirectMessagesTask extends AsyncTask<Integer, Void, Void> {

    private final static int MESSAGES_COUNT = 100;

    private DirectMessagesMainFragment mFragment;

    public DirectMessagesTask(DirectMessagesMainFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        ResponseList<DirectMessage> receivedMessages;
        ResponseList<DirectMessage> sentMessages;

        int pageNumberReceived = params[0];
        int pageNumberSent = params[1];

        Paging pageReceived = new Paging();
        pageReceived.setCount(MESSAGES_COUNT);
        pageReceived.setPage(pageNumberReceived);

        Paging pageSent = new Paging();
        pageSent.setCount(MESSAGES_COUNT);
        pageSent.setPage(pageNumberSent);


        try {
            receivedMessages = twitter.getDirectMessages(pageReceived);
            processReceivedMessages(receivedMessages);
            sentMessages = twitter.getSentDirectMessages(pageSent);
            processSentMessages(sentMessages);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        mFragment.setPageReceived(++pageNumberReceived);
        mFragment.setPageSent(++pageNumberSent);

        return null;
    }

    private void processReceivedMessages(ResponseList<DirectMessage> receivedMessages) {

        ArrayList<DirectUser> users = mFragment.getUsers();
        for (DirectMessage message : receivedMessages) {
            User sender = message.getSender();

            addMessage(sender, message, users, true);
        }

    }

    private void processSentMessages(ResponseList<DirectMessage> sentMessages) {

        ArrayList<DirectUser> users = mFragment.getUsers();

        for (DirectMessage message : sentMessages) {
            User recipient = message.getRecipient();

            addMessage(recipient, message, users, false);
        }
    }


    private void addMessage(User user, DirectMessage message, ArrayList<DirectUser> users, boolean isReceived) {
        for (DirectUser dUser : users) {
            if (dUser.user.equals(user)) {

                if (isReceived && !dUser.sentMessages.contains(message))
                    dUser.sentMessages.add(message);
                else if (!isReceived && !dUser.receivedMessages.contains(message))
                    dUser.receivedMessages.add(message);

                return;
            }
        }

        DirectUser newUser = new DirectUser(user);
        if (isReceived)
            newUser.sentMessages.add(message);
        else
            newUser.receivedMessages.add(message);

        users.add(newUser);
    }

    @Override
    protected void onPostExecute(Void result) {
        mFragment.getAdapter().notifyDataSetChanged();

        mFragment.stopAnim();
    }
}
