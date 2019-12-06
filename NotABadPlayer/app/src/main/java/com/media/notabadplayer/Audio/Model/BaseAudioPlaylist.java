package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import java.io.Serializable;
import java.util.List;

import com.media.notabadplayer.Constants.AppSettings;

public interface BaseAudioPlaylist extends Serializable {
    @NonNull String getName();
    int size();

    boolean isPlaying();

    @NonNull List<BaseAudioTrack> getTracks();
    @NonNull BaseAudioTrack getTrack(int index);
    boolean hasTrack(@NonNull BaseAudioTrack track);
    int getPlayingTrackIndex();
    @NonNull BaseAudioTrack getPlayingTrack();
    
    boolean isPlayingFirstTrack();
    boolean isPlayingLastTrack();

    boolean isAlbum();
    boolean isTemporary();

    @NonNull BaseAudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting);
}
