package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import androidx.annotation.NonNull;

import com.google.common.base.Function;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Lists.CreatePlaylistAlbumsAdapter;
import com.media.notabadplayer.View.Lists.CreatePlaylistTracksAdapter;

public interface CreatePlaylistPresenterDelegate extends BaseView {
    void goBack();
    
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
