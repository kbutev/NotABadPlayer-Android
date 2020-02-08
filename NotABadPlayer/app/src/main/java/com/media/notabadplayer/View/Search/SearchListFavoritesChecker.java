package com.media.notabadplayer.View.Search;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

interface SearchListFavoritesChecker {
    boolean isMarkedFavorite(@NonNull BaseAudioTrack track);
}
