package com.media.notabadplayer.Audio.Model;

import android.net.Uri;
import androidx.annotation.NonNull;

import java.util.Date;

public interface BaseAudioTrackBuilderNode {
    @NonNull BaseAudioTrack build() throws Exception;
    void reset();

    void setFilePath(@NonNull String value);

    void setTitle(@NonNull String value);
    void setArtist(@NonNull String value);
    void setAlbumTitle(@NonNull String value);
    void setAlbumID(@NonNull String value);
    void setArtCover(@NonNull AudioArtCover value);
    void setTrackNum(int number);

    void setDuration(double duration);
    void setSource(@NonNull AudioTrackSource source);

    void setLyrics(@NonNull String value);
    void setNumberOfTimesPlayed(int count);
    void setLastPlayedPosition(double position);
    void setDateAdded(@NonNull Date value);
    void setDateModified(@NonNull Date value);
    void setDateFirstPlayed(@NonNull Date value);
    void setDateLastPlayed(@NonNull Date value);
}
