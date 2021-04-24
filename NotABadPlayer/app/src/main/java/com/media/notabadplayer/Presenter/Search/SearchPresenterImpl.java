package com.media.notabadplayer.Presenter.Search;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Other.AudioPlayerTimerValue;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Constants.SearchFilter;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.MVP.BaseView;
import com.media.notabadplayer.View.Search.SearchView;

public class SearchPresenterImpl implements SearchPresenter
{
    private final Object lock = new Object();
    
    private SearchView _view;
    private @NonNull Context _context;
    private @NonNull AudioInfo _audioInfo;
    private List<BaseAudioTrack> _searchResults = new ArrayList<>();
    private @Nullable String _lastSearchQuery = null;
    private SearchFilter _lastSearchFilter = SearchFilter.Title;
    private final boolean _restoreLastSearchQuery;

    private boolean _running = false;

    public SearchPresenterImpl(@NonNull Context context, @NonNull AudioInfo audioInfo, boolean restoreLastSearchQuery)
    {
        _context = context;
        _audioInfo = audioInfo;
        _restoreLastSearchQuery = restoreLastSearchQuery;
    }
    
    public SearchPresenterImpl(@NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        this(context, audioInfo, true);
    }

    private void setSearchResults(@NonNull List<BaseAudioTrack> results)
    {
        synchronized (lock)
        { 
            _searchResults = results;
        }
    }

    // SearchPresenter
    
    @Override
    public void start() 
    {
        if (_view == null)
        {
            throw new IllegalStateException("SearchPresenter: view has not been set");
        }

        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Start.");

        // Restore last search query from storage
        if (_restoreLastSearchQuery)
        {
            restoreSearchQueryFromStorage();
        }
    }

    @Override
    public void setView(@NonNull SearchView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlayerPresenter: view has already been set");
        }

