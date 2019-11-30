package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

public interface AudioPlayerObserver {
    void onPlayerPlay(@NonNull BaseAudioTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause(@NonNull BaseAudioTrack track);
    void onPlayerResume(@NonNull BaseAudioTrack track);
    void onPlayOrderChange(AudioPlayOrder order);
}
