package com.media.notabadplayer.Presenter.Albums;

import android.util.Log;

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumPresenter implements BasePresenter {
    private BaseView _view;
    private MediaInfo _mediaInfo;
    
    private final AlbumInfo _album;
    
    private ArrayList<AudioTrack> _songs = new ArrayList<>();
    
    public AlbumPresenter(BaseView view, MediaInfo mediaInfo, AlbumInfo album)
    {
        _view = view;
        _mediaInfo = mediaInfo;
        _album = album;
    }

    @Override
    public void start()
    {
        _songs = _mediaInfo.getAlbumSongs(_album);
        _view.onAlbumSongsLoad(_songs);
    }

    @Override
    public void onAlbumClick(int index) 
    {
        
    }

    @Override
    public void onAlbumsItemClick(int index)
    {
        AudioTrack track = _songs.get(index);
        
        Log.v("AlbumPresenter", "Play song " + track.title);
        
        _view.startPlayer(track);
    }
}
