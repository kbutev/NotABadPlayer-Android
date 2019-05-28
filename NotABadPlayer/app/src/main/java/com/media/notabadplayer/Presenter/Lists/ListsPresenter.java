package com.media.notabadplayer.Presenter.Lists;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Storage.AudioStorage;
import com.media.notabadplayer.View.BaseView;

public class ListsPresenter implements BasePresenter
{
    private @NonNull BaseView _view;

    private @NonNull AudioInfo _audioInfo;

    public ListsPresenter(@NonNull BaseView view, @NonNull AudioInfo audioInfo)
    {
        _view = view;

        _audioInfo = audioInfo;
    }
    
    @Override
    public void start()
    {
        
    }

    @Override
    public void onAlbumItemClick(int index)
    {

    }

    @Override
    public void onPlaylistItemClick(int index) 
    {

    }

    @Override
    public void onOpenPlayer(@Nullable AudioPlaylist playlist)
    {
        if (playlist != null)
        {
            Log.v(ListsPresenter.class.getCanonicalName(), "Open player screen with playlist " + playlist.getName());

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
    public void onSearchResultClick(int index) 
    {

    }

    @Override
    public void onSearchQuery(@NonNull String searchValue) 
    {

    }

    @Override
    public void onAppSettingsReset()
    {

    }

    @Override
    public void onAppThemeChange(AppSettings.AppTheme themeValue)
    {

    }

    @Override
    public void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar) 
    {

    }

    @Override
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {

    }
}
