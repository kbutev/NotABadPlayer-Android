package com.media.notabadplayer.Audio.Other;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Utilities.TimeValue;

public enum AudioPlayerTimerValue {
    NONE,
    MIN_30,
    MIN_60,
    MIN_90,
    HR_2,
    HR_3,
    HR_4,
    HR_5,
    HR_6,
    HR_10;

    public @NonNull TimeValue asTimeValue()
    {
        switch (this) {
            case NONE:
                return TimeValue.zero();
            case MIN_30:
                return TimeValue.fromMinutes(30);
            case MIN_60:
                return TimeValue.fromMinutes(60);
            case MIN_90:
                return TimeValue.fromMinutes(90);
            case HR_2:
                return TimeValue.fromMinutes(120);
            case HR_3:
                return TimeValue.fromHours(3);
            case HR_4:
                return TimeValue.fromHours(4);
            case HR_5:
                return TimeValue.fromHours(5);
            case HR_6:
                return TimeValue.fromHours(6);
            case HR_10:
                return TimeValue.fromHours(10);
        }

        return TimeValue.zero();
    }

    public String toString()
    {
        switch (this) {
            case NONE:
                return "None";
            case MIN_30:
                return "30min";
            case MIN_60:
                return "1:00hr";
            case MIN_90:
                return "1:30hr";
            case HR_2:
                return "2:00hr";
            case HR_3:
                return "3:00hr";
            case HR_4:
                return "4:00hr";
            case HR_5:
                return "5:00hr";
            case HR_6:
                return "6:00hr";
            case HR_10:
                return "10:00hr";
        }

        return "None";
    }
}
