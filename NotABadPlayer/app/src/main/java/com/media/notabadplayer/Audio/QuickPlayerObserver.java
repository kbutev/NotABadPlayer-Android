package com.media.notabadplayer.Audio;

// Note: Delegation is always performed on the main thread.
public interface QuickPlayerObserver extends AudioPlayerObserver {
    void updateTime(double currentTime, double totalDuration);
}
