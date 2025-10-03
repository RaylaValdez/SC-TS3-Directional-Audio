package com.teamspeak.ts3sdkclient.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 09.02.17
 *
 * Simple class to collect {@link InfoMessage}
 */
public class Info {

    public interface OnInfoListener {
        void onUpdateGUI();
    }

    private List<InfoMessage> messages;
    private static Info instance;
    private OnInfoListener listener;

    private Info() {
        messages = new ArrayList<>();
    }

    public static Info getInstance() {
        if (instance == null)
            instance = new Info();
        return instance;
    }

    public void addMessage(String message, int color) {
        messages.add(0, new InfoMessage(message, color));
        if(listener != null)
            listener.onUpdateGUI();
    }

    public List<InfoMessage> getMessages() {
        return messages;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.listener = listener;
    }
}
