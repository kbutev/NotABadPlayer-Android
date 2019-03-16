package com.media.notabadplayer.View;

import com.media.notabadplayer.Constants.AppSettings;

public interface BasePresenter {
    void start();
    
    void onAlbumClick(int index);
    void onAlbumsItemClick(int index);
    
    void onSearchResultClick(int index);
    void onSearchQuery(String searchValue);

    void onAppSettingsReset();
    void onAppThemeChange(AppSettings.AppTheme themeValue);
    void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting);
    void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar);
    void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input);
}
