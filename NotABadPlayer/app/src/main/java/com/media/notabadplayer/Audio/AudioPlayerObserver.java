package com.media.notabadplayer.Audio;

public interface AudioPlayerObserver {
    void onPlayerPlay(AudioTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause();
    void onPlayerResume();
    void onPlayerVolumeChanged();
}
