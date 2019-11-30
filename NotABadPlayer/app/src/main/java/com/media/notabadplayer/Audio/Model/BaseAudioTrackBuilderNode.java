package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

public interface BaseAudioTrackBuilderNode {
    @NonNull BaseAudioTrack build() throws Exception;

    void setFilePath(@NonNull String value);

    void setTitle(@NonNull String value);
    void setArtist(@NonNull String value);
    void setAlbumTitle(@NonNull String value);
    void setAlbumID(@NonNull String value);
    void setArtCover(@NonNull String value);
    void setTrackNum(int number);

    void setDuration(double duration);
    void setSource(AudioTrackSource source);

    void setLyrics(@NonNull String value);

    void setNumberOfTimesPlayed(int count);
    void setTotalTimePlayed(int count);

    void setDate(@NonNull BaseAudioTrackDate value);
}
