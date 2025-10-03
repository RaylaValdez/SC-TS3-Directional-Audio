package com.teamspeak.ts3sdkclient.ts3sdk.states;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TalkStatus.STATUS_NOT_TALKING, TalkStatus.STATUS_TALKING, TalkStatus.STATUS_TALKING_WHILE_DISABLED})
public @interface TalkStatus {
    int STATUS_NOT_TALKING = 0;
    int STATUS_TALKING = 1;
    int STATUS_TALKING_WHILE_DISABLED = 2;
}
