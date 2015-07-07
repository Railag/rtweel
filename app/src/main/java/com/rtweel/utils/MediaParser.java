package com.rtweel.utils;

import android.text.TextUtils;

/**
 * Created by firrael on 24.4.15.
 */
public class MediaParser {

    private static String ARRAY_DIVIDER = "#media#";

    public static String serialize(String content[]){
        return TextUtils.join(ARRAY_DIVIDER, content);
    }

    public static String[] deserialize(String content){
        return content.split(ARRAY_DIVIDER);
    }
}
