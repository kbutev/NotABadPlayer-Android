package com.media.notabadplayer.Presenter.Player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Other.AudioPlayerTimerValue;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.MVP.BaseView;
import com.media.notabadplayer.View.Player.QuickPlayerView;

public class QuickPlayerPresenterImpl implements QuickPlayerPresenter
{
    private QuickPlayerView _view;

    private @NonNull AudioInfo _audioInfo;
    
    private boolean _running = false;

    public QuickPlayerPresenterImpl(@NonNull AudioInfo audioInfo)
    {
        this._audioInfo = audioInfo;
    }

    // QuickPlayerPresenter

    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("QuickPlayerPresenter: view has not been set");
        }

        Log.v(QuickPlayerPresenterImpl.class.getCanonicalName(), "Start.");
    }

    @Override
    public void setView(@NonNull QuickPlayerView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("PlayerPresenter: view has already been set");
        }
        
        _view = view;
    }

    @Override
    public void onDestroy()
    {
        Log.v(QuickPlayerPresenterImpl.class.getCanonicalName(), "Destroyed.");

        _running = false;
    }

    @Override
    public void onAppStateChange(AppState state)
    {
        _running = state.isRunning();
    }

    @Override
    public void onOpenPlayer(@Nullable BaseAudioPlaylist playlist)
    {
        if (!_running)
        {
            return;
        }

        BaseAudioPlaylist currentlyPlayingPlaylist = Player.getShared().getPlaylist();

        if (currentlyPlayingPlaylist == null)
        {
            return;
        }

        Log.v(QuickPlayerPresenterImpl.class.getCanonicalName(), "Open player screen");
        
        _view.openPlayerScreen(currentlyPlayingPlaylist);
    }

    @Override
    public void onPlayerButtonClick(ApplicationInput input)
    {
        if (!_running)
        {
            return;
        }
        
        Exception exception = KeyBinds.getShared().evaluateInput(input);
        
        if (exception != null)
        {
            _view.onPlayerErrorEncountered(exception);
        }
    }

    @Override
    public void onPlayOrderButtonClick()
    {
        if (!_running)
        {
            return;
        }

        Exception exception = KeyBinds.getShared().performAction(ApplicationAction.CHANGE_PLAY_ORDER);

        if (exception != null)
        {
            _view.onPlayerErrorEncountered(exception);
        }
    }
    
    @Override
    public void onOpenPlaylistButtonClick()
    {
        if (!_running)
        {
            return;
        }

        BaseAudioPlaylist currentlyPlayingPlaylist = Player.getShared().getPlaylist();
        
        if (currentlyPlayingPlaylist != null)
        {
            try {
                _view.openPlaylistScreen(_audioInfo, currentlyPlayingPlaylist, OpenPlaylistOptions.buildDefault());
            } catch (Exception e) {
                Log.v(QuickPlayerPresenterImpl.class.getCanonicalName(), "Failed to open playlist screen, error: " + e);
            }
        }
    }
}
