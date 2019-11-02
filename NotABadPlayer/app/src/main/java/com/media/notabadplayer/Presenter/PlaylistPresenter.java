package com.media.notabadplayer.Presenter;

import java.util.ArrayList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BaseView;

public class PlaylistPresenter implements BasePresenter {
    private BaseView _view;

    private final @NonNull AudioPlaylist _playlist;

    private @NonNull AudioInfo _audioInfo;
    
    public PlaylistPresenter(@NonNull AudioPlaylist playlist, @NonNull AudioInfo audioInfo)
    {
        // Sort playlist
        // Sort only playlists of type album
        AppSettings.TrackSorting sorting = GeneralStorage.getShared().getTrackSortingValue();
        AudioPlaylist sortedPlaylist = playlist.isAlbumPlaylist() ? playlist.sortedPlaylist(sorting) : playlist;

        _playlist = sortedPlaylist;

        _audioInfo = audioInfo;
    }

    @Override
    public void setView(@NonNull BaseView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlaylistPresenter: view has already been set");
        }
        
        _view = view;
    }
    
    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("PlaylistPresenter: view has not been set");
        }

        Log.v(PlaylistPresenter.class.getCanonicalName(), "Start.");
        
        _view.onPlaylistLoad(_playlist);
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
    public void onPlaylistItemClick(int index)
    {
        ArrayList<AudioTrack> tracks = _playlist.getTracks();

        if (index < 0 || index >= tracks.size())
        {
            Log.v(PlaylistPresenter.class.getCanonicalName(), "Error: Invalid track list index, cannot respond to event properly");
            return;
        }

        AudioTrack clickedTrack = tracks.get(index);

        if (GeneralStorage.getShared().getOpenPlayerOnPlayValue().openForPlaylist())
        {
            openPlayerScreen(clickedTrack);
        }
        else
        {
            playNewTrack(clickedTrack);
        }
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

    private void openPlayerScreen(@NonNull AudioTrack clickedTrack)
    {
        String playlistName = _playlist.getName();
        ArrayList<AudioTrack> tracks = _playlist.getTracks();

        try {
            AudioPlaylist playlist = new AudioPlaylist(playlistName, tracks, clickedTrack);

            Log.v(PlaylistPresenter.class.getCanonicalName(), "Opening player screen");

            _view.openPlayerScreen(playlist);
        } catch (Exception e) {
            Log.v(PlaylistPresenter.class.getCanonicalName(), "Error: Could not open player screen: " + e.toString());
        }
    }

    private void playNewTrack(@NonNull AudioTrack clickedTrack)
    {
        String playlistName = _playlist.getName();
        ArrayList<AudioTrack> tracks = _playlist.getTracks();
        
        AudioPlaylist playlistToPlay;
        
        try {
            playlistToPlay = new AudioPlaylist(playlistName, tracks, clickedTrack);
        } catch (Exception e) {
            Log.v(PlaylistPresenter.class.getCanonicalName(), "Error: Could not play track: " + e.toString());
            return;
        }

        Player player = Player.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = playlistToPlay.getName();
            AudioTrack newTrack = playlistToPlay.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!playlistToPlay.equals(currentPlaylist))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(PlaylistPresenter.class.getCanonicalName(), "Playing track '" + newTrack.title + "' from playlist '" + newPlaylistName + "'");
                playNew(playlistToPlay);

                return;
            }

            // Do nothing, track is already playing

            return;
        }

        // Set audio player playlist for the first time and play its track
        Log.v(PlaylistPresenter.class.getCanonicalName(), "Playing track '" + _playlist.getPlayingTrack().title + "' from playlist '" + _playlist.getName() + " for the first time");
        playFirstTime(playlistToPlay);
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
