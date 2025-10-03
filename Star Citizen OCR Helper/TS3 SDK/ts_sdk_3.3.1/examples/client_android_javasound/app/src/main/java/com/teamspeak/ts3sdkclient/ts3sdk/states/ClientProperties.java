package com.teamspeak.ts3sdkclient.ts3sdk.states;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        ClientProperties.CLIENT_UNIQUE_IDENTIFIER, ClientProperties.CLIENT_NICKNAME, ClientProperties.CLIENT_VERSION,
        ClientProperties.CLIENT_PLATFORM, ClientProperties.CLIENT_FLAG_TALKING, ClientProperties.CLIENT_INPUT_MUTED,
        ClientProperties.CLIENT_OUTPUT_MUTED, ClientProperties.CLIENT_OUTPUTONLY_MUTED, ClientProperties.CLIENT_INPUT_HARDWARE,
        ClientProperties.CLIENT_OUTPUT_HARDWARE, ClientProperties.CLIENT_INPUT_DEACTIVATED, ClientProperties.CLIENT_IDLE_TIME,
        ClientProperties.CLIENT_DEFAULT_CHANNEL, ClientProperties.CLIENT_DEFAULT_CHANNEL_PASSWORD, ClientProperties.CLIENT_SERVER_PASSWORD,
        ClientProperties.CLIENT_META_DATA, ClientProperties.CLIENT_IS_MUTED, ClientProperties.CLIENT_IS_RECORDING,
        ClientProperties.CLIENT_VOLUME_MODIFICATOR, ClientProperties.CLIENT_VERSION_SIGN, ClientProperties.CLIENT_SECURITY_HASH,
        ClientProperties.CLIENT_DUMMY_3, ClientProperties.CLIENT_DUMMY_4, ClientProperties.CLIENT_DUMMY_5,
        ClientProperties.CLIENT_DUMMY_6, ClientProperties.CLIENT_DUMMY_7, ClientProperties.CLIENT_DUMMY_8,
        ClientProperties.CLIENT_DUMMY_9, ClientProperties.CLIENT_KEY_OFFSET, ClientProperties.CLIENT_LAST_VAR_REQUEST,
        ClientProperties.CLIENT_LOGIN_NAME, ClientProperties.CLIENT_LOGIN_PASSWORD, ClientProperties.CLIENT_DATABASE_ID,
        ClientProperties.CLIENT_CHANNEL_GROUP_ID, ClientProperties.CLIENT_SERVERGROUPS, ClientProperties.CLIENT_CREATED,
        ClientProperties.CLIENT_LASTCONNECTED, ClientProperties.CLIENT_TOTALCONNECTIONS, ClientProperties.CLIENT_AWAY,
        ClientProperties.CLIENT_AWAY_MESSAGE, ClientProperties.CLIENT_TYPE, ClientProperties.CLIENT_FLAG_AVATAR,
        ClientProperties.CLIENT_TALK_POWER, ClientProperties.CLIENT_TALK_REQUEST, ClientProperties.CLIENT_TALK_REQUEST_MSG,
        ClientProperties.CLIENT_DESCRIPTION, ClientProperties.CLIENT_IS_TALKER, ClientProperties.CLIENT_MONTH_BYTES_UPLOADED,
        ClientProperties.CLIENT_MONTH_BYTES_DOWNLOADED, ClientProperties.CLIENT_TOTAL_BYTES_UPLOADED, ClientProperties.CLIENT_TOTAL_BYTES_DOWNLOADED,
        ClientProperties.CLIENT_IS_PRIORITY_SPEAKER, ClientProperties.CLIENT_UNREAD_MESSAGES, ClientProperties.CLIENT_NICKNAME_PHONETIC,
        ClientProperties.CLIENT_NEEDED_SERVERQUERY_VIEW_POWER, ClientProperties.CLIENT_DEFAULT_TOKEN, ClientProperties.CLIENT_ICON_ID,
        ClientProperties.CLIENT_IS_CHANNEL_COMMANDER, ClientProperties.CLIENT_COUNTRY, ClientProperties.CLIENT_CHANNEL_GROUP_INHERITED_CHANNEL_ID,
        ClientProperties.CLIENT_BADGES, ClientProperties.CLIENT_ENDMARKER_RARE})

