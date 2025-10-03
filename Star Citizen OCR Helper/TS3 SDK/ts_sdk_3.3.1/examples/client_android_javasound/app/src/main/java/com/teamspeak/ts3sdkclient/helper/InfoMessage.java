package com.teamspeak.ts3sdkclient.helper;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 09.02.17
 *
 * Simple class to hold information data
 */
public class InfoMessage {
    private String message;
    private int color;
    private Date timeStamp;

    public InfoMessage(String message, int color) {
        this.message = message;
        this.color = color;
        timeStamp = new Date();
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public int getColor() {
        return color;
    }

    public String getTimeStampString(Context context){
        return new SimpleDateFormat("HH:mm:ss.SSS", context.getResources().getConfiguration().locale)
                .format(timeStamp);
    }
}
