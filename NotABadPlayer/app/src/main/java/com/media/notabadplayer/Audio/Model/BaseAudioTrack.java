package com.media.notabadplayer.Audio.Model;

import androidx.annotation.NonNull;
import java.io.Serializable;

public interface BaseAudioTrack extends Serializable {
    @NonNull String getFilePath();

    @NonNull String getTitle();
    @NonNull String getArtist();
    @NonNull String getAlbumTitle();
    @NonNull String getAlbumID();
    @NonNull AudioArtCover getArtCover();
    int getTrackNum();

    double getDurationInSeconds();
    @NonNull String getDuration();
    
    // Describes the playlist of which this track is part of.
    @NonNull AudioTrackSource getSource();
    
    // If the track was moved from one playlist to another, it's @source value may have changed.
    // This value never changes.
    @NonNull AudioTrackSource getOriginalSource();

    @NonNull String getLyrics();

    int getNumberOfTimesPlayed();

    @NonNull AudioTrackDate getDate();

    double getLastPlayedPosition();
}
