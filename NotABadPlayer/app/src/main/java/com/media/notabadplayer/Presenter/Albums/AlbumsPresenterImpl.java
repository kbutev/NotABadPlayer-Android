package com.media.notabadplayer.Presenter.Albums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Utilities.ListAlphabet;
import com.media.notabadplayer.View.Albums.AlbumsView;

public class AlbumsPresenterImpl implements AlbumsPresenter, AudioLibrary.ChangesListener {
    private AlbumsView _view;
    
    private @NonNull AudioInfo _audioInfo;
    
    private @NonNull List<AudioAlbum> _albums = new ArrayList<>();

    private boolean _running = false;
    
    private boolean _fetchingData = false;
    
    public AlbumsPresenterImpl(@NonNull AudioInfo audioInfo)
    {
        _audioInfo = audioInfo;
    }

    // AlbumsPresenter

    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("AlbumsPresenter: view has not been set");
        }

        Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Start.");

        AudioLibrary.getShared().registerLibraryChangesListener(this);

        fetchData();
    }

    @Override
    public void setView(@NonNull AlbumsView view)
    {
        if (_view != null) {
            throw new IllegalStateException("AlbumsPresenter: view has already been set");
        }

        _view = view;
    }

    @Override
    public void onDestroy()
    {
        Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Destroyed.");

        AudioLibrary.getShared().unregisterLibraryChangesListener(this);

        _running = false;
    }

    @Override
    public void fetchData()
    {
        if (_fetchingData)
        {
            return;
        }
        
        Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Fetching albums...");

        _fetchingData = true;

        final boolean running = _running;

        // Wait for the app start running
        // Then, update the view on the main thread
        Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable;

                if (running)
                {
                    final List<AudioAlbum> albums = _audioInfo.getAlbums();

                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Retrieved albums, updating view");

                            _fetchingData = false;

                            _albums = albums;
                            sortAlbums();

                            _view.onMediaAlbumsLoad(_albums);
                        }
                    };
                }
                else
                {
                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Presenter is not ready to fetch yet!");

                            _fetchingData = false;

                            _view.onFetchDataErrorEncountered(new RuntimeException("Presenter is not ready to fetch yet"));
                        }
                    };
                }

                mainHandler.post(myRunnable);
            }
        });

        handler.post(thread);
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
            Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Error: Invalid album list index, cannot respond to event properly");
            return;
        }

        AudioAlbum album = _albums.get(index);
        
        Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Open '" + album.albumTitle + "' album");

        List<BaseAudioTrack> tracks = _audioInfo.getAlbumTracks(album);

        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
        node.setName(album.albumTitle);
        node.setTracks(tracks);

        // Try to build
        try {
            BaseAudioPlaylist playlist = node.build();
            _view.openPlaylistScreen(_audioInfo, playlist, OpenPlaylistOptions.buildDefault());
        } catch (Exception e) {
            Log.v(AlbumsPresenterImpl.class.getCanonicalName(), "Error: Failed to create a playlist for the clicked album track");
        }
    }

    // AudioLibrary.ChangesListener

    @Override
    public void onMediaLibraryChanged()
    {
        if (_view != null)
        {
            _view.onDeviceLibraryChanged();
        }

        // Stay up to date with the latest library data
        fetchData();
    }

    // Utilities

    private void sortAlbums()
    {
        Collections.sort(_albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum first, AudioAlbum second) {
                String a = first.albumTitle;
                String b = second.albumTitle;
                return ListAlphabet.compareStrings(a, b);
            }
        });
    }
}
