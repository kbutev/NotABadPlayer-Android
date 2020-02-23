package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Presenter.BasePresenter;

public interface BaseCreatePlaylistPresenter extends BasePresenter {
    void onPlaylistNameChanged(@NonNull String name);
    void onSaveUserPlaylist();

    // Added tracks operations
    boolean isTrackAdded(@NonNull BaseAudioTrack track);
    void onAddedTrackClicked(int index);

    // Album track operations
    int getSelectedAlbumIndex();
    void onOpenAlbumAt(int index);
    void onCloseAlbum();
    void onAlbumTrackClick(int fromOpenedAlbumIndex);
}
