package com.media.notabadplayer.Presenter.Player;

import android.support.annotation.NonNull;
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
            
            if (!newPlaylistName.equals(currentPlaylistName) || !newTrack.equals(currentTrack))
            {
                Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing new playlist " + _playlist.getName() + " with track " + _playlist.getPlayingTrack().title);

                player.playPlaylist(_playlist);

                if (!player.isPlaying())
                {
                    player.resume();
                }

                _view.openPlayerScreen(_playlist);

                return;
            }

            Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player without changing current audio player state");
            
            _view.openPlayerScreen(currentPlaylist);
            
            return;
        }

        Log.v(PlayerPresenter.class.getCanonicalName(), "Opening player and playing playlist with track " + _playlist.getPlayingTrack().title);

        player.playPlaylist(_playlist);

        if (!player.isPlaying())
        {
            player.resume();
        }
        
        _view.openPlayerScreen(_playlist);
    }
    
    @Override
    public void onAlbumClick(int index) 
    {
        
    }
    
    @Override
    public void onAlbumsItemClick(int index)
    {
        
    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input)
    {
        KeyBinds.getShared().evaluateInput(input);
    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }

    @Override
    public void onPlayOrderButtonClick()
    {
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
    public void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }

    @Override
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {

    }
}
