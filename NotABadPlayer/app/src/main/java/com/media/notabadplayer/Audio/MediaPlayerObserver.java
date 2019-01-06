package com.media.notabadplayer.Audio;

public interface MediaPlayerObserver {
    void onPlayerPlay(MediaTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause();
    void onPlayerResume();
    void onPlayerVolumeChanged();
}
