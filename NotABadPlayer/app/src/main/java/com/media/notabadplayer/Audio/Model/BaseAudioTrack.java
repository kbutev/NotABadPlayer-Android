package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import java.io.Serializable;

public interface BaseAudioTrack extends Serializable {
    @NonNull String getFilePath();

    @NonNull String getTitle();
    @NonNull String getArtist();
    @NonNull String getAlbumTitle();
    @NonNull String getAlbumID();
    @NonNull String getArtCover();
    int getTrackNum();

    double getDurationInSeconds();
    @NonNull String getDuration();
    @NonNull AudioTrackSource getSource();

    @NonNull String getLyrics();

    int getNumberOfTimesPlayed();

    @NonNull BaseAudioTrackDate getDate();

    double getLastPlayedPosition();
}
