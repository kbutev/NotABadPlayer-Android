package com.media.notabadplayer.Presenter.Albums;

import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumPresenter implements BasePresenter {
    private BaseView _view;
    private AudioInfo _audioInfo;
    
    private final AudioAlbum _album;
    
    private ArrayList<AudioTrack> _songs = new ArrayList<>();
    
    public AlbumPresenter(BaseView view, AudioInfo audioInfo, AudioAlbum album)
    {
        _view = view;
        _audioInfo = audioInfo;
        _album = album;
    }

    @Override
    public void start()
    {
        _songs = _audioInfo.getAlbumTracks(_album);
        _view.onAlbumSongsLoad(_songs);
    }

    @Override
    public void onAlbumClick(int index) 
    {
        
    }

    @Override
    public void onAlbumsItemClick(int index)
    {
        // Index zero is the header - ignore
        if (index == 0)
        {
            return;
        }
        
        // Index greater than zero is an song track
        index--;
        
        AudioTrack clickedTrack = _songs.get(index);
        AudioPlaylist playlist = new AudioPlaylist(_songs, clickedTrack);
        
        Log.v("AlbumPresenter", "Play playlist with specific song " + clickedTrack.title);
        
        _view.openPlayerScreen(playlist);
    }

    @Override
    public void onSearchResultClick(int index)
    {

    }

    @Override
    public void onSearchQuery(String searchValue)
    {

    }
}
