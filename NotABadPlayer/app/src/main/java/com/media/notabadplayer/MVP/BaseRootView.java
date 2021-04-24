package com.media.notabadplayer.MVP;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Constants.AppSettings;

/*
 * Describes a view that can be a root view of the app.
 */
public interface BaseRootView extends BaseView {
    void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options) throws Exception;

    void onResetAppSettings();
    void onAppThemeChanged(AppSettings.AppTheme appTheme);
    void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting);
}
