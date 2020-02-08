package com.media.notabadplayer.Presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.View.BaseView;

public interface BasePresenter {
    // Call this only once, before start().
    void setView(@NonNull BaseView view);
    
    // Call this only once, as soon as the view is ready to use
    void start();

    // Call this once, when presenter will no longer be used anymore.
    // May be called without even calling start().
    void onDestroy();
    
    void fetchData();
    
    void onAppStateChange(AppState state);
    
    void onAlbumItemClick(int index);
    void onPlaylistItemClick(int index);
    void onOpenPlayer(@Nullable BaseAudioPlaylist playlist);
    
    void onPlayerButtonClick(ApplicationInput input);
    void onPlayOrderButtonClick();
    void onOpenPlaylistButtonClick();
    void onPlayerVolumeSet(double value);
    boolean onMarkOrUnmarkContextTrackFavorite();
    
    void onPlaylistItemDelete(int index);
    
    void onSearchResultClick(int index);
    void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter);
    
    void onAppSettingsReset();
    void onAppThemeChange(AppSettings.AppTheme themeValue);
    void onAppTrackSortingChange(AppSettings.TrackSorting trackSorting);
    void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar);
    void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value);
    void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input);
}
