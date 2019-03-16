package com.media.notabadplayer.Storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    
    private HashMap<ApplicationInput, ApplicationAction> _keyBinds = new HashMap<>();
    
    private GeneralStorage()
    {
        
    }

    synchronized public static GeneralStorage getShared()
    {
        if (singleton == null)
        {
            singleton = new GeneralStorage();
        }
        
        return singleton;
    }
    
    private void firstTimeLaunch(Context context)
    {
        resetDefaultSettingsActions(context);
    }
    
    synchronized public boolean isFirstApplicationLaunch(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        boolean firstTime = preferences.getBoolean("firstTime", true);
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstTime", false);
        editor.apply();
        
        if (firstTime)
        {
            firstTimeLaunch(context);
        }
        
        return firstTime;
    }
    
    synchronized public void savePlayerState(Context context)
    {
        AudioPlayer player = AudioPlayer.getShared();
        
        if (!player.hasPlaylist())
        {
            return;
        }
        
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("player_current_playlist", Serializing.serializeObject(player.getPlaylist()));
        editor.putInt("player_current_position", player.getCurrentPositionMSec());
        
        editor.apply();
    }
    
    synchronized public void restorePlayerState(Application application, Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        AudioPlayer player = AudioPlayer.getShared();
        
        String data = preferences.getString("player_current_playlist", "");
        
        if (data == null)
        {
            return;
        }
        
        AudioPlaylist playlist = (AudioPlaylist)Serializing.deserializeObject(data);
        
        if (playlist == null)
        {
            return;
        }
        
        player.playPlaylist(application, playlist);
        
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

    synchronized public void savePlayerPlayHistoryState(Context context)
    {
        AudioPlayer player = AudioPlayer.getShared();

        if (!player.hasPlaylist())
        {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("player_play_history", Serializing.serializeObject(player.getPlayHistory()));
        
        editor.apply();
    }

    synchronized public void restorePlayerPlayHistoryState(Application application, Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        AudioPlayer player = AudioPlayer.getShared();
        
        String data = preferences.getString("player_play_history", "");

        if (data == null)
        {
            return;
        }
        
        ArrayList<AudioTrack> playHistory = (ArrayList<AudioTrack>)Serializing.deserializeObject(data);
        
        if (playHistory == null)
        {
            return;
        }
        
        player.setPlayHistory(playHistory);
    }

    synchronized public void saveSearchQuery(Context context, String searchQuery)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("searchQuery", searchQuery);
        
        editor.apply();
    }

    synchronized public String retrieveSearchQuery(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        return preferences.getString("searchQuery", "");
    }
    
    synchronized public void resetDefaultSettingsActions(Context context)
    {
        savePlayerPlayedHistory(context, 20);
        saveAppThemeValue(context, AppSettings.AppTheme.LIGHT);
        saveAlbumSortingValue(context, AppSettings.AlbumSorting.TITLE);
        saveTrackSortingValue(context, AppSettings.TrackSorting.TRACK_NUMBER);
        saveShowStarsValue(context, AppSettings.ShowStars.NO);
        saveShowVolumeBarValue(context, AppSettings.ShowVolumeBar.NO);
        
        saveSettingsAction(context, ApplicationInput.HOME_BUTTON, ApplicationAction.DO_NOTHING);
        saveSettingsAction(context, ApplicationInput.SCREEN_LOCK_BUTTON, ApplicationAction.DO_NOTHING);
        saveSettingsAction(context, ApplicationInput.EARPHONES_UNPLUG, ApplicationAction.PAUSE);
        saveSettingsAction(context, ApplicationInput.PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(context, ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(context, ApplicationInput.PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(context, ApplicationInput.PLAYER_RECALL, ApplicationAction.PREVIOUS_PLAYED_TRACK);
        saveSettingsAction(context, ApplicationInput.PLAYER_NEXT_BUTTON, ApplicationAction.NEXT_TRACK);
        saveSettingsAction(context, ApplicationInput.PLAYER_PREVIOUS_BUTTON, ApplicationAction.PREVIOUS_TRACK);
        saveSettingsAction(context, ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(context, ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(context, ApplicationInput.QUICK_PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(context, ApplicationInput.QUICK_PLAYER_NEXT_BUTTON, ApplicationAction.JUMP_FORWARDS_15);
        saveSettingsAction(context, ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON, ApplicationAction.JUMP_BACKWARDS_15);
        
        saveCachingPolicyFlagForAlbumsTab(context, true);
        saveCachingPolicyFlagForListsTab(context, false);
        saveCachingPolicyFlagForSearchTab(context, false);
        saveCachingPolicyFlagForSettingsTab(context, false);
    }
    
    synchronized public void saveSettingsAction(Context context, ApplicationInput input, ApplicationAction action)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString(input.name(), action.name());
        
        editor.apply();
        
        _keyBinds.put(input, action);
    }
    
    synchronized public ApplicationAction getSettingsAction(Context context, ApplicationInput input)
    {
        ApplicationAction cachedAction = _keyBinds.get(input);
        
        if (cachedAction != null)
        {
            return cachedAction;
        }
        
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
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
    
    synchronized public int getPlayerPlayedHistoryCapacity(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        return preferences.getInt("player_history_capacity", 1);
    }
    
    synchronized public void savePlayerPlayedHistory(Context context, int value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("player_history_capacity", value >= 0 ? value : 0);
        editor.apply();
    }

    synchronized public AppSettings.AppTheme getAppThemeValue(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        try {
            return AppSettings.AppTheme.valueOf(preferences.getString("app_theme_value", ""));
        } 
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not read AppTheme value from storage");
        }
        
        return AppSettings.AppTheme.LIGHT;
    }

    synchronized public void saveAppThemeValue(Context context, AppSettings.AppTheme value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("app_theme_value", value.name());
        editor.apply();
    }

    synchronized public AppSettings.AlbumSorting getAlbumSortingValue(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        try {
            return AppSettings.AlbumSorting.valueOf(preferences.getString("album_sorting", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not read AlbumSorting value from storage");
        }

        return AppSettings.AlbumSorting.TITLE;
    }

    synchronized public void saveAlbumSortingValue(Context context, AppSettings.AlbumSorting value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("album_sorting", value.name());
        editor.apply();
    }

    synchronized public AppSettings.TrackSorting getTrackSortingValue(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        try {
            return AppSettings.TrackSorting.valueOf(preferences.getString("track_sorting", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not read TrackSorting value from storage");
        }

        return AppSettings.TrackSorting.TITLE;
    }

    synchronized public void saveTrackSortingValue(Context context, AppSettings.TrackSorting value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("track_sorting", value.name());
        editor.apply();
    }

    synchronized public AppSettings.ShowStars getShowStarsValue(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        try {
            return AppSettings.ShowStars.valueOf(preferences.getString("show_stars", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not read ShowStars value from storage");
        }

        return AppSettings.ShowStars.NO;
    }

    synchronized public void saveShowStarsValue(Context context, AppSettings.ShowStars value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("show_stars", value.name());
        editor.apply();
    }

    synchronized public AppSettings.ShowVolumeBar getShowVolumeBarValue(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        try {
            return AppSettings.ShowVolumeBar.valueOf(preferences.getString("show_volume_bar", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not read ShowVolumeBar value from storage");
        }

        return AppSettings.ShowVolumeBar.NO;
    }

    synchronized public void saveShowVolumeBarValue(Context context, AppSettings.ShowVolumeBar value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("show_volume_bar", value.name());
        editor.apply();
    }
    
    synchronized public boolean getCachingPolicyFlagForAlbumsTab(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("caching_policy_albums_tab", false);
    }
    
    synchronized public void saveCachingPolicyFlagForAlbumsTab(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("caching_policy_albums_tab", value);
        editor.apply();
    }
    
    synchronized public boolean getCachingPolicyFlagForListsTab(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("caching_policy_lists_tab", false);
    }
    
    synchronized public void saveCachingPolicyFlagForListsTab(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("caching_policy_lists_tab", value);
        editor.apply();
    }
    
    synchronized public boolean getCachingPolicyFlagForSearchTab(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("caching_policy_search_tab", false);
    }
    
    synchronized public void saveCachingPolicyFlagForSearchTab(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("caching_policy_search_tab", value);
        editor.apply();
    }
    
    synchronized public boolean getCachingPolicyFlagForSettingsTab(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("caching_policy_settings_tab", false);
    }
    
    synchronized public void saveCachingPolicyFlagForSettingsTab(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("caching_policy_settings_tab", value);
        editor.apply();
    }
}
