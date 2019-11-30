package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface BaseAudioTrack {
    @NonNull String getFilePath();

    @NonNull String getTitle();
    @NonNull String getArtist();
    @NonNull String getAlbumTitle();
    @NonNull String getAlbumID();
    @NonNull String getArtCover();
    @NonNull int getTrackNum();

    double getDurationInSeconds();
    @NonNull String getDuration();
    @NonNull AudioTrackSource getSource();

    @NonNull String getLyrics();

    int getNumberOfTimesPlayed();
    double getTotalTimePlayed();

    @NonNull BaseAudioTrackDate getDate();

    @NonNull String serialize() throws Exception;
}
