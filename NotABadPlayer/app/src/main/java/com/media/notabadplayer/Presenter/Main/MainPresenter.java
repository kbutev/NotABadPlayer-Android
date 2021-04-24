package com.media.notabadplayer.Presenter.Main;

import androidx.annotation.NonNull;

import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.MVP.BaseRootView;

public interface MainPresenter extends BasePresenter {
    void setView(@NonNull BaseRootView view);
}
