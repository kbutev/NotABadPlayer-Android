package com.media.notabadplayer.View.Other;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

public interface TrackListHighlightedChecker {
    boolean shouldBeHighlighted(@NonNull BaseAudioTrack track);
}
