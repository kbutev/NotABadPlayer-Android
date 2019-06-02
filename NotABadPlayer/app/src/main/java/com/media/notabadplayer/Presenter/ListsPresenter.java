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
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class ListsPresenter implements BasePresenter
{
    private BaseView _view;

    private @NonNull Context _context;
    
    private @NonNull AudioInfo _audioInfo;
    
    private List<AudioPlaylist> _playlists = new ArrayList<>();

    private boolean _running = false;
    
    public ListsPresenter(@NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        _context = context;
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
            throw new IllegalStateException("ListsPresenter: view has not been set");
        }

        updatePlaylistsData(0);
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
        if (index < _playlists.size())
        {
            _playlists.remove(index);

            if (_playlists.size() > 0)
            {
                ArrayList<AudioPlaylist> playlists = new ArrayList<>(_playlists);

                playlists.remove(0);

                GeneralStorage.getShared().saveUserPlaylists(playlists);
                
                updatePlaylistsData(0);
            }
        }
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
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {

    }
    
    private void updatePlaylistsData(int delay)
    {
        // Use background thread to retrieve the user playlists
        // Then, update the view on the main thread
        Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!_running)
                {
                    updatePlaylistsData(250);
                    return;
                }
                
                List<AudioPlaylist> userPlaylists = GeneralStorage.getShared().getUserPlaylists();
                final List<AudioPlaylist> playlists = userPlaylists != null ? userPlaylists : new ArrayList<AudioPlaylist>();
                final ArrayList<AudioTrack> history = AudioPlayer.getShared().playHistory.getPlayHistory();
                
                if (history.size() > 0)
                {
                    String historyPlaylistName = _context.getResources().getString(R.string.playlist_name_recently_played);
                    playlists.add(0, new AudioPlaylist(historyPlaylistName, history));
                }

                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        Log.v(ListsPresenter.class.getCanonicalName(), "Updating user playlist data");
                        
                        _playlists = playlists;
                        _view.onUserPlaylistsLoad(_playlists);
                    }
                };

                mainHandler.post(myRunnable);
            }
        });
        
        handler.postDelayed(thread, delay);
    }
}
