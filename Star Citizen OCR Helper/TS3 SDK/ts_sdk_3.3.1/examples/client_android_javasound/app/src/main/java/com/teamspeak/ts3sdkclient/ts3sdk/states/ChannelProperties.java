package com.teamspeak.ts3sdkclient.ts3sdk.states;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ChannelProperties.CHANNEL_NAME, ChannelProperties.CHANNEL_TOPIC, ChannelProperties.CHANNEL_DESCRIPTION,
        ChannelProperties.CHANNEL_PASSWORD, ChannelProperties.CHANNEL_CODEC, ChannelProperties.CHANNEL_CODEC_QUALITY,
        ChannelProperties.CHANNEL_MAXCLIENTS, ChannelProperties.CHANNEL_MAXFAMILYCLIENTS, ChannelProperties.CHANNEL_ORDER,
        ChannelProperties.CHANNEL_FLAG_PERMANENT, ChannelProperties.CHANNEL_FLAG_SEMI_PERMANENT, ChannelProperties.CHANNEL_FLAG_DEFAULT,
        ChannelProperties.CHANNEL_FLAG_PASSWORD, ChannelProperties.CHANNEL_CODEC_LATENCY_FACTOR, ChannelProperties.CHANNEL_CODEC_IS_UNENCRYPTED,
        ChannelProperties.CHANNEL_SECURITY_SALT, ChannelProperties.CHANNEL_DELETE_DELAY, ChannelProperties.CHANNEL_DUMMY_2,
        ChannelProperties.CHANNEL_DUMMY_3, ChannelProperties.CHANNEL_DUMMY_4, ChannelProperties.CHANNEL_DUMMY_5,
        ChannelProperties.CHANNEL_DUMMY_6, ChannelProperties.CHANNEL_DUMMY_7, ChannelProperties.CHANNEL_FLAG_MAXCLIENTS_UNLIMITED,
        ChannelProperties.CHANNEL_FLAG_MAXFAMILYCLIENTS_UNLIMITED, ChannelProperties.CHANNEL_FLAG_MAXFAMILYCLIENTS_INHERITED, ChannelProperties.CHANNEL_FLAG_ARE_SUBSCRIBED,
        ChannelProperties.CHANNEL_FILEPATH, ChannelProperties.CHANNEL_NEEDED_TALK_POWER, ChannelProperties.CHANNEL_FORCED_SILENCE,
        ChannelProperties.CHANNEL_NAME_PHONETIC, ChannelProperties.CHANNEL_ICON_ID, ChannelProperties.CHANNEL_FLAG_PRIVATE,
        ChannelProperties.CHANNEL_ENDMARKER_RARE})

public @interface ChannelProperties {
        int CHANNEL_NAME = 0;                             // Available for all channels that are "in view", always up-to-date
        int CHANNEL_TOPIC = 1;                            // Available for all channels that are "in view", always up-to-date
        int CHANNEL_DESCRIPTION = 2;                      // Must be requested (=>requestChannelDescription)
        int CHANNEL_PASSWORD = 3;                         // not available client side
        int CHANNEL_CODEC = 4;                            // Available for all channels that are "in view", always up-to-date
        int CHANNEL_CODEC_QUALITY = 5;                    // Available for all channels that are "in view", always up-to-date
        int CHANNEL_MAXCLIENTS = 6;                       // Available for all channels that are "in view", always up-to-date
        int CHANNEL_MAXFAMILYCLIENTS = 7;                 // Available for all channels that are "in view", always up-to-date
        int CHANNEL_ORDER = 8;                            // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_PERMANENT = 9;                   // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_SEMI_PERMANENT = 10;             // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_DEFAULT = 11;                    // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_PASSWORD = 12;                   // Available for all channels that are "in view", always up-to-date
        int CHANNEL_CODEC_LATENCY_FACTOR = 13;            // Available for all channels that are "in view", always up-to-date
        int CHANNEL_CODEC_IS_UNENCRYPTED = 14;            // Available for all channels that are "in view", always up-to-date
        int CHANNEL_SECURITY_SALT = 15;                   // sdk
        int CHANNEL_DELETE_DELAY = 16;                    // How many seconds to wait before deleting this channel
        int CHANNEL_DUMMY_2 = 17;
        int CHANNEL_DUMMY_3 = 18;
        int CHANNEL_DUMMY_4 = 19;
        int CHANNEL_DUMMY_5 = 20;
        int CHANNEL_DUMMY_6 = 21;
        int CHANNEL_DUMMY_7 = 22;
        int CHANNEL_FLAG_MAXCLIENTS_UNLIMITED = 23;       // Available always up-to-date
        int CHANNEL_FLAG_MAXFAMILYCLIENTS_UNLIMITED = 24; // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_MAXFAMILYCLIENTS_INHERITED = 25; // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_ARE_SUBSCRIBED = 26;             // Only available client side, stores whether we are subscribed to this channel
        int CHANNEL_FILEPATH = 27;                        // not available client side, the folder used for file-transfers for this channel
        int CHANNEL_NEEDED_TALK_POWER = 28;               // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FORCED_SILENCE = 29;                  // Available for all channels that are "in view", always up-to-date
        int CHANNEL_NAME_PHONETIC = 30;                   // Available for all channels that are "in view", always up-to-date
        int CHANNEL_ICON_ID = 31;                         // Available for all channels that are "in view", always up-to-date
        int CHANNEL_FLAG_PRIVATE = 32;                    // Available for all channels that are "in view", always up-to-date
        int CHANNEL_ENDMARKER_RARE = 33;
    }