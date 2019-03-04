package com.media.notabadplayer.Presenter.Player;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerPresenter implements BasePresenter
{
    private BaseView _view;
    private Application _application;
    private AudioPlaylist _playlist;
    
    public PlayerPresenter(BaseView view, Application application, @Nullable AudioPlaylist playlist)
    {
        _view = view;
        _application = application;
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

            player.playPlaylist(_application, _playlist);
            
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
    public void onAppThemeChange()
    {

    }

    @Override
    public void onKeybindSelected(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }
}
