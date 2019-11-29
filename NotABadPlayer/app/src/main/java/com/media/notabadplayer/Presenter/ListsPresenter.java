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
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Constants.SearchFilter;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class ListsPresenter implements BasePresenter
{
    private BaseView _view;
    
    private @NonNull AudioInfo _audioInfo;
    
    private List<AudioPlaylist> _playlists = new ArrayList<>();
    
    private final String _historyPlaylistName;

    private boolean _running = false;
    
    private boolean _fetchingData = false;
    
    public ListsPresenter(@NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        _audioInfo = audioInfo;
        _historyPlaylistName = context.getResources().getString(R.string.playlist_name_recently_played);
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
                    ArrayList<AudioPlaylist> lists = GeneralStorage.getShared().getUserPlaylists();
                    AudioPlaylist recentlyPlayed = Player.getShared().playHistory.getPlayHistoryAsPlaylist(_historyPlaylistName);
                    
                    if (recentlyPlayed != null)
                    {
                        lists.add(0, recentlyPlayed);
                    }

                    final ArrayList<AudioPlaylist> data = lists;
                    
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
        
        AudioPlaylist playlist = _playlists.get(index);

        Log.v(ListsPresenter.class.getCanonicalName(), "Open playlist '" + playlist.getName() + "'");

        _view.openPlaylistScreen(_audioInfo, playlist);
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
        if (!_running)
        {
            return;
        }
        
        if (index < _playlists.size())
        {
            _playlists.remove(index);

            if (_playlists.size() > 0)
            {
                ArrayList<AudioPlaylist> playlists = new ArrayList<>(_playlists);

                playlists.remove(0);

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
}
