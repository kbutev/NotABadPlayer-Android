package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.AudioTrack;

public interface AudioPlayerObserver {
    void onPlayerPlay(@NonNull AudioTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause(@NonNull AudioTrack track);
    void onPlayerResume(@NonNull AudioTrack track);
    void onPlayOrderChange(AudioPlayOrder order);
}
