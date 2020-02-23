package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.media.notabadplayer.View.Lists.CreatePlaylistAlbumsAdapter;
import com.media.notabadplayer.View.Lists.CreatePlaylistTracksAdapter;

public interface CreatePlaylistPresenterDelegate {
    void goBack();
    
    // Added tracks operations
    void updateAddedTracksDataSource(@NonNull CreatePlaylistTracksAdapter adapter);
    void updateAlbumsDataSource(@NonNull CreatePlaylistAlbumsAdapter adapter);

    @NonNull Function<Integer, Void> onAlbumTrackClick();

    void showNoTracksDialog();
    void showInvalidNameDialog();
    void showNameTakenDialog();
    void showUnknownErrorDialog();
}
