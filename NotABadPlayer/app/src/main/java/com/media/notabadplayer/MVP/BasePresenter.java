package com.media.notabadplayer.MVP;

import com.media.notabadplayer.Constants.AppState;

public interface BasePresenter {
    // Call this only once, as soon as the view is ready to use
    void start();

    // Call this once, when presenter will no longer be used anymore.
    // May be called without even calling start().
    void onDestroy();

    void onAppStateChange(AppState state);
}
