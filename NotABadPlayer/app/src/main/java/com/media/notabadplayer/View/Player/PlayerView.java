package com.media.notabadplayer.View.Player;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.MVP.BaseView;

public interface PlayerView extends BaseView {
    void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist);

    void onPlayerErrorEncountered(@NonNull Exception error);
}
