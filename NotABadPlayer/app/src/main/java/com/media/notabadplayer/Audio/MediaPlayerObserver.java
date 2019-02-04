package com.media.notabadplayer.Audio;

public interface MediaPlayerObserver {
    void onPlayerPlay(MediaTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause(MediaTrack track);
    void onPlayerResume(MediaTrack track);
    void onPlayerVolumeChanged();
}
