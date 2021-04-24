package com.media.notabadplayer.Presenter.Playlist;

import java.util.List;

import androidx.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.AudioTrackSource;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.Playlist.PlaylistView;

public class PlaylistPresenterImpl implements PlaylistPresenter {
    private PlaylistView _view;

    private final @NonNull BaseAudioPlaylist _playlist;

    private @NonNull AudioInfo _audioInfo;
    private @NonNull OpenPlaylistOptions _options;
    
    public PlaylistPresenterImpl(@NonNull BaseAudioPlaylist playlist, @NonNull AudioInfo audioInfo, @NonNull OpenPlaylistOptions options)
    {
        // Sort playlist
        // Sort only playlists of type album
        AppSettings.TrackSorting sorting = GeneralStorage.getShared().getTrackSortingValue();
        BaseAudioPlaylist sortedPlaylist = playlist.isAlbum() ? playlist.sortedPlaylist(sorting) : playlist;

        _playlist = sortedPlaylist;

        _audioInfo = audioInfo;
        _options = options;
    }

    // PlaylistPresenter

    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("PlaylistPresenter: view has not been set");
        }

        Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Start.");

        _view.onPlaylistLoad(_playlist);
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void onAppStateChange(AppState state)
    {

    }

    @Override
    public void setView(@NonNull PlaylistView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlaylistPresenter: view has already been set");
        }
        
        _view = view;
    }

    @Override
    public void onPlaylistItemClick(int index)
    {
        List<BaseAudioTrack> tracks = _playlist.getTracks();

        if (index < 0 || index >= tracks.size())
        {
            Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Error: Invalid track list index, cannot respond to event properly");
            return;
        }

        BaseAudioTrack clickedTrack = tracks.get(index);

        if (GeneralStorage.getShared().getOpenPlayerOnPlayValue().openForPlaylist())
        {
            openPlayerScreen(clickedTrack);
        }
        else
        {
            playNewTrack(clickedTrack);
        }
    }

    private void openPlayerScreen(@NonNull BaseAudioTrack clickedTrack)
    {
        BaseAudioPlaylist playlist;

        try {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start(_playlist);
            node.setPlayingTrack(clickedTrack);

            // Try to build
            playlist = node.build();
        } catch (Exception e) {
            Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Error: Could not open player screen: " + e.toString());
            return;
        }

        Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Opening player screen");

        _view.openPlayerScreen(playlist);
    }

    private void playNewTrack(@NonNull BaseAudioTrack clickedTrack)
    {
        // The playlist that will be played would be either the given playlist to the presenter
        // or the source playlist of the clicked track
        BaseAudioPlaylist playlistToBuild = _playlist;
        
        if (_options.openOriginalSourcePlaylist) {
            AudioTrackSource originalSource = clickedTrack.getOriginalSource();
            BaseAudioPlaylist source = originalSource.getSourcePlaylist(_audioInfo, clickedTrack);
            
            if (source != null)
            {
                playlistToBuild = source;
            }
        }
        
        BaseAudioPlaylist playlistToPlay;
        
        try {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start(playlistToBuild);
            node.setPlayingTrack(clickedTrack);

            // Try to build
            playlistToPlay = node.build();
        } catch (Exception e) {
            Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Error: Could not play track: " + e.toString());
            return;
        }

        Player player = Player.getShared();
        BaseAudioPlaylist currentPlaylist = player.getPlaylist();

        if (currentPlaylist != null)
        {
            String newPlaylistName = playlistToPlay.getName();
            BaseAudioTrack newTrack = playlistToPlay.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!playlistToPlay.equals(currentPlaylist))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Playing track '" + newTrack.getTitle() + "' from playlist '" + newPlaylistName + "'");
                playNew(playlistToPlay);

                return;
            }

            // Do nothing, track is already playing

            return;
        }

        // Set audio player playlist for the first time and play its track
        Log.v(PlaylistPresenterImpl.class.getCanonicalName(), "Playing track '" + _playlist.getPlayingTrack().getTitle() + "' from playlist '" + _playlist.getName() + " for the first time");
        playFirstTime(playlistToPlay);
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
}
