package com.media.notabadplayer.Presenter.Playlist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class PlaylistPresenter implements BasePresenter {
    public static final boolean OPEN_PLAYER_ON_TRACK_PLAY = false;

    private @NonNull BaseView _view;
    
    private final @Nullable AudioAlbum _album;
    private final @Nullable AudioPlaylist _playlist;
    
    private @NonNull ArrayList<AudioTrack> _songs = new ArrayList<>();
    
    public PlaylistPresenter(@NonNull BaseView view, @NonNull AudioAlbum album)
    {
        _view = view;
        _album = album;
        _playlist = null;
    }

    public PlaylistPresenter(@NonNull BaseView view, @NonNull AudioPlaylist playlist)
    {
        _view = view;
        _album = null;
        _playlist = playlist;
    }

    @Override
    public void start()
    {
        if (_album != null)
        {
            _songs = AudioPlayer.getShared().getAudioInfo().getAlbumTracks(_album);
            _view.onAlbumSongsLoad(_songs);
        }
        else
        {
            _songs = _playlist.getTracks();
            _view.onPlaylistLoad(_playlist);
        }
    }

    @Override
    public void onAlbumItemClick(int index)
    {
        
    }

    @Override
    public void onPlaylistItemClick(int index)
    {
        // Index zero is the header - ignore
        if (index == 0)
        {
            return;
        }

        // Index greater than zero is an song track
        index--;

        AudioTrack clickedTrack = _songs.get(index);

        if (OPEN_PLAYER_ON_TRACK_PLAY)
        {
            openPlayerScreen(clickedTrack);
        }
        else
        {
            playNewTrack(clickedTrack);
        }
    }

    @Override
    public void onOpenPlayer()
    {
        AudioPlaylist currentlyPlayingPlaylist = AudioPlayer.getShared().getPlaylist();

        if (currentlyPlayingPlaylist != null)
        {
            _view.openPlaylistScreen(currentlyPlayingPlaylist);
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
    public void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }

    @Override
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }

    private void openPlayerScreen(@Nullable AudioTrack clickedTrack)
    {
        String playlistName = _album != null ? _album.albumTitle : _playlist.getName();

        AudioPlaylist playlist = new AudioPlaylist(playlistName, _songs, clickedTrack);

        Log.v(PlaylistPresenter.class.getCanonicalName(), "Play playlist with specific song " + clickedTrack.title);

        _view.openPlayerScreen(playlist);
    }

    private void playNewTrack(@Nullable AudioTrack clickedTrack)
    {
        String playlistName = _album != null ? _album.albumTitle : _playlist.getName();
        AudioPlaylist playlist = new AudioPlaylist(playlistName, _songs, clickedTrack);

        AudioPlayer player = AudioPlayer.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = playlist.getName();
            String currentPlaylistName = currentPlaylist.getName();

            AudioTrack newTrack = playlist.getPlayingTrack();
            AudioTrack currentTrack = currentPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!newPlaylistName.equals(currentPlaylistName) || !newTrack.equals(currentTrack))
            {
                // Change the audio player playlist to equal the presenter's playlist
                playNew(playlist);

                return;
            }

            // Do nothing, track is already playing

            return;
        }

        // Set audio player playlist for the first time and play its track
        playFirstTime(playlist);
    }

    private void playFirstTime(@NonNull AudioPlaylist playlist)
    {
        playNew(playlist);
    }

    private void playNew(@NonNull AudioPlaylist playlist)
    {
        String newPlaylistName = playlist.getName();
        AudioTrack newTrack = playlist.getPlayingTrack();

        Log.v(PlaylistPresenter.class.getCanonicalName(), "Opening player and playing new playlist '" + newPlaylistName + "' with track '" + newTrack.title + "'");

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
