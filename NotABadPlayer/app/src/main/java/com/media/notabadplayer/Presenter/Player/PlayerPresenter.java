package com.media.notabadplayer.Presenter.Player;

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
    private Context _applicationContext;
    private final MediaTrack _track;
    
    public PlayerPresenter(BaseView view, Context applicationContext, @Nullable MediaTrack track)
    {
        _view = view;
        _applicationContext = applicationContext;
        _track = track;
    }
    
    @Override
    public void start() 
    {
        if (_track != null)
        {
            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing a song");
            
            MediaPlayerPlaylist playlist = new MediaPlayerPlaylist(_track);
            
            AudioPlayer.getShared().playPlaylist(_applicationContext, playlist);
            _view.openPlayerScreen(playlist.getPlayingTrack());
        }
        else
        {
            MediaPlayerPlaylist playlist = AudioPlayer.getShared().getPlaylist();
            
            if (playlist != null)
            {
                Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and continuing to listen to current song");
                _view.openPlayerScreen(playlist.getPlayingTrack());
            }
            else
            {
                Log.v(PlayerPresenter.class.getCanonicalName(), "Cannot open player and continuing to listen to current song - nothing is playing");
            }
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
