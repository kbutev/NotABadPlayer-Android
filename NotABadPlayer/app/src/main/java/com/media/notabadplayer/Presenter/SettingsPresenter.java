package com.media.notabadplayer.Presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class SettingsPresenter implements BasePresenter 
{
    private BaseView _view;

    private @NonNull AudioInfo _audioInfo;

    public SettingsPresenter(@NonNull AudioInfo audioInfo)
    {
        _audioInfo = audioInfo;
    }

    @Override
    public void setView(@NonNull BaseView view)
    {
        _view = view;
    }
    
    @Override
    public void start() {
        if (_view == null)
        {
            throw new IllegalStateException("SettingsPresenter: view has not been set");
        }
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

        AppSettings.AppTheme themeValue = GeneralStorage.getShared().getAppThemeValue();

        _view.appThemeChanged(themeValue);
    }

    @Override
    public void onAppThemeChange(AppSettings.AppTheme themeValue) {
        if (themeValue == GeneralStorage.getShared().getAppThemeValue())
        {
            return;
        }

        Log.v(SettingsPresenter.class.getCanonicalName(), "Save picked settings AppTheme value " + themeValue.name());
        
        GeneralStorage.getShared().saveAppThemeValue(themeValue);
        
        _view.appThemeChanged(themeValue);
    }
    
    @Override
    public void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {
        Log.v(SettingsPresenter.class.getCanonicalName(), "Save picked settings ShowVolumeBar value " + trackSorting.name());
        
        GeneralStorage.getShared().saveTrackSortingValue(trackSorting);
        
        _view.appTrackSortingChanged(trackSorting);
    }
    
    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {
        Log.v(SettingsPresenter.class.getCanonicalName(), "Save picked settings ShowVolumeBar value " + value.name());
        
        GeneralStorage.getShared().saveShowVolumeBarValue(value);
    }

    @Override
    public void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value)
    {
        Log.v(SettingsPresenter.class.getCanonicalName(), "Save picked settings OpenPlayerOnPlay value " + value.name());
        
        GeneralStorage.getShared().saveOpenPlayerOnPlayValue(value);
    }
    
    @Override
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {
        Log.v(SettingsPresenter.class.getCanonicalName(), "Save picked keybind value of action " + action.name() + " for input " + input.name());
        
        GeneralStorage.getShared().saveSettingsAction(input, action);
    }
}
