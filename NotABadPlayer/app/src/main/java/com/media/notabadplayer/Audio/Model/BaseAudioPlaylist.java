package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Constants.AppSettings;

import java.io.Serializable;
import java.util.ArrayList;

public interface BaseAudioPlaylist extends Serializable {
    @NonNull String getName();
    int size();

    boolean isPlaying();

    @NonNull ArrayList<BaseAudioTrack> getTracks();
    @NonNull BaseAudioTrack getTrack(int index);
    boolean hasTrack(@NonNull BaseAudioTrack track);
    @NonNull BaseAudioTrack getPlayingTrack();

    boolean isAlbumPlaylist();
    boolean isTemporaryPlaylist();
    boolean isPlayingFirstTrack();
    boolean isPlayingLastTrack();

    void playCurrent();
    void goToTrack(@NonNull BaseAudioTrack track);
    void goToTrackBasedOnPlayOrder(AudioPlayOrder playOrder);
    void goToNextPlayingTrack();
    void goToNextPlayingTrackRepeat();
    void goToPreviousPlayingTrack();
    void goToTrackByShuffle();

    @NonNull BaseAudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting);
}
