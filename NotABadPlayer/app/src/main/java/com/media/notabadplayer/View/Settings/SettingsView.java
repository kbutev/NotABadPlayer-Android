package com.media.notabadplayer.View.Settings;

import androidx.annotation.NonNull;

import com.media.notabadplayer.MVP.BaseRootView;

public interface SettingsView extends BaseRootView {
    void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage);

    void onFetchDataErrorEncountered(@NonNull Exception error);
    void onPlayerErrorEncountered(@NonNull Exception error);
}
