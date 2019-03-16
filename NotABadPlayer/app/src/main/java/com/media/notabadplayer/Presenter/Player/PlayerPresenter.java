package com.media.notabadplayer.Presenter.Player;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Storage.AudioInfo;
import com.media.notabadplayer.View.BaseView;

public class PlayerPresenter implements BasePresenter
{
    private BaseView _view;
    private AudioPlaylist _playlist;
    
    public PlayerPresenter(@NonNull BaseView view, 
                           @NonNull AudioPlaylist playlist)
    {
        _view = view;
        _playlist = playlist;
    }
    
    @Override
    public void start() 
    {
        AudioPlayer player = AudioPlayer.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();
        AudioTrack currentPlayingTrack = currentPlaylist != null ? currentPlaylist.getPlayingTrack() : null;
        
        if (!_playlist.getPlayingTrack().equals(currentPlayingTrack))
        {
            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing playlist with " + String.valueOf(_playlist.size()) + " tracks");

            player.playPlaylist(_playlist);
            
            if (!player.isPlaying())
            {
                player.resume();
            }
            
            _view.openPlayerScreen(_playlist);
        }
        else
        {
            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and continuing to listen to current song");
            
            _view.openPlayerScreen(currentPlaylist);
        }
    }
    
    @Override
    public void onAlbumClick(int index) 
    {
        
    }
    
    @Override
    public void onAlbumsItemClick(int index)
    {
        
    }

    @Override
    public void onSearchResultClick(int index)
    {

    }
    
    @Override
    public void onSearchQuery(String searchValue)
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
