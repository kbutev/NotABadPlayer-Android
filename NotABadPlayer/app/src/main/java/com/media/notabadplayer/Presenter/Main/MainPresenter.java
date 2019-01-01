package com.media.notabadplayer.Presenter.Main;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class MainPresenter implements BasePresenter, AudioPlayerObserver {
    private BaseView _view;
    
    public MainPresenter(BaseView view) {
        _view = view;
    }
    
    public void start()
    {
        AudioPlayer.getShared().attachObserver(this);
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
    public void onPlay(AudioTrack current)
    {
        _view.onPlayerPlay(current);
    }
    
    @Override
    public void onStop()
    {
        _view.onPlayerStop();
    }
    
    @Override
    public void onPause()
    {
        _view.onPlayerPause();
    }
    
    @Override
    public void onResume()
    {
        _view.onPlayerResume();
    }
    
    @Override
    public void onVolumeChanged()
    {
        _view.onPlayerVolumeChanged();
    }
}
