package com.media.notabadplayer.Audio;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

// Note: Delegation may be performed on a background thread.
public interface AudioPlayerObserver {
    void onPlayerPlay(@NonNull BaseAudioTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause(@NonNull BaseAudioTrack track);
    void onPlayerResume(@NonNull BaseAudioTrack track);
    void onPlayOrderChange(AudioPlayOrder order);
}
