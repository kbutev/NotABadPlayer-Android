package com.media.notabadplayer.Presenter.Settings;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Other.AudioPlayerTimerValue;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.MVP.BaseView;
import com.media.notabadplayer.View.Settings.SettingsView;

public class SettingsPresenterImpl implements SettingsPresenter
{
    private SettingsView _view;

    private boolean _running = false;
    
    private boolean _fetchingData = false;
    
    public SettingsPresenterImpl()
    {
        
    }

    // SettingsPresenter

    @Override
    public void start() {
        if (_view == null)
        {
            throw new IllegalStateException("SettingsPresenter: view has not been set");
        }

        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Start.");

        fetchData();
    }

    @Override
    public void onDestroy()
    {

    }
    
    @Override
    public void setView(@NonNull SettingsView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("SettingsPresenter: view has already been set");
        }
        
        _view = view;
    }

    @Override
    public void fetchData()
    {
        if (_fetchingData)
        {
            return;
        }

        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Fetching app settings...");
        
        _fetchingData = true;

        final boolean running = _running;

        // Wait for the app start running
        // Then, update the view on the main thread
        Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable;

                if (running)
                {
                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Retrieved app settings, updating view");

                            _fetchingData = false;
                            
                            updateSettingsData();
                        }
                    };
                }
                else
                {
                    myRunnable = new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Presenter is not ready to fetch yet!");

                            _fetchingData = false;
                            
                            _view.onFetchDataErrorEncountered(new RuntimeException("Presenter is not ready to fetch yet"));
                        }
                    };
                }

                mainHandler.post(myRunnable);
            }
        });

        handler.post(thread);
    }

    @Override
    public void onAppStateChange(AppState state)
    {
        _running = state.isRunning();
    }

    @Override
    public void onAppSettingsReset() 
    {
        if (!_running)
        {
            return;
        }

        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Resetting app settings to their defaults...");

        GeneralStorage storage = GeneralStorage.getShared();
        Player player = Player.getShared();

        // Reset storage
        storage.resetDefaultSettingsValues();

        // Update player state (storage does not change the player directly)
        player.setPlayOrder(storage.retrievePlayerStatePlayOrder());

        // Alert view delegate
        _view.onResetAppSettings();
        _view.onAppThemeChanged(storage.getAppThemeValue());

        // Always unmute and pause the player
        player.unmute();
        player.pause();

        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Finished resetting app settings!");
    }

    @Override
    public void onAppThemeChange(AppSettings.AppTheme themeValue) {
        if (!_running)
        {
            return;
        }
        
        if (themeValue == GeneralStorage.getShared().getAppThemeValue())
        {
            return;
        }

        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Save picked settings AppTheme value " + themeValue.name());
        
        GeneralStorage.getShared().saveAppThemeValue(themeValue);
        
        _view.onAppThemeChanged(themeValue);
    }
    
    @Override
    public void onAppTrackSortingChange(AppSettings.TrackSorting trackSorting)
    {
        if (!_running)
        {
            return;
        }
        
        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Save picked settings ShowVolumeBar value " + trackSorting.name());
        
        GeneralStorage.getShared().saveTrackSortingValue(trackSorting);
        
        _view.onAppTrackSortingChanged(trackSorting);
    }
    
    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {
        if (!_running)
        {
            return;
        }
        
        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Save picked settings ShowVolumeBar value " + value.name());
        
        GeneralStorage.getShared().saveShowVolumeBarValue(value);

        // Since the volume icon may no longer be visible after this change, always unmute & pause
        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Setting ShowVolumeBar was changed, automatically unmuting and pausing player");
        Player.getShared().unmute();
        Player.getShared().pause();
    }

    @Override
    public void onOpenPlayerOnPlaySettingChange(AppSettings.OpenPlayerOnPlay value)
    {
        if (!_running)
        {
            return;
        }
        
        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Save picked settings OpenPlayerOnPlay value " + value.name());
        
        GeneralStorage.getShared().saveOpenPlayerOnPlayValue(value);
    }
    
    @Override
    public void onKeybindChange(ApplicationAction action, ApplicationInput input) 
    {
        if (!_running)
        {
            return;
        }
        
        Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Save picked keybind value of action " + action.name() + " for input " + input.name());
        
        GeneralStorage.getShared().saveSettingsAction(input, action);
        
        // If the player volume keybind was changed, always unmute & pause
        if (input == ApplicationInput.PLAYER_VOLUME)
        {
            Log.v(SettingsPresenterImpl.class.getCanonicalName(), "Keybind PLAYER_VOLUME was changed, automatically unmuting and pausing player");
            
            Player.getShared().unmute();
            Player.getShared().pause();
        }
    }

    @Override
    public void onAudioIdleTimerValueChange(AudioPlayerTimerValue value)
    {
        if (!_running)
        {
            return;
        }

        GeneralStorage.getShared().saveAudioIdleStopTimer(value);
    }

    private void updateSettingsData()
    {
        _view.onAppSettingsLoad(GeneralStorage.getShared());
    }
}
