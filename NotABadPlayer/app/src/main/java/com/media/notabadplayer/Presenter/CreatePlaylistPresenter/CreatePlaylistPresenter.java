package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.Lists.CreatePlaylistActivity;
import com.media.notabadplayer.View.Lists.CreatePlaylistAlbumsAdapter;
import com.media.notabadplayer.View.Lists.CreatePlaylistTracksAdapter;

public class CreatePlaylistPresenter implements BaseCreatePlaylistPresenter {
    private @NonNull Context _context;
    
    private CreatePlaylistPresenterDelegate _delegate;

    private List<AudioAlbum> _albums;

    private @Nullable BaseAudioPlaylist _playlist;
    private @NonNull ArrayList<BaseAudioTrack> _playlistTracks = new ArrayList<>();

    private String _playlistName = "";
    private CreatePlaylistTracksAdapter _addedTracksAdapter;
    private CreatePlaylistAlbumsAdapter _albumsAdapter;
    
    public CreatePlaylistPresenter(@NonNull Context context, @NonNull CreatePlaylistPresenterDelegate delegate)
    {
        this._context = context;
        this._delegate = delegate;

        _albums = Player.getShared().getAudioInfo().getAlbums();
    }

    @Override
    public void start()
    {
        updateAlbumsView();
    }

    @Override
    public void updateAddedTracksView()
    {
        _addedTracksAdapter = new CreatePlaylistTracksAdapter(_context, _playlistTracks);
        _delegate.updateAddedTracksDataSource(_addedTracksAdapter);
    }

    @Override
    public void updateAlbumsView()
    {
        if (_albumsAdapter == null)
        {
            Function<Integer, Void> onAlbumTrackClick = _delegate.onAlbumTrackClick();
            _albumsAdapter = new CreatePlaylistAlbumsAdapter(_context, Player.getShared().getAudioInfo(), _albums, onAlbumTrackClick);
        }
        
        _delegate.updateAlbumsDataSource(_albumsAdapter);
    }
    
    @Override
    public void onPlaylistNameChanged(@NonNull String name)
    {
        _playlistName = name;
    }

    @Override
    public void onSaveUserPlaylist()
    {
        if (_playlist == null || _playlistTracks.size() == 0)
        {
            _delegate.showNoTracksDialog();
            return;
        }

        List<BaseAudioPlaylist> playlists = GeneralStorage.getShared().getUserPlaylists();

        String name = _playlistName;

        // Must not have empty name
        if (name.isEmpty())
        {
            _delegate.showInvalidNameDialog();
            return;
        }

        // Name must not be taken already
        for (int e = 0; e < playlists.size(); e++)
        {
            if (playlists.get(e).getName().equals(name))
            {
                _delegate.showNameTakenDialog();
                return;
            }
        }

        // Successful save
        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
        node.setName(name);
        node.setTracks(_playlistTracks);

        // Try to build
        try {
            BaseAudioPlaylist playlist = node.build();

            playlists.add(playlist);
        } catch (Exception e) {
            Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to save playlist to storage");
            _delegate.showUnknownErrorDialog();
            return;
        }

        GeneralStorage.getShared().saveUserPlaylists(playlists);

        _delegate.goBack();
    }

    // Added tracks operations

    @Override
    public boolean isTrackAdded(@NonNull BaseAudioTrack track)
    {
        return _playlistTracks.contains(track);
    }

    @Override
    public void onAddedTrackClicked(int index)
    {
        BaseAudioTrack track = _playlistTracks.get(index);
        removeFromPlaylist(track);
        _albumsAdapter.deselectTrack(track);
        updateAddedTracksView();
    }

    // Album track operations

    @Override
    public int getSelectedAlbumIndex()
    {
        if (_albumsAdapter == null) {
            return 0;
        }
        
        return _albumsAdapter.getSelectedAlbumPosition();
    }

    @Override
    public void onOpenAlbumAt(int index)
    {
        if (_albumsAdapter == null) {
            return;
        }
        
        _albumsAdapter.selectAlbum(index);
        _albumsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCloseAlbum()
    {
        _albumsAdapter.deselectAlbum();
        _albumsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAlbumTrackClick(int fromOpenedAlbumIndex)
    {
        BaseAudioTrack track = _albumsAdapter.getTrackForOpenedAlbumIndex(fromOpenedAlbumIndex);

        if (track == null) 
        {
            return;
        }
        
        if (!isTrackAdded(track))
        {
            // Select if not selected already
            addToPlaylist(track);
            _albumsAdapter.selectTrack(track);
        }
        else
        {
            // Otherwise, deselect
            removeFromPlaylist(track);
            _albumsAdapter.deselectTrack(track);
        }
        
        updateAddedTracksView();
    }
    
    // Playlist update

    private void addToPlaylist(@NonNull BaseAudioTrack track)
    {
        if (_playlistTracks.contains(track))
        {
            return;
        }

        String name = _playlistName;

        if (name.length() == 0)
        {
            name = "Playlist";
        }

        ArrayList<BaseAudioTrack> playlistTracks = new ArrayList<>(_playlistTracks);
        playlistTracks.add(track);

        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
        node.setName(name);
        node.setTracks(playlistTracks);

        // Try to build
        try {
            _playlist = node.build();
            _playlistTracks.add(track);
        } catch (Exception e) {
            Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to rebuild playlist");
        }
    }

    private void removeFromPlaylist(@NonNull BaseAudioTrack track)
    {
        String name = _playlistName;

        if (name.length() == 0)
        {
            name = "Playlist";
        }

        ArrayList<BaseAudioTrack> playlistTracks = new ArrayList<>(_playlistTracks);
        playlistTracks.remove(track);

        if (!playlistTracks.isEmpty())
        {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(name);
            node.setTracks(playlistTracks);

            try {
                _playlist = node.build();
                _playlistTracks.remove(track);
            } catch (Exception e) {
                Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to rebuild playlist");
            }
        }
        else
        {
            _playlist = null;
            _playlistTracks.clear();
        }
    }
}
