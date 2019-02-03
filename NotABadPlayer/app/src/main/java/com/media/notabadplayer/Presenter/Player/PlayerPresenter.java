package com.media.notabadplayer.Presenter.Player;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.MediaPlayerPlaylist;
import com.media.notabadplayer.Audio.MediaTrack;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerPresenter implements BasePresenter
{
    private BaseView _view;
    private Application _application;
    private MediaPlayerPlaylist _playlist;
    
    public PlayerPresenter(BaseView view, Application application, @Nullable MediaPlayerPlaylist playlist)
    {
        _view = view;
        _application = application;
        _playlist = playlist;
    }
    
    @Override
    public void start() 
    {
        MediaPlayerPlaylist currentPlaylist = AudioPlayer.getShared().getPlaylist();
        MediaTrack currentPlayingTrack = currentPlaylist != null ? currentPlaylist.getPlayingTrack() : null;
        
        if (currentPlayingTrack == null || !_playlist.getPlayingTrack().title.equals(currentPlayingTrack.title))
        {
            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing playlist with " + String.valueOf(_playlist.size()) + " tracks");
            
            AudioPlayer.getShared().playPlaylist(_application, _playlist);
            
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
}
