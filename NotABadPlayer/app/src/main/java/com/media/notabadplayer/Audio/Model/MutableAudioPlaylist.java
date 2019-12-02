package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

public interface MutableAudioPlaylist extends BaseAudioPlaylist {
    void playCurrent();
    void goToTrack(@NonNull BaseAudioTrack track);
    void goToTrackAt(int trackIndex);
    void goToTrackBasedOnPlayOrder(AudioPlayOrder playOrder);
    void goToNextPlayingTrack();
    void goToNextPlayingTrackRepeat();
    void goToPreviousPlayingTrack();
    void goToTrackByShuffle();
}
