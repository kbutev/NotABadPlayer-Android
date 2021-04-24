package com.media.notabadplayer.Presenter.Player;

import androidx.annotation.NonNull;

import com.media.notabadplayer.MVP.BasePlayerPresenter;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Player.QuickPlayerView;

public interface QuickPlayerPresenter extends BasePresenter, BasePlayerPresenter {
    void setView(@NonNull QuickPlayerView view);
}
