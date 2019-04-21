package com.media.notabadplayer.Presenter;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;

public interface BasePresenter {
    void start();
    
    void onAlbumClick(int index);
    void onAlbumsItemClick(int index);
    
    void onPlayerButtonClick(ApplicationInput input);
    void onPlayOrderButtonClick();
    void onOpenPlaylistButtonClick();
    
    void onSearchResultClick(int index);
    void onSearchQuery(@NonNull String searchValue);
    
    void onAppSettingsReset();
    void onAppThemeChange(AppSettings.AppTheme themeValue);
    void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting);
    void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar);
    void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input);
}
