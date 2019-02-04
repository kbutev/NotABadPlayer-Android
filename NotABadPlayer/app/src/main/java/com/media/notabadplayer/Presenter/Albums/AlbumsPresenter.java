package com.media.notabadplayer.Presenter.Albums;

import android.util.Log;

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class AlbumsPresenter implements BasePresenter {
    private BaseView _view;
    private MediaInfo _mediaInfo;

    public AlbumsPresenter(BaseView view, MediaInfo mediaInfo)
    {
        _view = view;
        _mediaInfo = mediaInfo;
    }
    
    @Override
    public void start()
    {
        _view.onMediaAlbumsLoad(_mediaInfo.getAlbums());
    }

    @Override
    public void onAlbumClick(int index) 
    {
        AlbumInfo a = _mediaInfo.getAlbums().get(index);
        String identifier = a.albumID;
        String title = a.albumTitle;
        String artist = a.albumArtist;
        String cover = a.albumCover;
        
        Log.v("AlbumsPresenter", "Open " + title + " album");
        _view.openAlbumScreen(_mediaInfo, identifier, artist, title, cover);
    }

    @Override
    public void onAlbumsItemClick(int index)
    {

    }
}
