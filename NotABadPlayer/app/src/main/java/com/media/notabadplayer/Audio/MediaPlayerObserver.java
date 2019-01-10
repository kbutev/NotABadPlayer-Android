package com.media.notabadplayer.Audio;

public interface MediaPlayerObserver {
    void onPlayerPlay(MediaTrack current);
    void onPlayerFinish(MediaTrack track);
    void onPlayerStop();
    void onPlayerPause(MediaTrack track);
    void onPlayerResume(MediaTrack track);
    void onPlayerVolumeChanged();
}
