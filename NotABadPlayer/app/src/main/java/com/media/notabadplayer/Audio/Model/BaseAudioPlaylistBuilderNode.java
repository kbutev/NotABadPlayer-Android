package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Constants.AppSettings;

import java.util.List;

public interface BaseAudioPlaylistBuilderNode {
    @NonNull BaseAudioPlaylist build() throws Exception;
    @NonNull MutableAudioPlaylist buildMutable() throws Exception;

    void setName(@NonNull String name);
    void setTracks(@NonNull List<BaseAudioTrack> tracks);
    void setTracksToOneTrack(@NonNull BaseAudioTrack singleTrack);

    void setPlayingTrack(@NonNull BaseAudioTrack playingTrack);
    void setPlayingTrackPosition(int trackIndex);
    
    void setIsTemporaryPlaylist(boolean temporary);
}
