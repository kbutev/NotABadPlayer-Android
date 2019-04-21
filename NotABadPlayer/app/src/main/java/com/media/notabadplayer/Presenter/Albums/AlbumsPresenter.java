package com.media.notabadplayer.Presenter.Albums;

import android.support.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class AlbumsPresenter implements BasePresenter {
    private @NonNull BaseView _view;
    private @NonNull AudioInfo _audioInfo;

    public AlbumsPresenter(@NonNull BaseView view, @NonNull AudioInfo audioInfo)
    {
        _view = view;
        _audioInfo = audioInfo;
    }
    
    @Override
    public void start()
    {
        _view.onMediaAlbumsLoad(_audioInfo.getAlbums());
    }

    @Override
    public void onAlbumClick(int index) 
    {
        AudioAlbum a = _audioInfo.getAlbums().get(index);
        
        Log.v(AlbumsPresenter.class.getCanonicalName(), "Open '" + a.albumTitle + "' album");
        _view.openPlaylistScreen(a);
    }

    @Override
    public void onAlbumsItemClick(int index)
    {

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
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }
}
