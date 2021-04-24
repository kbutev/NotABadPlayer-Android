package com.media.notabadplayer.Presenter.Player;

import androidx.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.Player.PlayerView;

public class PlayerPresenterImpl implements PlayerPresenter
{
    private PlayerView _view;
    private @NonNull BaseAudioPlaylist _playlist;
    
    public PlayerPresenterImpl(@NonNull BaseAudioPlaylist playlist)
    {
        _playlist = playlist;
    }

    // PlayerPresenter

    @Override
    public void start() 
    {
        if (_view == null)
        {
            throw new IllegalStateException("PlayerPresenter: view has not been set");
        }

        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Start.");
        
        Player player = Player.getShared();
        BaseAudioPlaylist currentPlaylist = player.getPlaylist();
        
        if (currentPlaylist != null)
        {
            BaseAudioTrack newTrack = _playlist.getPlayingTrack();
            BaseAudioTrack currentTrack = currentPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!_playlist.equals(currentPlaylist))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Opening player screen and playing track '" + newTrack.getTitle() + "'");
                playNew(_playlist);

                return;
            }
            
            // Just open screen
            Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Opening player screen with current audio player track '" + currentTrack.getTitle() + "'");
            playContinue(currentPlaylist);
            
            return;
        }

        // Set audio player playlist for the first time and play its track
        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Opening player screen for the first time and playing track '" + _playlist.getPlayingTrack().getTitle() + "'");
        playFirstTime(_playlist);
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
    public void setView(@NonNull PlayerView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlayerPresenter: view has already been set");
        }

        _view = view;
    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input)
    {
        ApplicationAction action = KeyBinds.getShared().getActionForInput(input);
        
        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Perform KeyBinds action '" + action.name() + "' for input '" + input.name() + "'");

        Exception exception = KeyBinds.getShared().performAction(action);

        if (exception != null)
        {
            _view.onPlayerErrorEncountered(exception);
        }
    }
    
    @Override
    public void onPlayOrderButtonClick()
    {
        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Player input: change play order");

        Exception exception = KeyBinds.getShared().performAction(ApplicationAction.CHANGE_PLAY_ORDER);

        if (exception != null)
        {
            _view.onPlayerErrorEncountered(exception);
        }
    }
    
    @Override
    public void onPlayerVolumeSet(double value)
    {
        Player.getShared().setVolume((int)value);
    }

    @Override
    public boolean onMarkOrUnmarkContextTrackFavorite()
    {
        BaseAudioPlaylist currentPlaylist = Player.getShared().getPlaylist();

        if (currentPlaylist == null) {
            return false;
        }
        
        BaseAudioTrack playingTrack = currentPlaylist.getPlayingTrack();
        boolean isFavorite = GeneralStorage.getShared().favorites.isMarkedFavorite(playingTrack);
        
        if (!isFavorite) {
            GeneralStorage.getShared().favorites.markFavoriteForced(playingTrack);
        } else {
            GeneralStorage.getShared().favorites.unmarkFavorite(playingTrack);
        }
                
        return !isFavorite;
    }

    private void playFirstTime(@NonNull BaseAudioPlaylist playlist)
    {
        playNew(playlist);
    }

    private void playContinue(@NonNull BaseAudioPlaylist playlist)
    {
        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Opening player without changing current audio player state");

        _view.updatePlayerScreen(playlist);
    }

    private void playNew(@NonNull BaseAudioPlaylist playlist)
    {
        String newPlaylistName = playlist.getName();
        BaseAudioTrack newTrack = playlist.getPlayingTrack();

        Log.v(PlayerPresenterImpl.class.getCanonicalName(), "Opening player and playing new playlist '" + newPlaylistName + "' with track '" + newTrack.getTitle() + "'");

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
