package com.media.notabadplayer.Presenter;

import java.util.ArrayList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
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
        _view = view;
    }
    
    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("PlaylistPresenter: view has not been set");
        }
        
        _view.onPlaylistLoad(_playlist);
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
        AudioPlaylist currentlyPlayingPlaylist = AudioPlayer.getShared().getPlaylist();

        if (currentlyPlayingPlaylist != null)
        {
            _view.openPlaylistScreen(_audioInfo, currentlyPlayingPlaylist);
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
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }

    private void openPlayerScreen(@NonNull AudioTrack clickedTrack)
    {
        String playlistName = _playlist.getName();
        ArrayList<AudioTrack> tracks = _playlist.getTracks();
        AudioPlaylist playlist = new AudioPlaylist(playlistName, tracks, clickedTrack);

        Log.v(PlaylistPresenter.class.getCanonicalName(), "Opening player screen");

        _view.openPlayerScreen(playlist);
    }

    private void playNewTrack(@NonNull AudioTrack clickedTrack)
    {
        String playlistName = _playlist.getName();
        ArrayList<AudioTrack> tracks = _playlist.getTracks();
        AudioPlaylist playlistToPlay = new AudioPlaylist(playlistName, tracks, clickedTrack);

        AudioPlayer player = AudioPlayer.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = playlistToPlay.getName();
            String currentPlaylistName = currentPlaylist.getName();

            AudioTrack newTrack = playlistToPlay.getPlayingTrack();
            AudioTrack currentTrack = currentPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!newPlaylistName.equals(currentPlaylistName) || !newTrack.equals(currentTrack))
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
        AudioPlayer player = AudioPlayer.getShared();

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
    }
}
