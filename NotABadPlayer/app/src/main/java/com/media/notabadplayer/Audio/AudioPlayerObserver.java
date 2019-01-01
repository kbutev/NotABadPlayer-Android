package com.media.notabadplayer.Audio;

public interface AudioPlayerObserver {
    void onPlay(AudioTrack current);
    void onStop();
    void onPause();
    void onResume();
    void onVolumeChanged();
}
