package com.media.notabadplayer.Presenter.Albums;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Albums.AlbumsView;

public interface AlbumsPresenter extends BasePresenter {
    void setView(@NonNull AlbumsView view);

    void fetchData();

    void onAlbumItemClick(int index);
}
