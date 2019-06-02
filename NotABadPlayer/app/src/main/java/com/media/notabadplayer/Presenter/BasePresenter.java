package com.media.notabadplayer.Presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.View.BaseView;

public interface BasePresenter {
    void setView(@NonNull BaseView view);
    
    void start();
    
    void onAppStateChange(AppState state);
    
    void onAlbumItemClick(int index);
    void onPlaylistItemClick(int index);
    void onOpenPlayer(@Nullable AudioPlaylist playlist);
    
    void onPlayerButtonClick(ApplicationInput input);
    void onPlayOrderButtonClick();
    void onOpenPlaylistButtonClick();
    
    void onSearchResultClick(int index);
    void onSearchQuery(@NonNull String searchValue);
    
    void onAppSettingsReset();
    void onAppThemeChange(AppSettings.AppTheme themeValue);
    void onAppTrackSortingChange(AppSettings.TrackSorting trackSorting);
    void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar);
    void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value);
    void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input);
}
