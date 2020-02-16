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
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class ListsPresenter implements BasePresenter
{
    private BaseView _view;
    
    private @NonNull AudioInfo _audioInfo;
    
    private List<BaseAudioPlaylist> _playlists = new ArrayList<>();
    
    private final String _historyPlaylistName;
    private final String _recentlyAddedPlaylistName;
    private final String _favoritesPlaylistName;

    private boolean _running = false;
    
    private boolean _fetchingData = false;
    
    public ListsPresenter(@NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        _audioInfo = audioInfo;
        _historyPlaylistName = context.getResources().getString(R.string.playlist_name_history);
        _recentlyAddedPlaylistName = context.getResources().getString(R.string.playlist_name_recently_added);
        _favoritesPlaylistName = context.getResources().getString(R.string.playlist_name_favorites);
    }
    
    @Override
    public void setView(@NonNull BaseView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("ListsPresenter: view has already been set");
        }
        
        _view = view;
    }
    
    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("ListsPresenter: view has not been set");
        }

        Log.v(ListsPresenter.class.getCanonicalName(), "Start.");
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void fetchData()
    {
        if (_fetchingData)
        {
            return;
        }

        Log.v(ListsPresenter.class.getCanonicalName(), "Fetching user playlists...");
        
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
                    List<BaseAudioPlaylist> lists = GeneralStorage.getShared().getUserPlaylists();

                    BaseAudioPlaylist recentlyAdded = getRecentlyAddedPlaylist();

                    if (recentlyAdded != null)
                    {
                        lists.add(0, recentlyAdded);
                    }

                    BaseAudioPlaylist recentlyPlayed = Player.getShared().playHistory.getPlayHistoryAsPlaylist(_historyPlaylistName);
                    
                    if (recentlyPlayed != null)
                    {
                        lists.add(0, recentlyPlayed);
                    }

                    BaseAudioPlaylist favorites = getFavoritesPlaylist();

                    if (favorites != null)
                    {
                        lists.add(0, favorites);
                    }

                    final List<BaseAudioPlaylist> data = lists;
                    
                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(ListsPresenter.class.getCanonicalName(), "Retrieved user playlists, updating view");

                            _fetchingData = false;
                            
                            _playlists = data;
                            
                            updatePlaylistsData();
                        }
                    };
                }
                else
                {
                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(ListsPresenter.class.getCanonicalName(), "Presenter is not ready to fetch yet!");

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

    }

    @Override
    public void onPlaylistItemClick(int index) 
    {
        if (!_running)
        {
            return;
        }
        
        if (index < 0 || index >= _playlists.size())
        {
            return;
        }

        BaseAudioPlaylist playlist = _playlists.get(index);

        Log.v(ListsPresenter.class.getCanonicalName(), "Open playlist '" + playlist.getName() + "'");

        OpenPlaylistOptions appropriateOptions = getAppropriateOpenOptions(playlist);

        _view.openPlaylistScreen(_audioInfo, playlist, appropriateOptions);
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
    public void onPlaylistItemDelete(int index)
    {
        if (!_running)
        {
            return;
        }
        
        if (index < _playlists.size())
        {
            _playlists.remove(index);

            if (_playlists.size() > 0)
            {
                ArrayList<BaseAudioPlaylist> playlists = new ArrayList<>(_playlists);

                // Remove the temporary lists before saving (recently played/added playlists)
                for (BaseAudioPlaylist playlist : _playlists)
                {
                    if (playlist.isTemporary())
                    {
                        playlists.remove(playlist);
                    }
                }

                // Save to storage
                GeneralStorage.getShared().saveUserPlaylists(playlists);
                
                updatePlaylistsData();
            }
        }
    }

    @Override
    public void onSearchResultClick(int index) 
    {

    }

    @Override
    public void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter)
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
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {

    }
    
    private void updatePlaylistsData()
    {
        _view.onUserPlaylistsLoad(_playlists);
    }

    private @Nullable BaseAudioPlaylist getRecentlyAddedPlaylist()
    {
        List<BaseAudioTrack> recentlyAddedTracks = AudioLibrary.getShared().getRecentlyAddedTracks();

        if (recentlyAddedTracks.size() > 0)
        {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(_recentlyAddedPlaylistName);
            node.setTracks(recentlyAddedTracks);
            node.setIsTemporaryPlaylist(true);

            try {
                return node.build();
            } catch (Exception exc) {

            }
        }

        return null;
    }

    private @Nullable BaseAudioPlaylist getFavoritesPlaylist()
    {
        List<BaseAudioTrack> favoriteTracks = AudioLibrary.getShared().getFavoriteTracks();

        if (favoriteTracks.size() > 0)
        {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(_favoritesPlaylistName);
            node.setTracks(favoriteTracks);
            node.setIsTemporaryPlaylist(true);

            try {
                return node.build();
            } catch (Exception exc) {

            }
        }

        return null;
    }
    
    private @NonNull OpenPlaylistOptions getAppropriateOpenOptions(@NonNull BaseAudioPlaylist playlist) 
    {
        if (!playlist.isTemporary()) {
            return OpenPlaylistOptions.buildDefault();
        }
        
        if (playlist.getName().equals(_favoritesPlaylistName)) 
        {
            return OpenPlaylistOptions.buildFavorites();
        }

        return OpenPlaylistOptions.buildDefault();
    }
}
