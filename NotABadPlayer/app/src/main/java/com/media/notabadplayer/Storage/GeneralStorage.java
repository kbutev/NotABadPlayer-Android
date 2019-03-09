package com.media.notabadplayer.Storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
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
        savePlayerPlayedHistory(context, 20);
        saveCachingPolicyFlagForAlbumsTab(context, true);
        saveCachingPolicyFlagForListsTab(context, false);
        saveCachingPolicyFlagForSearchTab(context, false);
        saveCachingPolicyFlagForSettingsTab(context, false);
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
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString(ApplicationInput.HOME_BUTTON.name(), ApplicationAction.DO_NOTHING.name());
        editor.putString(ApplicationInput.SCREEN_LOCK_BUTTON.name(), ApplicationAction.DO_NOTHING.name());
        editor.putString(ApplicationInput.EARPHONES_UNPLUG.name(), ApplicationAction.PAUSE.name());
        editor.putString(ApplicationInput.PLAYER_VOLUME_UP_BUTTON.name(), ApplicationAction.VOLUME_UP.name());
        editor.putString(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON.name(), ApplicationAction.VOLUME_DOWN.name());
        editor.putString(ApplicationInput.PLAYER_PLAY_BUTTON.name(), ApplicationAction.PAUSE_OR_RESUME.name());
        editor.putString(ApplicationInput.PLAYER_RECALL.name(), ApplicationAction.PREVIOUS_PLAYED_TRACK.name());
        editor.putString(ApplicationInput.PLAYER_NEXT_BUTTON.name(), ApplicationAction.NEXT_TRACK.name());
        editor.putString(ApplicationInput.PLAYER_PREVIOUS_BUTTON.name(), ApplicationAction.PREVIOUS_TRACK.name());
        editor.putString(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON.name(), ApplicationAction.VOLUME_UP.name());
        editor.putString(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON.name(), ApplicationAction.VOLUME_DOWN.name());
        editor.putString(ApplicationInput.QUICK_PLAYER_PLAY_BUTTON.name(), ApplicationAction.PAUSE_OR_RESUME.name());
        editor.putString(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON.name(), ApplicationAction.JUMP_FORWARDS_15.name());
        editor.putString(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON.name(), ApplicationAction.JUMP_BACKWARDS_15.name());
        
        editor.apply();
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
