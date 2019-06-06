package com.media.notabadplayer.Audio;

public interface AudioPlayerObservers
{
    void attach(AudioPlayerObserver observer);
    void detach(AudioPlayerObserver observer);
}
