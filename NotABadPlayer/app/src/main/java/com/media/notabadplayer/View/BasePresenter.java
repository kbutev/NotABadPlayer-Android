package com.media.notabadplayer.View;

public interface BasePresenter {
    void start();
    
    void onAlbumClick(int index);
    void onAlbumsItemClick(int index);
    
    void onSearchResultClick(int index);
    void onSearchQuery(String searchValue);

    void onAppThemeChange(int themeValue);
    void onAppSortingChange(int value);
    void onKeybindSelected(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input);
}
