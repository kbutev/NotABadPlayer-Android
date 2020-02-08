package com.media.notabadplayer.View.Playlist;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

interface PlaylistListFavoritesChecker {
    boolean isMarkedFavorite(@NonNull BaseAudioTrack track);
}
