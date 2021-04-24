package com.media.notabadplayer.View.Player;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.MVP.BaseRootView;

public interface QuickPlayerView extends BaseRootView {
    void openPlayerScreen(@NonNull BaseAudioPlaylist playlist);

    void onPlayerErrorEncountered(@NonNull Exception error);
}
