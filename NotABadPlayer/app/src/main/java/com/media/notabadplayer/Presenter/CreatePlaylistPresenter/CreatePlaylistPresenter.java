package com.media.notabadplayer.Presenter.CreatePlaylistPresenter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Presenter.SearchPresenter;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Lists.CreatePlaylistActivity;
import com.media.notabadplayer.View.Lists.CreatePlaylistAlbumsAdapter;
import com.media.notabadplayer.View.Lists.CreatePlaylistTracksAdapter;

public class CreatePlaylistPresenter implements BaseCreatePlaylistPresenter {
    private @NonNull SearchPresenter _searchPresenter;
    
    private @NonNull Context _context;
    private CreatePlaylistPresenterDelegate _delegate;

    private List<AudioAlbum> _albums;

    private @Nullable BaseAudioPlaylist _playlist;
    private @NonNull ArrayList<BaseAudioTrack> _playlistTracks = new ArrayList<>();

    private String _playlistName = "";
    private CreatePlaylistTracksAdapter _addedTracksAdapter;
    private CreatePlaylistAlbumsAdapter _albumsAdapter;

    private final boolean _isEditPlaylist;
    
    public CreatePlaylistPresenter(@NonNull Context context,
                                   @NonNull AudioInfo audioInfo, 
                                   @NonNull CreatePlaylistPresenterDelegate delegate, 
                                   @Nullable BaseAudioPlaylist templatePlaylist)
    {
        this._searchPresenter = new SearchPresenter(context, audioInfo, false);
        this._context = context;
        this._delegate = delegate;

        _albums = Player.getShared().getAudioInfo().getAlbums();

        this._searchPresenter.setView(delegate);

        _isEditPlaylist = templatePlaylist != null;
        
        if (_isEditPlaylist)
        {
            this._playlist = templatePlaylist;
            this._playlistTracks = new ArrayList<>(templatePlaylist.getTracks());
        }
    }
    
    public CreatePlaylistPresenter(@NonNull Context context, @NonNull AudioInfo audioInfo, @NonNull CreatePlaylistPresenterDelegate delegate)
    {
        this(context, audioInfo, delegate, null);
    }
    
    public void updateAddedTracksView()
    {
        _addedTracksAdapter = new CreatePlaylistTracksAdapter(_context, _playlistTracks);
        _delegate.updateAddedTracks(_addedTracksAdapter);
    }

    public void updateAlbumsView()
    {
        if (_albumsAdapter == null)
        {
            Function<Integer, Void> onAlbumTrackClick = _delegate.onAlbumTrackClick();
            _albumsAdapter = new CreatePlaylistAlbumsAdapter(_context, Player.getShared().getAudioInfo(), _albums, onAlbumTrackClick);
        }
        
        _delegate.updateAlbums(_albumsAdapter);
    }

    public void updateSearchTracksView()
    {
        _delegate.refreshSearchTracks();
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
                if (!_isEditPlaylist)
                {
                    _delegate.showNameTakenDialog();
                    return;
                }
                else
                {
                    playlists.remove(e);
                    break;
                }
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
        updateAlbumsView();
        updateSearchTracksView();
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
        
        selectOrDeselectTrack(track);
    }
    
    // BasePresenter

    @Override
    public void setView(@NonNull BaseView view)
    {

    }

    @Override
    public void start()
    {
        Log.v(CreatePlaylistPresenter.class.getCanonicalName(), "Start.");
        
        _searchPresenter.onAppStateChange(AppState.RUNNING);
        _searchPresenter.start();
        
        updateAddedTracksView();
        updateAlbumsView();
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void fetchData()
    {

    }

    @Override
    public void onAppStateChange(AppState state)
    {

    }

    @Override
    public void onAlbumItemClick(int index)
    {

    }

    @Override
    public void onOpenPlayer(@Nullable BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input)
    {

    }

    @Override
    public void onPlayOrderButtonClick()
    {

    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }

    @Override
    public void onPlayerVolumeSet(double value)
    {

    }

    @Override
    public boolean onMarkOrUnmarkContextTrackFavorite()
    {
        return false;
    }

    @Override
    public void onPlaylistItemClick(int index)
    {

    }

    @Override
    public void onPlaylistItemEdit(int index)
    {
        
    }
    
    @Override
    public void onPlaylistItemDelete(int index)
    {

    }

    @Override
    public void onSearchResultClick(int index)
    {
        List<BaseAudioTrack> searchResults = _searchPresenter.getSearchResults();
        
        if (index < 0 || index >= searchResults.size())
        {
            return;
        }
        
        BaseAudioTrack track = searchResults.get(index);
        
        selectOrDeselectTrack(track);
        
        _delegate.onSearchResultClick(index);
    }

    @Override
    public void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter)
    {
        _searchPresenter.onSearchQuery(searchValue, filter);
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
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar)
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
    
    // Playlist update
    
    private void selectOrDeselectTrack(@NonNull BaseAudioTrack track)
    {
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
        updateSearchTracksView();
    }

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
