package com.media.notabadplayer.Presenter.Player;

import android.content.Context;

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
    
    public PlayerPresenter(BaseView view, Context applicationContext, MediaTrack track)
    {
        _view = view;
        _applicationContext = applicationContext;
        _track = track;
    }
    
    @Override
    public void start() 
    {
        AudioPlayer.getShared().playPlaylist(_applicationContext, new MediaPlayerPlaylist(_track));
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
