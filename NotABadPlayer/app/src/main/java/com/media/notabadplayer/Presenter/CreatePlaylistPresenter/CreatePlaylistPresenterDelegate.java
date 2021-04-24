package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.MVP.BaseView;
import com.media.notabadplayer.View.Lists.CreatePlaylistAlbumsAdapter;
import com.media.notabadplayer.View.Lists.CreatePlaylistTracksAdapter;
import com.media.notabadplayer.View.Search.SearchView;

import java.util.List;

public interface CreatePlaylistPresenterDelegate extends SearchView {
    void goBack();

    void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState);
    
    void updateAddedTracks(@NonNull CreatePlaylistTracksAdapter adapter);
    void updateAlbums(@NonNull CreatePlaylistAlbumsAdapter adapter);
    void refreshSearchTracks();
    
    // Album tracks operations
    @NonNull Function<Integer, Void> onAlbumTrackClick();

    // Search track operations
    void onSearchResultClick(int index);
    
    // Dialogue
    void showNoTracksDialog();
    void showInvalidNameDialog();
    void showNameTakenDialog();
    void showUnknownErrorDialog();
}