public @interface ClientProperties {
        int CLIENT_UNIQUE_IDENTIFIER = 0;                   // automatically up-to-date for any client "in view", can be used to identify this particular client installation
        int CLIENT_NICKNAME = 1;                            // automatically up-to-date for any client "in view"
        int CLIENT_VERSION = 2;                             // for other clients than ourself, this needs to be requested (=> requestClientVariables)
        int CLIENT_PLATFORM = 3;                            // for other clients than ourself, this needs to be requested (=> requestClientVariables)
        int CLIENT_FLAG_TALKING = 4;                        // automatically up-to-date for any client that can be heard (in room / whisper)
        int CLIENT_INPUT_MUTED = 5;                         // automatically up-to-date for any client "in view", this clients microphone mute status
        int CLIENT_OUTPUT_MUTED = 6;                        // automatically up-to-date for any client "in view", this clients headphones/speakers mute status
        int CLIENT_OUTPUTONLY_MUTED = 7;                    // automatically up-to-date for any client "in view", this clients headphones/speakers only mute status
        int CLIENT_INPUT_HARDWARE = 8;                      // automatically up-to-date for any client "in view", this clients microphone hardware status (is the capture device opened?)
        int CLIENT_OUTPUT_HARDWARE = 9;                     // automatically up-to-date for any client "in view", this clients headphone/speakers hardware status (is the playback device opened?)
        int CLIENT_INPUT_DEACTIVATED = 10;                  // only usable for ourself, not propagated to the network
        int CLIENT_IDLE_TIME = 11;                          // internal use
        int CLIENT_DEFAULT_CHANNEL = 12;                    // only usable for ourself, the default channel we used to connect on our last connection attempt
        int CLIENT_DEFAULT_CHANNEL_PASSWORD = 13;           // internal use
        int CLIENT_SERVER_PASSWORD = 14;                    // internal use
        int CLIENT_META_DATA = 15;                          // automatically up-to-date for any client "in view", not used by TeamSpeak, free storage for sdk users
        int CLIENT_IS_MUTED = 16;                           // only make sense on the client side locally, "1" if this client is´currently muted by us, "0" if he is not
        int CLIENT_IS_RECORDING = 17;                       // automatically up-to-date for any client "in view"
        int CLIENT_VOLUME_MODIFICATOR = 18;                 // internal use
        int CLIENT_VERSION_SIGN = 19;                       // sign
        int CLIENT_SECURITY_HASH = 20;                      // SDK use
        int CLIENT_DUMMY_3 = 21;
        int CLIENT_DUMMY_4 = 22;
        int CLIENT_DUMMY_5 = 23;
        int CLIENT_DUMMY_6 = 24;
        int CLIENT_DUMMY_7 = 25;
        int CLIENT_DUMMY_8 = 26;
        int CLIENT_DUMMY_9 = 27;
        int CLIENT_KEY_OFFSET = 28;                         // internal use
        int CLIENT_LAST_VAR_REQUEST = 29;                   // internal use
        int CLIENT_LOGIN_NAME = 30;                         // used for serverquery clients, makes no sense on normal clients currently
        int CLIENT_LOGIN_PASSWORD = 31;                     // used for serverquery clients, makes no sense on normal clients currently
        int CLIENT_DATABASE_ID = 32;                        // automatically up-to-date for any client "in view", only valid with PERMISSION feature, holds database client id
        int CLIENT_CHANNEL_GROUP_ID = 33;                   // automatically up-to-date for any client "in view", only valid with PERMISSION feature, holds database client id
        int CLIENT_SERVERGROUPS = 34;                       // automatically up-to-date for any client "in view", only valid with PERMISSION feature, holds all servergroups client belongs too
        int CLIENT_CREATED = 35;                            // this needs to be requested (=>requestClientVariables), first time this client connected to this server
        int CLIENT_LASTCONNECTED = 36;                      // this needs to be requested (=>requestClientVariables), last time this client connected to this server
        int CLIENT_TOTALCONNECTIONS = 37;                   // this needs to be requested (=>requestClientVariables), how many times this client connected to this server
        int CLIENT_AWAY = 38;                               // automatically up-to-date for any client "in view", this clients away status
        int CLIENT_AWAY_MESSAGE = 39;                       // automatically up-to-date for any client "in view", this clients away message
        int CLIENT_TYPE = 40;                               // automatically up-to-date for any client "in view", determines if this is a real client or a server-query connection
        int CLIENT_FLAG_AVATAR = 41;                        // automatically up-to-date for any client "in view", this client got an avatar
        int CLIENT_TALK_POWER = 42;                         // automatically up-to-date for any client "in view", only valid with PERMISSION feature
        int CLIENT_TALK_REQUEST = 43;                       // automatically up-to-date for any client "in view", only valid with PERMISSION feature, holds timestamp where client requested to talk
        int CLIENT_TALK_REQUEST_MSG = 44;                   // automatically up-to-date for any client "in view", only valid with PERMISSION feature, holds matter for the request
        int CLIENT_DESCRIPTION = 45;                        // automatically up-to-date for any client  "in view"
        int CLIENT_IS_TALKER = 46;                          // automatically up-to-date for any client "in view"
        int CLIENT_MONTH_BYTES_UPLOADED = 47;               // this needs to be requested (=>requestClientVariables)
        int CLIENT_MONTH_BYTES_DOWNLOADED = 48;             // this needs to be requested (=>requestClientVariables)
        int CLIENT_TOTAL_BYTES_UPLOADED = 49;               // this needs to be requested (=>requestClientVariables)
        int CLIENT_TOTAL_BYTES_DOWNLOADED = 50;             // this needs to be requested (=>requestClientVariables)
        int CLIENT_IS_PRIORITY_SPEAKER = 51;                // automatically up-to-date for any client "in view"
        int CLIENT_UNREAD_MESSAGES =52;                     // automatically up-to-date for any client "in view"
        int CLIENT_NICKNAME_PHONETIC = 53;                  // automatically up-to-date for any client "in view"
        int CLIENT_NEEDED_SERVERQUERY_VIEW_POWER = 54;      // automatically up-to-date for any client "in view"
        int CLIENT_DEFAULT_TOKEN = 55;                      // only usable for ourself, the default token we used to connect on our last connection attempt
        int CLIENT_ICON_ID = 56;                            // automatically up-to-date for any client "in view"
        int CLIENT_IS_CHANNEL_COMMANDER = 57;               // automatically up-to-date for any client "in view"
        int CLIENT_COUNTRY = 58;                            // automatically up-to-date for any client "in view"
        int CLIENT_CHANNEL_GROUP_INHERITED_CHANNEL_ID = 59; // automatically up-to-date for any
        // client "in view", only valid with PERMISSION feature, contains channel_id where the channel_group_id is set from
        int CLIENT_BADGES = 60;                             // automatically up-to-date for any client "in view", stores icons for partner badges
        int CLIENT_ENDMARKER_RARE = 61;
}