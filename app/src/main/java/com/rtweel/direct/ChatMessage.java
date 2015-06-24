package com.rtweel.direct;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.rtweel.storage.AppUser;

import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;

/**
 * Created by firrael on 23.6.15.
 */
public class ChatMessage implements  Comparable<ChatMessage>, Parcelable {

    private DirectMessage message;

    public ChatMessage(DirectMessage message) {
        this.message = message;
    }

    public boolean isLeft(Context context) {
        return AppUser.getUserId(context) == message.getRecipientId();
    }

    public boolean hasMedia() {
        MediaEntity[] entities = message.getExtendedMediaEntities();
        MediaEntity[] mediaEntities = message.getMediaEntities();
        return entities.length > 0 || mediaEntities.length > 0;
    }

    public String getMediaUrl() {
        if (hasMedia())
            if (message.getExtendedMediaEntities().length > 0)
                return message.getExtendedMediaEntities()[0].getMediaURL();
            else
                return message.getMediaEntities()[0].getMediaURL();
        else
            return null;
    }

    @Override
    public int compareTo(@NonNull ChatMessage another) {
        Date leftDate = message.getCreatedAt();
        Date rightDate = another.getMessage().getCreatedAt();
        if (leftDate.equals(rightDate))
            return 0;
        if (leftDate.after(rightDate))
            return 1;
        else
            return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
    }

    public DirectMessage getMessage() {
        return message;
    }

    public String getText() {
        return message.getText();
    }

    public Date getCreatedAt() {
        return message.getCreatedAt();
    }

    public long getRecipientId() {
        return message.getRecipientId();
    }
}
