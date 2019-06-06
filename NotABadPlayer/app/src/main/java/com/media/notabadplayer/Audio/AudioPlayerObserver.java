package com.media.notabadplayer.Audio;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.AudioTrack;

public interface AudioPlayerObserver {
    void onPlayerPlay(AudioTrack current);
    void onPlayerFinish();
    void onPlayerStop();
    void onPlayerPause(AudioTrack track);
    void onPlayerResume(AudioTrack track);
    void onPlayOrderChange(AudioPlayOrder order);
}
