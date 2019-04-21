package com.media.notabadplayer.Presenter.Settings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class SettingsPresenter implements BasePresenter 
{
    @NonNull private BaseView _view;
    @NonNull private BaseView _applicationRootView;
    @NonNull private Context _context;

    public SettingsPresenter(@NonNull BaseView view, @NonNull BaseView applicationRootView, @NonNull Context context)
    {
        _view = view;
        _applicationRootView = applicationRootView;
        _context = context;
    }
    
    @Override
    public void start() {
        
    }

    @Override
    public void onAlbumClick(int index) {

    }

    @Override
    public void onAlbumsItemClick(int index) {

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
