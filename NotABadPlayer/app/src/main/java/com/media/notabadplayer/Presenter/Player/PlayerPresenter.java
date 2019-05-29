package com.media.notabadplayer.Presenter.Player;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerPresenter implements BasePresenter
{
    private @NonNull BaseView _view;
    private @NonNull AudioPlaylist _playlist;
    
    public PlayerPresenter(@NonNull BaseView view, 
                           @NonNull AudioPlaylist playlist)
    {
        _view = view;
        _playlist = playlist;
    }
    
    @Override
    public void start() 
    {
        AudioPlayer player = AudioPlayer.getShared();
        AudioPlaylist currentPlaylist = player.getPlaylist();
        
        if (currentPlaylist != null)
        {
            String newPlaylistName = _playlist.getName();
            String currentPlaylistName = currentPlaylist.getName();

            AudioTrack newTrack = _playlist.getPlayingTrack();
            AudioTrack currentTrack = currentPlaylist.getPlayingTrack();

            // Current playing playlist or track does not match the state of the presenter's playlist?
            if (!newPlaylistName.equals(currentPlaylistName) || !newTrack.equals(currentTrack))
            {
                // Change the audio player playlist to equal the presenter's playlist
                Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player screen and playing track '" + newTrack.title + "'");
                playNew(_playlist);

                return;
            }
            
            // Just open screen
            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player screen with current audio player track '" + currentTrack.title + "'");
            playContinue(currentPlaylist);
            
            return;
        }

        // Set audio player playlist for the first time and play its track
        Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player screen for the first time and playing track '" + _playlist.getPlayingTrack().title + "'");
        playFirstTime(_playlist);
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
        ApplicationAction action = KeyBinds.getShared().getActionForInput(input);
        
        Log.v(PlayerPresenter.class.getCanonicalName(), "Perform KeyBinds action '" + action.name() + "' for input '" + input.name() + "'");
        
        KeyBinds.getShared().performAction(action);
    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }

    @Override
    public void onPlayOrderButtonClick()
    {
        Log.v(PlayerPresenter.class.getCanonicalName(), "Player input: change play order");
        
        KeyBinds.getShared().performAction(ApplicationAction.CHANGE_PLAY_ORDER);
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

    private void playFirstTime(@NonNull AudioPlaylist playlist)
    {
        playNew(playlist);
    }

    private void playContinue(@NonNull AudioPlaylist playlist)
    {
        Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player without changing current audio player state");

        _view.updatePlayerScreen(playlist);
    }

    private void playNew(@NonNull AudioPlaylist playlist)
    {
        String newPlaylistName = playlist.getName();
        AudioTrack newTrack = playlist.getPlayingTrack();

        Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing new playlist '" + newPlaylistName + "' with track '" + newTrack.title + "'");

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

        _view.updatePlayerScreen(playlist);
    }
}
