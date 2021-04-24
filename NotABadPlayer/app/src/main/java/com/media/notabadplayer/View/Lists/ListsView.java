package com.media.notabadplayer.View.Lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.MVP.BaseRootView;

import java.util.List;

public interface ListsView extends BaseRootView {
    void onUserPlaylistsLoad(@NonNull List<BaseAudioPlaylist> playlists);

    void openCreatePlaylistScreen(@Nullable BaseAudioPlaylist playlistToEdit);

    void onFetchDataErrorEncountered(@NonNull Exception error);
    void onPlayerErrorEncountered(@NonNull Exception error);
}
