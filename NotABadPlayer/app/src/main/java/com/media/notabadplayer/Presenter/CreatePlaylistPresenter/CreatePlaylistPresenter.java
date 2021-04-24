package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Presenter.Search.SearchPresenter;

public interface CreatePlaylistPresenter extends SearchPresenter {
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

    // Search operations
    void onSearchResultClick(int index);
    void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter);
}