        _view = view;
    }

    @Override
    public void onDestroy()
    {
        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Destroyed.");

        _running = false;
    }

    @Override
    public void onAppStateChange(AppState state)
    {
        boolean previousRunningState = _running;

        _running = state.isRunning();

        if (_running && !previousRunningState && _restoreLastSearchQuery)
        {
            restoreSearchQueryFromStorage();
        }
    }

    @Override
    public @NonNull List<BaseAudioTrack> getSearchResults()
    {
        synchronized (lock)
        {
            return new ArrayList<>(_searchResults);
        }
    }

    @Override
    public void onSearchResultClick(int index)
    {
        if (!_running)
        {
            return;
        }
        
        if (index < 0 || index >= _searchResults.size())
        {
            Log.v(SearchPresenterImpl.class.getCanonicalName(), "Error: invalid clicked search index");
            return;
        }

        BaseAudioTrack clickedTrack = _searchResults.get(index);

        if (GeneralStorage.getShared().getOpenPlayerOnPlayValue().openForSearch())
        {
            openPlayerScreen(clickedTrack);
        }
        else
        {
            playNewTrack(clickedTrack);
        }

        BaseAudioPlaylist audioPlayerPlaylist = Player.getShared().getPlaylist();
        
        if (audioPlayerPlaylist != null)
        {
            _view.updatePlayerScreen(audioPlayerPlaylist);
        }
    }
    
    @Override
    public void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter)
    {
        if (!_running)
        {
            return;
        }

        // Skip if we already searched for this
        if (_lastSearchQuery != null) {
            if (searchValue.equals(_lastSearchQuery)) {
                if (_lastSearchFilter == filter) {
                    return;
                }
            }
        }

        _lastSearchQuery = searchValue;
        _lastSearchFilter = filter;

        searchForQuery();
    }

    private void searchForQuery()
    {
        if (_lastSearchQuery == null)
        {
            return;
        }

        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Searching for '" + _lastSearchQuery + "' ...");

        final String searchQuery = _lastSearchQuery.toLowerCase();
        final SearchFilter searchFilter = _lastSearchFilter;

        // Save search query
        GeneralStorage storage = GeneralStorage.getShared();
        storage.saveSearchQuery(searchQuery);
        storage.saveSearchQueryFilter(searchFilter);

        // Start search process
        String searchString = _context.getResources().getString(R.string.search_state_searching);
        _view.updateSearchQueryResults(searchQuery, searchFilter, new ArrayList<BaseAudioTrack>(), searchString);

        // Use background thread to retrieve the search results
        // Then, update the view on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<BaseAudioTrack> results = _audioInfo.searchForTracks(searchQuery, searchFilter);

                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Retrieved search results, updating view");

                        setSearchResults(results);
                        
                        _view.updateSearchQueryResults(searchQuery, searchFilter, getSearchResults(), null);
                    }
                };

                mainHandler.post(myRunnable);
            }
        });

        thread.start();
    }

    private void openPlayerScreen(@NonNull BaseAudioTrack clickedTrack)
    {
        String searchPlaylistName = _context.getResources().getString(R.string.playlist_name_search_results);
        BaseAudioPlaylist searchPlaylist;
        
        try {
            // Build a playlist with the search results and the clicked track as
            // the playing track
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(searchPlaylistName);
            node.setTracks(_searchResults);
            node.setPlayingTrack(clickedTrack);

            searchPlaylist = node.build();
        } catch (Exception e) {
            Log.v(SearchPresenterImpl.class.getCanonicalName(), "Error: Could not open player screen: " + e.toString());
            return;
        }

        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Opening player screen");

        _view.openPlayerScreen(searchPlaylist);
    }

    private void playNewTrack(@NonNull BaseAudioTrack clickedTrack)
    {
        String searchPlaylistName = _context.getResources().getString(R.string.playlist_name_search_results);
        BaseAudioPlaylist searchPlaylist;
        
        try {
            // Build a playlist with the search results and the clicked track as
            // the playing track
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(searchPlaylistName);
            node.setTracks(_searchResults);
            node.setPlayingTrack(clickedTrack);

            searchPlaylist = node.build();
        } catch (Exception e) {
            Log.v(SearchPresenterImpl.class.getCanonicalName(), "Error: Could not play track: " + e.toString());
            return;
        }
        
        Player player = Player.getShared();
        BaseAudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = searchPlaylist.getName();
            BaseAudioTrack newTrack = searchPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!searchPlaylist.equals(currentPlaylist))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(SearchPresenterImpl.class.getCanonicalName(), "Playing track '" + newTrack.getTitle() + "' from playlist '" + newPlaylistName + "'");
                playNew(searchPlaylist);

                return;
            }

            // Do nothing, track is already playing

            return;
        }
        
        // Set audio player playlist for the first time and play its track
        Log.v(SearchPresenterImpl.class.getCanonicalName(), "Playing track '" + searchPlaylist.getPlayingTrack().getTitle() + "' for the first time");
        playFirstTime(searchPlaylist);
    }

    private void playFirstTime(@NonNull BaseAudioPlaylist playlist)
    {
        playNew(playlist);
    }

    private void playNew(@NonNull BaseAudioPlaylist playlist)
    {
        Player player = Player.getShared();

        try {
            player.playPlaylist(playlist);
        } catch (Exception e) {
            _view.onPlayerErrorEncountered(e);
            return;
        }

        if (!player.isPlaying())
        {
            player.resume();
        }

        _view.updatePlayerScreen(playlist);
    }

    private void restoreSearchQueryFromStorage()
    {
        GeneralStorage storage = GeneralStorage.getShared();

        String searchQuery = storage.retrieveSearchQuery();
        SearchFilter searchFilter = storage.retrieveSearchQueryFilter();

        if (searchQuery != null && searchQuery.length() > 0)
        {
            _lastSearchQuery = searchQuery;
            _lastSearchFilter = searchFilter;

            // Search only when app is already running
            if (_running)
            {
                searchForQuery();
            }
        }
    }
}
