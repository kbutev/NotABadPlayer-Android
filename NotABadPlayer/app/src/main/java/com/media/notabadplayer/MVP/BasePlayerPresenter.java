package com.media.notabadplayer.MVP;

import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Controls.ApplicationInput;

public interface BasePlayerPresenter extends BasePresenter {
    void onOpenPlayer(@Nullable BaseAudioPlaylist playlist);

    void onPlayerButtonClick(ApplicationInput input);
    void onPlayOrderButtonClick();
    void onOpenPlaylistButtonClick();
}
