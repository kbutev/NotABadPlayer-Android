package com.media.notabadplayer.View.Playlist;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.MVP.BaseView;

public interface PlaylistView extends BaseView {
    void onPlaylistLoad(@NonNull BaseAudioPlaylist playlist);

    void openPlayerScreen(@NonNull BaseAudioPlaylist playlist);
    void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist);

    void onPlayerErrorEncountered(@NonNull Exception error);
}
