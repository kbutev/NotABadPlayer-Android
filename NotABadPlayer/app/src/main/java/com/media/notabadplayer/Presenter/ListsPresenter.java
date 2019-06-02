package com.media.notabadplayer.Presenter;

import java.util.ArrayList;
import java.util.List;
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
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class ListsPresenter implements BasePresenter
{
    private BaseView _view;

    private @NonNull AudioInfo _audioInfo;

    public ListsPresenter(@NonNull AudioInfo audioInfo)
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
            throw new IllegalStateException("ListsPresenter: view has not been set");
        }

        // Use background thread to retrieve the user playlists
        // Then, update the view on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AudioPlaylist> playlists = GeneralStorage.getShared().getUserPlaylists();
                final ArrayList<AudioTrack> history = AudioPlayer.getShared().playHistory.getPlayHistory();
                
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        List<AudioPlaylist> userPlaylists = playlists != null ? playlists : new ArrayList<AudioPlaylist>();
                        AudioPlaylist historyPlaylist = history.size() > 0 ? new AudioPlaylist("...", history) : null;
                        _view.onUserPlaylistsLoad(historyPlaylist, userPlaylists);
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
        if (playlist != null)
        {
            Log.v(ListsPresenter.class.getCanonicalName(), "Open player screen with playlist " + playlist.getName());

            _view.openPlaylistScreen(_audioInfo, playlist);
        }
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
}
