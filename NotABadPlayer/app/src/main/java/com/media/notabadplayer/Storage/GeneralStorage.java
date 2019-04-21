package com.media.notabadplayer.Storage;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Utilities.Serializing;

import java.util.ArrayList;
import java.util.HashMap;

public class GeneralStorage
{
    private static GeneralStorage singleton;
    
    private Application ___context;
    
    private SharedPreferences ___preferences;
    
    private boolean _firstTimeLaunch;
    
    private HashMap<ApplicationInput, ApplicationAction> _keyBinds = new HashMap<>();
    
    private GeneralStorage()
    {
        _firstTimeLaunch = true;
    }

    synchronized public static GeneralStorage getShared()
    {
        if (singleton == null)
        {
            singleton = new GeneralStorage();
        }
        
        return singleton;
    }
    
    public void init(@NonNull Application context)
    {
        ___context = context;
        
        ___preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), context.MODE_PRIVATE);
        
        detectFirstTimeLaunch();
    }
    
    private Application getContext()
    {
        if (___context == null)
        {
            throw new UncheckedExecutionException(new Exception("GeneralStorage cannot be used before being initialized (call init())"));
        }
        
        return ___context;
    }
    
    private SharedPreferences getSharedPreferences()
    {
        if (___context == null)
        {
            throw new UncheckedExecutionException(new Exception("GeneralStorage cannot be used before being initialized (call init())"));
        }
        
        return ___preferences;
    }
    
    private void detectFirstTimeLaunch()
    {
        this._firstTimeLaunch = getSharedPreferences().getBoolean("firstTime", true);
        
        if (this._firstTimeLaunch)
        {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
            
            resetDefaultSettingsActions();
        }
    }
    
    synchronized public boolean isFirstApplicationLaunch()
    {
        return _firstTimeLaunch;
    }
    
    synchronized public void savePlayerState()
    {
        AudioPlayer player = AudioPlayer.getShared();
        
        if (!player.hasPlaylist())
        {
            return;
        }
        
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        
        editor.putString("player_current_playlist", Serializing.serializeObject(player.getPlaylist()));
        editor.putInt("player_current_position", player.getCurrentPositionMSec());
        
        editor.apply();
    }
    
    synchronized public void restorePlayerState(@NonNull Application application, @NonNull AudioInfo audioInfo)
    {
        SharedPreferences preferences = getSharedPreferences();
        AudioPlayer player = AudioPlayer.getShared();
        
        String data = preferences.getString("player_current_playlist", "");
        
        if (data == null)
        {
            return;
        }

        Object result = Serializing.deserializeObject(data);

        if (!(result instanceof AudioPlaylist))
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not restore player state, the stored data is invalid");
            return;
        }
        
        AudioPlaylist playlist = (AudioPlaylist)result;
        
        player.playPlaylist(playlist);
        
        AudioPlaylist newPlayerPlaylist = player.getPlaylist();
        
        if (newPlayerPlaylist != null)
        {
            newPlayerPlaylist.setPlayOrder(playlist.getPlayOrder());
        }
        
        int positionMSec = preferences.getInt("player_current_position", 0);
        
        player.seekTo(positionMSec);
        
        // Always pause by default when restoring state from storage
        player.pause();
    }

    synchronized public void savePlayerPlayHistoryState()
    {
        SharedPreferences preferences = getSharedPreferences();
        AudioPlayer player = AudioPlayer.getShared();

        if (!player.hasPlaylist())
        {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("player_play_history", Serializing.serializeObject(player.getPlayHistory()));
        
        editor.apply();
    }

    synchronized public void restorePlayerPlayHistoryState(@NonNull Application application)
    {
        SharedPreferences preferences = getSharedPreferences();
        AudioPlayer player = AudioPlayer.getShared();
        
        String data = preferences.getString("player_play_history", "");

        if (data == null)
        {
            return;
        }
        
        Object playHistory = Serializing.deserializeObject(data);
        
        if (!(playHistory instanceof ArrayList))
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not restore player history state, the stored data is invalid");
            return;
        }
        
        player.setPlayHistory((ArrayList<AudioTrack>)playHistory);
    }

    synchronized public void saveSearchQuery(String searchQuery)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("searchQuery", searchQuery);
        
        editor.apply();
    }

    synchronized public String retrieveSearchQuery()
    {
        return getSharedPreferences().getString("searchQuery", "");
    }
    
    synchronized public void resetDefaultSettingsActions()
    {
        savePlayerPlayedHistory(30);
        saveAppThemeValue(AppSettings.AppTheme.LIGHT);
        saveAlbumSortingValue(AppSettings.AlbumSorting.TITLE);
        saveTrackSortingValue(AppSettings.TrackSorting.TRACK_NUMBER);
        saveShowStarsValue(AppSettings.ShowStars.NO);
        saveShowVolumeBarValue(AppSettings.ShowVolumeBar.NO);
        
        saveSettingsAction(ApplicationInput.PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(ApplicationInput.PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(ApplicationInput.PLAYER_RECALL, ApplicationAction.RECALL);
        saveSettingsAction(ApplicationInput.PLAYER_NEXT_BUTTON, ApplicationAction.NEXT);
        saveSettingsAction(ApplicationInput.PLAYER_PREVIOUS_BUTTON, ApplicationAction.PREVIOUS);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON, ApplicationAction.FORWARDS_15);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON, ApplicationAction.BACKWARDS_15);
        saveSettingsAction(ApplicationInput.EARPHONES_UNPLUG, ApplicationAction.PAUSE);
        
        saveCachingPolicy(AppSettings.CachingPolicies.ALBUMS_ONLY);
    }
    
    synchronized public void saveSettingsAction(ApplicationInput input, ApplicationAction action)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString(input.name(), action.name());
        
        editor.apply();
        
        _keyBinds.put(input, action);
    }
    
    synchronized public ApplicationAction getSettingsAction(ApplicationInput input)
    {
        SharedPreferences preferences = getSharedPreferences();
        ApplicationAction cachedAction = _keyBinds.get(input);
        
        if (cachedAction != null)
        {
            return cachedAction;
        }
        
        String actionStr = preferences.getString(input.name(), "");
        
        try {
            ApplicationAction action = ApplicationAction.valueOf(actionStr);
            _keyBinds.put(input, action);
            return action;
        }
        catch (Exception e) {
            
        }
        
        return ApplicationAction.DO_NOTHING;
    }
    
    synchronized public int getPlayerPlayedHistoryCapacity()
    {
        return getSharedPreferences().getInt("player_history_capacity", 1);
    }
    
    synchronized public void savePlayerPlayedHistory(int value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("player_history_capacity", value >= 0 ? value : 0);
        editor.apply();
    }

    synchronized public ArrayList<AudioPlaylist> getPlaylists()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            Object result = Serializing.deserializeObject(preferences.getString("playlists", ""));
            
            if (!(result instanceof ArrayList))
            {
                Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not deserialize playlists, the stored data is invalid");
                return null;
            }
            
            return (ArrayList<AudioPlaylist>)result;
        }
        catch (Exception e)
        {
            
        }
        
        return null;
    }

    synchronized public void savePlaylists(@NonNull ArrayList<AudioPlaylist> playlists)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("playlists", Serializing.serializeObject(playlists));
        editor.apply();
    }

    synchronized public AppSettings.AppTheme getAppThemeValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.AppTheme.valueOf(preferences.getString("app_theme_value", ""));
        } 
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read AppThemeSetter value from storage");
        }
        
        return AppSettings.AppTheme.LIGHT;
    }

    synchronized public void saveAppThemeValue(AppSettings.AppTheme value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("app_theme_value", value.name());
        editor.apply();
    }

    synchronized public AppSettings.AlbumSorting getAlbumSortingValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.AlbumSorting.valueOf(preferences.getString("album_sorting", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read AlbumSorting value from storage");
        }

        return AppSettings.AlbumSorting.TITLE;
    }

    synchronized public void saveAlbumSortingValue(AppSettings.AlbumSorting value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("album_sorting", value.name());
        editor.apply();
    }

    synchronized public AppSettings.TrackSorting getTrackSortingValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.TrackSorting.valueOf(preferences.getString("track_sorting", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read TrackSorting value from storage");
        }

        return AppSettings.TrackSorting.TITLE;
    }

    synchronized public void saveTrackSortingValue(AppSettings.TrackSorting value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("track_sorting", value.name());
        editor.apply();
    }

    synchronized public AppSettings.ShowStars getShowStarsValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.ShowStars.valueOf(preferences.getString("show_stars", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read ShowStars value from storage");
        }

        return AppSettings.ShowStars.NO;
    }

    synchronized public void saveShowStarsValue(AppSettings.ShowStars value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("show_stars", value.name());
        editor.apply();
    }

    synchronized public AppSettings.ShowVolumeBar getShowVolumeBarValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.ShowVolumeBar.valueOf(preferences.getString("show_volume_bar", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read ShowVolumeBar value from storage");
        }

        return AppSettings.ShowVolumeBar.NO;
    }

    synchronized public void saveShowVolumeBarValue(AppSettings.ShowVolumeBar value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("show_volume_bar", value.name());
        editor.apply();
    }
    
    synchronized public AppSettings.CachingPolicies getCachingPolicy()
    {
        SharedPreferences preferences = getSharedPreferences();

        try {
            return AppSettings.CachingPolicies.valueOf(preferences.getString("caching_policy", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read CachingPolicies value from storage");
        }

        return AppSettings.CachingPolicies.NO_CACHING;
    }
    
    synchronized public void saveCachingPolicy(AppSettings.CachingPolicies value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("caching_policy", value.name());
        editor.apply();
    }
}
