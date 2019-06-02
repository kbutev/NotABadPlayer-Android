package com.media.notabadplayer.Presenter;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.View.BaseView;

public class AlbumsPresenter implements BasePresenter {
    private BaseView _view;
    
    private @NonNull AudioInfo _audioInfo;
    
    private @NonNull List<AudioAlbum> _albums = new ArrayList<>();

    private boolean _running = false;
    
    public AlbumsPresenter(@NonNull AudioInfo audioInfo)
    {
        _audioInfo = audioInfo;
    }
    
    @Override
    public void setView(@NonNull BaseView view)
    {
        _view = view;
    }
    
    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("AlbumsPresenter: view has not been set");
        }

        Log.v(AlbumsPresenter.class.getCanonicalName(), "Starting... retrieving albums data...");
        
        // Use background thread to pull the albums data.
        // Then, alert view on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AudioAlbum> albums = _audioInfo.getAlbums();

                Handler mainHandler = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        _albums = albums;
                        
                        _view.onMediaAlbumsLoad(_albums);
                    }
                };

                mainHandler.post(myRunnable);
            }
        });

        thread.start();
    }
    
    @Override
    public void onAppStateChange(AppState state)
    {
        _running = state.isRunning();
    }

    @Override
    public void onAlbumItemClick(int index)
    {
        if (!_running)
        {
            return;
        }
        
        if (index < 0 || index >= _albums.size())
        {
            Log.v(AlbumsPresenter.class.getCanonicalName(), "Error: Invalid album list index, cannot respond to event properly");
            return;
        }

        AudioAlbum album = _albums.get(index);
        
        Log.v(AlbumsPresenter.class.getCanonicalName(), "Open '" + album.albumTitle + "' album");

        List<AudioTrack> tracks = _audioInfo.getAlbumTracks(album);

        AudioPlaylist playlist = new AudioPlaylist(album.albumTitle, tracks);

        _view.openPlaylistScreen(_audioInfo, playlist);
    }

    @Override
    public void onPlaylistItemClick(int index)
    {

    }

    @Override
    public void onOpenPlayer(@Nullable AudioPlaylist playlist)
    {
        
    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input)
    {

    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }

    @Override
    public void onPlayOrderButtonClick()
    {

    }

    @Override
    public void onPlaylistItemDelete(int index)
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
    public void onAppTrackSortingChange(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value)
    {

    }

    @Override
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }
}
