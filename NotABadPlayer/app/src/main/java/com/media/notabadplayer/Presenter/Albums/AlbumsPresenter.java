package com.media.notabadplayer.Presenter.Albums;

import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class AlbumsPresenter implements BasePresenter {
    private BaseView _view;
    private AudioInfo _audioInfo;

    public AlbumsPresenter(BaseView view, AudioInfo audioInfo)
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
        String identifier = a.albumID;
        String title = a.albumTitle;
        String artist = a.albumArtist;
        String cover = a.albumCover;
        
        Log.v("AlbumsPresenter", "Open " + title + " album");
        _view.openAlbumScreen(_audioInfo, identifier, artist, title, cover);
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
    public void onAppThemeChange(int themeValue)
    {

    }
    
    @Override
    public void onAppSortingChange(int value)
    {

    }

    @Override
    public void onKeybindSelected(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }
}
