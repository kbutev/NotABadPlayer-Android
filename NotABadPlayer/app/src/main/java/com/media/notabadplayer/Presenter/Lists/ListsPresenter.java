package com.media.notabadplayer.Presenter.Lists;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Lists.ListsView;

public interface ListsPresenter extends BasePresenter {
    void setView(@NonNull ListsView view);

    void fetchData();

    void onPlaylistItemClick(int index);
    void onPlaylistItemEdit(int index);
    void onPlaylistItemDelete(int index);
}
