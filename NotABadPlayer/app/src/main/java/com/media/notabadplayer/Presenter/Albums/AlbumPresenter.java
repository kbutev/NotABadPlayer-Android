package com.media.notabadplayer.Presenter.Albums;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumPresenter implements BasePresenter {
    private @NonNull BaseView _view;
    
    private final @Nullable AudioAlbum _album;
    private final @Nullable AudioPlaylist _playlist;
    
    private @NonNull ArrayList<AudioTrack> _songs = new ArrayList<>();
    
    public AlbumPresenter(@NonNull BaseView view, @NonNull AudioAlbum album)
    {
        _view = view;
        _album = album;
        _playlist = null;
    }

    public AlbumPresenter(@NonNull BaseView view, @NonNull AudioPlaylist playlist)
    {
        _view = view;
        _album = null;
        _playlist = playlist;
    }

    @Override
    public void start()
    {
        if (_album != null)
        {
            _songs = AudioPlayer.getShared().getAudioInfo().getAlbumTracks(_album);
            _view.onAlbumSongsLoad(_songs);
        }
        else
        {
            _songs = _playlist.getTracks();
            _view.onPlaylistLoad(_playlist, false);
        }
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
        
        String playlistName = _album != null ? _album.albumTitle : _playlist.getName();
        
        // Index greater than zero is an song track
        index--;
        
        AudioTrack clickedTrack = _songs.get(index);
        AudioPlaylist playlist = new AudioPlaylist(playlistName, _songs, clickedTrack);
        
        Log.v(AlbumPresenter.class.getCanonicalName(), "Play playlist with specific song " + clickedTrack.title);
        
        _view.openPlayerScreen(playlist);
    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input, @NonNull Context context)
    {

    }

    @Override
    public void onOpenPlaylistButtonClick(@NonNull Context context)
    {

    }

    @Override
    public void onPlayOrderButtonClick(@NonNull Context context)
    {

    }

    @Override
    public void onSearchResultClick(int index)
    {

    }

    @Override
    public void onSearchQuery(@NonNull String searchValue)
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
