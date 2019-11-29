package com.media.notabadplayer.Presenter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class SearchPresenter implements BasePresenter
{
    private BaseView _view;
    private @NonNull Context _context;
    private @NonNull AudioInfo _audioInfo;
    private List<AudioTrack> _searchResults = new ArrayList<>();
    private @Nullable String _lastSearchQuery = null;

    private boolean _running = false;
    
    public SearchPresenter(@NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        _context = context;
        _audioInfo = audioInfo;
    }

    @Override
    public void setView(@NonNull BaseView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlayerPresenter: view has already been set");
        }
        
        _view = view;
    }
    
    @Override
    public void start() 
    {
        if (_view == null)
        {
            throw new IllegalStateException("SearchPresenter: view has not been set");
        }

        Log.v(SearchPresenter.class.getCanonicalName(), "Start.");

        // Restore last search query from storage
        String searchQuery = GeneralStorage.getShared().retrieveSearchQuery();

        // Search only when app is already running
        if (searchQuery != null && searchQuery.length() > 0 && _running)
        {
            _lastSearchQuery = searchQuery;

            searchForQuery();
        }
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
        boolean previousRunningState = _running;

        _running = state.isRunning();

        if (_running && !previousRunningState)
        {
            searchForQuery();
        }
    }
    
    @Override
    public void onAlbumItemClick(int index)
    {

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
    public void onPlaylistItemDelete(int index)
    {

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
            Log.v(SearchPresenter.class.getCanonicalName(), "Error: invalid clicked search index");
            return;
        }
        
        AudioTrack clickedTrack = _searchResults.get(index);

        if (GeneralStorage.getShared().getOpenPlayerOnPlayValue().openForSearch())
        {
            openPlayerScreen(clickedTrack);
        }
        else
        {
            playNewTrack(clickedTrack);
        }
        
        AudioPlaylist audioPlayerPlaylist = Player.getShared().getPlaylist();
        
        if (audioPlayerPlaylist != null)
        {
            _view.updatePlayerScreen(audioPlayerPlaylist);
        }
    }
    
    @Override
    public void onSearchQuery(@NonNull String searchValue)
    {
        if (!_running)
        {
            return;
        }

        // Skip if we already searched for this
        if (_lastSearchQuery != null) {
            if (searchValue.equals(_lastSearchQuery)) {
                return;
            }
        }

        _lastSearchQuery = searchValue;

        searchForQuery();
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

    private void searchForQuery()
    {
        String searchValue = _lastSearchQuery;

        Log.v(SearchPresenter.class.getCanonicalName(), "Searching for '" + searchValue + "' ...");

        _lastSearchQuery = searchValue;

        final String searchQuery = searchValue.toLowerCase();

        // Save search query
        GeneralStorage.getShared().saveSearchQuery(searchQuery);

        // Start search process
        _view.updateSearchQueryResults(searchQuery, new ArrayList<AudioTrack>(), _context.getResources().getString(R.string.search_hint_searching));

        // Use background thread to retrieve the search results
        // Then, update the view on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                _searchResults = _audioInfo.searchForTracks(searchQuery);

                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        Log.v(SearchPresenter.class.getCanonicalName(), "Retrieved search results, updating view");

                        _view.updateSearchQueryResults(searchQuery, _searchResults, null);
                    }
                };

                mainHandler.post(myRunnable);
            }
        });

        thread.start();
    }

    private void openPlayerScreen(@NonNull AudioTrack clickedTrack)
    {
        String searchPlaylistName = _context.getResources().getString(R.string.playlist_name_search_results);
        
        try {
            AudioPlaylist searchPlaylist = new AudioPlaylist(searchPlaylistName, _searchResults, clickedTrack);

            Log.v(SearchPresenter.class.getCanonicalName(), "Opening player screen");

            _view.openPlayerScreen(searchPlaylist);
        } catch (Exception e) {
            Log.v(SearchPresenter.class.getCanonicalName(), "Error: Could not open player screen: " + e.toString());
        }
    }

    private void playNewTrack(@NonNull AudioTrack clickedTrack)
    {
        String searchPlaylistName = _context.getResources().getString(R.string.playlist_name_search_results);
        AudioPlaylist searchPlaylist;
        
        try {
            searchPlaylist = new AudioPlaylist(searchPlaylistName, _searchResults, clickedTrack);
        } catch (Exception e) {
            Log.v(SearchPresenter.class.getCanonicalName(), "Error: Could not play track: " + e.toString());
            return;
        }
        
        Player player = Player.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = searchPlaylist.getName();
            AudioTrack newTrack = searchPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!searchPlaylist.equals(currentPlaylist))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(SearchPresenter.class.getCanonicalName(), "Playing track '" + newTrack.title + "' from playlist '" + newPlaylistName + "'");
                playNew(searchPlaylist);

                return;
            }

            // Do nothing, track is already playing

            return;
        }
        
        // Set audio player playlist for the first time and play its track
        Log.v(SearchPresenter.class.getCanonicalName(), "Playing track '" + searchPlaylist.getPlayingTrack().title + "' for the first time");
        playFirstTime(searchPlaylist);
    }

    private void playFirstTime(@NonNull AudioPlaylist playlist)
    {
        playNew(playlist);
    }

    private void playNew(@NonNull AudioPlaylist playlist)
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
}
