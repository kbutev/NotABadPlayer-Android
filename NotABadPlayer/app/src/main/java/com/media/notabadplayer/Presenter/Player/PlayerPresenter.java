package com.media.notabadplayer.Presenter.Player;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Player.PlayerView;

public interface PlayerPresenter extends BasePresenter {
    void setView(@NonNull PlayerView view);

    void onPlayerButtonClick(ApplicationInput input);
    void onPlayOrderButtonClick();
    void onPlayerVolumeSet(double value);

    boolean onMarkOrUnmarkContextTrackFavorite();
}
