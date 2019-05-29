package com.media.notabadplayer.Presenter.Settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class SettingsPresenter implements BasePresenter 
{
    private @NonNull BaseView _view;
    private @NonNull BaseView _applicationRootView;

    private @NonNull AudioInfo _audioInfo;

    public SettingsPresenter(@NonNull BaseView view,
                             @NonNull BaseView applicationRootView,
                             @NonNull AudioInfo audioInfo)
    {
        _view = view;
        _applicationRootView = applicationRootView;

        _audioInfo = audioInfo;
    }
    
    @Override
    public void start() {
        
    }

    @Override
    public void onAlbumItemClick(int index) {

    }

    @Override
    public void onPlaylistItemClick(int index) {

    }

    @Override
    public void onOpenPlayer(@Nullable AudioPlaylist playlist)
    {
        if (playlist != null)
        {
            Log.v(SettingsPresenter.class.getCanonicalName(), "Open player screen with playlist " + playlist.getName());

            _view.openPlaylistScreen(_audioInfo, playlist);
        }
    }
    
    @Override
    public void onPlayerButtonClick(ApplicationInput input) 
    {
        
    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }
    
    @Override
    public void onPlayOrderButtonClick()
    {
        
    }

    @Override
    public void onSearchResultClick(int index) {

    }

    @Override
    public void onSearchQuery(@NonNull String searchValue) {

    }
    
    @Override
    public void onAppSettingsReset() 
    {
        GeneralStorage.getShared().resetDefaultSettingsValues();
        
        _view.appSettingsReset();
        _applicationRootView.appSettingsReset();

        AppSettings.AppTheme themeValue = GeneralStorage.getShared().getAppThemeValue();

        _view.appThemeChanged(themeValue);
        _applicationRootView.appThemeChanged(themeValue);
    }

    @Override
    public void onAppThemeChange(AppSettings.AppTheme themeValue) {
        if (themeValue == GeneralStorage.getShared().getAppThemeValue())
        {
            return;
        }
        
        GeneralStorage.getShared().saveAppThemeValue(themeValue);
        
        _view.appThemeChanged(themeValue);
        _applicationRootView.appThemeChanged(themeValue);
    }
    
    @Override
    public void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {
        GeneralStorage.getShared().saveAlbumSortingValue(albumSorting);
        GeneralStorage.getShared().saveTrackSortingValue(trackSorting);
        
        _view.appSortingChanged(albumSorting, trackSorting);
        _applicationRootView.appSortingChanged(albumSorting, trackSorting);
    }
    
    @Override
    public void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {
        GeneralStorage.getShared().saveShowStarsValue(showStars);
        GeneralStorage.getShared().saveShowVolumeBarValue(showVolumeBar);
        
        _view.appAppearanceChanged(showStars, showVolumeBar);
        _applicationRootView.appAppearanceChanged(showStars, showVolumeBar);
    }
    
    @Override
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {
        GeneralStorage.getShared().saveSettingsAction(input, action);
    }
}
