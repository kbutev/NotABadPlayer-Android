package com.media.notabadplayer.Utilities;

import androidx.annotation.NonNull;

public class TimeValue {
    private final long _milliseconds;

    public static @NonNull TimeValue zero() {
        return new TimeValue(0);
    }

    public static @NonNull TimeValue fromMilliseconds(long ms) {
        return new TimeValue(ms);
    }

    public static @NonNull TimeValue fromSeconds(double seconds) {
        seconds *= 1000;
        long ms = (long)seconds;
        return new TimeValue(ms);
    }

    public static @NonNull TimeValue fromMinutes(double minutes) {
        minutes *= 60;
        minutes *= 1000;
        long ms = (long)minutes;
        return new TimeValue(ms);
    }

    public static @NonNull TimeValue fromHours(double hours) {
        hours *= 60;
        hours *= 60;
        hours *= 1000;
        long ms = (long)hours;
        return new TimeValue(ms);
    }

    public static @NonNull TimeValue fromDays(double days) {
        days *= 24;
        days *= 60;
        days *= 60;
        days *= 1000;
        long ms = (long)days;
        return new TimeValue(ms);
    }

    private TimeValue() {
        _milliseconds = 0;
    }

    private TimeValue(long ms) {
        _milliseconds = ms;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public @NonNull String toString(boolean showType) {
        double days = inDays();

        if (days >= 1) {
            return String.format("%.0f%s", days, showType ? " days" : "");
        }

        double hours = inHours();

        if (hours >= 1) {
            return String.format("%.1f%s", hours, showType ? " hours" : "");
        }

        double minutes = inMinutes();

        if (minutes >= 1) {
            return String.format("%.1f%s", minutes, showType ? " min" : "");
        }

        double seconds = inSeconds();

        if (seconds >= 1) {
            return String.format("%.1f%s", seconds, showType ? " sec" : "");
        }

        return String.format("%d%s", _milliseconds, showType ? " ms" : "");
    }

    public long inMilliseconds() {
        return _milliseconds;
    }

    public @NonNull String inMillisecondsAsString() {
        return String.format("%d", _milliseconds);
    }

    public double inSeconds() {
        double sec = _milliseconds;
        sec /= 1000.0;
        return sec;
    }

    public double inMinutes() {
        double min = _milliseconds;
        min /= 1000.0;
        min /= 60.0;
        return min;
    }

    public double inHours() {
        double hours = _milliseconds;
        hours /= 1000.0;
        hours /= 60.0;
        hours /= 60.0;
        return hours;
    }

    public double inDays() {
        double days = _milliseconds;
        days /= 1000.0;
        days /= 60.0;
        days /= 60.0;
        days /= 24.0;
        return days;
    }
}