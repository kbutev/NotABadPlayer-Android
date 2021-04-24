package com.media.notabadplayer.Presenter.Playlist;

import androidx.annotation.NonNull;

import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Playlist.PlaylistView;

public interface PlaylistPresenter extends BasePresenter {
    void setView(@NonNull PlaylistView view);

    void onPlaylistItemClick(int index);
}
