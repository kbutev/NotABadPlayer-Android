package com.media.notabadplayer.Presenter.Settings;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Other.AudioPlayerTimerValue;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Settings.SettingsView;

public interface SettingsPresenter extends BasePresenter {
    void setView(@NonNull SettingsView view);

    void fetchData();

    void onAppSettingsReset();
    void onAppThemeChange(AppSettings.AppTheme themeValue);
    void onAppTrackSortingChange(AppSettings.TrackSorting trackSorting);
    void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value);
    void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value);
    void onKeybindChange(ApplicationAction action, ApplicationInput input);
    void onAudioIdleTimerValueChange(AudioPlayerTimerValue value);
}
