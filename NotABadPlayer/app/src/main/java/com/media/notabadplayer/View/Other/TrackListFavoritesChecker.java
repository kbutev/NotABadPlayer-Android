package com.media.notabadplayer.View.Other;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

public interface TrackListFavoritesChecker {
    boolean isMarkedFavorite(@NonNull BaseAudioTrack track);
}
