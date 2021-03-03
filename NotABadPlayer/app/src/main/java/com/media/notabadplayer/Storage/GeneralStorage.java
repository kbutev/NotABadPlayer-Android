package com.media.notabadplayer.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.AudioTrackBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.MutableAudioPlaylist;
import com.media.notabadplayer.Audio.Other.AudioPlayerTimerValue;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.SearchFilter;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.PlayerApplication;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.CollectionUtilities;
import com.media.notabadplayer.Utilities.Serializing;

// Provides simple interface to the user preferences (built in storage).
public class GeneralStorage
{
    public static final int PLAYER_HISTORY_CAPACITY = 50;

    private static GeneralStorage singleton;
    
    public final FavoritesStorage favorites;
    
    private Application _context;
    private SharedPreferences _preferences;
    
    private boolean _firstTimeLaunch;
    
    private HashMap<ApplicationInput, ApplicationAction> _keyBinds = new HashMap<>();
    private boolean _keyBindsFullyRetrieved = false;
    
    private GeneralStorage()
    {
        _firstTimeLaunch = true;

        Log.v(GeneralStorage.class.getCanonicalName(), "Initializing...");

        _context = PlayerApplication.getShared();

        _preferences = _context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        favorites = new FavoritesStorage(_preferences);

        detectFirstTimeLaunch();

        detectVersionChange();

        Log.v(GeneralStorage.class.getCanonicalName(), "Initialized!");
    }

    public synchronized static GeneralStorage getShared()
    {
        if (singleton == null)
        {
            singleton = new GeneralStorage();
        }
        
        return singleton;
    }
    
    private @NonNull Application getContext()
    {
        return _context;
    }
    
    private @NonNull SharedPreferences getSharedPreferences()
    {
        return _preferences;
    }
    
    private void detectFirstTimeLaunch()
    {
        this._firstTimeLaunch = getSharedPreferences().getBoolean("firstTime", true);
        
        if (this._firstTimeLaunch)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "First time launching the program!");
            
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putBoolean("firstTime", false);
            editor.apply();

            clearCurrentlySelectedNavigationTab();

            resetDefaultSettingsValues();
        }
    }
    
    private void detectVersionChange()
    {
        Application context = getContext();
        
        String preferencesVersion = getStorageVersion();
        String currentVersion = context.getResources().getString(R.string.storage_version);
        
        if (preferencesVersion.equals(currentVersion))
        {
            return;
        }

        SharedPreferences preferences = getSharedPreferences();
        
        saveStorageVersion(currentVersion);
        
        // Migrate from version to version, one by one
        // Migrate to 1.0
        if (preferencesVersion.equals(""))
        {
            String version = "1.0";
            
            Log.v(GeneralStorage.class.getCanonicalName(), "Migrating settings from first version to version " + version);

            String playlistsData = preferences.getString("user_playlists", "");
            
            if (playlistsData != null)
            {
                try {
                    List<BaseAudioPlaylist> result = AudioPlaylistBuilder.buildArrayListFromSerializedData(playlistsData);
                    saveUserPlaylists(result);
                } catch (Exception e) {
                    Log.v(GeneralStorage.class.getCanonicalName(), "Failed to migrate user playlists: " + e.toString());
                }
            }

            preferencesVersion = version;
        }

        // Migrate to 1.1
        if (preferencesVersion.equals("1.0"))
        {
            String version = "1.1";
            
            Log.v(GeneralStorage.class.getCanonicalName(), "Migrating settings from version " + preferencesVersion + " to version " + version);
            
            saveSettingsAction(ApplicationInput.PLAYER_SWIPE_LEFT, ApplicationAction.PREVIOUS);
            saveSettingsAction(ApplicationInput.PLAYER_SWIPE_RIGHT, ApplicationAction.NEXT);

            preferencesVersion = version;
        }

        // Migrate to 1.2
        if (preferencesVersion.equals("1.1"))
        {
            String version = "1.2";
            
            Log.v(GeneralStorage.class.getCanonicalName(), "Migrating settings from version " + preferencesVersion + " to version " + version);

            SharedPreferences.Editor editor = getSharedPreferences().edit();

            editor.putString("player_play_order", AudioPlayOrder.FORWARDS.name());

            editor.apply();

            preferencesVersion = version;
        }

        // Migrate to 1.3
        if (preferencesVersion.equals("1.2"))
        {
            String version = "1.3";
            
            Log.v(GeneralStorage.class.getCanonicalName(), "Migrating settings from version " + preferencesVersion + " to version " + version);

            SharedPreferences.Editor editor = getSharedPreferences().edit();

            editor.putString("open_player_on_play", AppSettings.OpenPlayerOnPlay.NO.name());

            editor.apply();
            
            preferencesVersion = version;
        }

        // Migrate to 1.4
        if (preferencesVersion.equals("1.3"))
        {
            String version = "1.4";

            Log.v(GeneralStorage.class.getCanonicalName(), "Migrating settings from version " + preferencesVersion + " to version " + version);

            // Class AudioTrack was completely changed, it can no longer be deserialized by older versions
            // Wipe out any data containing audio tracks
            clearPlayerPlayHistoryState();
            clearUserPlaylists();

            preferencesVersion = version;
        }
        
        Log.v(GeneralStorage.class.getCanonicalName(), "Successfully migrated settings values!");
    }

    public void resetDefaultSettingsValues()
    {
        Log.v(GeneralStorage.class.getCanonicalName(), "Resetting values to their defaults");

        resetPlayerState();
        savePlayerPlayedHistoryCapacity(PLAYER_HISTORY_CAPACITY);
        saveAppThemeValue(AppSettings.AppTheme.LIGHT);
        saveAlbumSortingValue(AppSettings.AlbumSorting.TITLE);
        saveTrackSortingValue(AppSettings.TrackSorting.TRACK_NUMBER);
        saveShowVolumeBarValue(AppSettings.ShowVolumeBar.NO);
        saveOpenPlayerOnPlayValue(AppSettings.OpenPlayerOnPlay.NO);
        saveAudioIdleStopTimer(AudioPlayerTimerValue.NONE);

        saveSettingsAction(ApplicationInput.PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(ApplicationInput.PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(ApplicationInput.PLAYER_RECALL, ApplicationAction.RECALL);
        saveSettingsAction(ApplicationInput.PLAYER_NEXT_BUTTON, ApplicationAction.NEXT);
        saveSettingsAction(ApplicationInput.PLAYER_PREVIOUS_BUTTON, ApplicationAction.PREVIOUS);
        saveSettingsAction(ApplicationInput.PLAYER_SWIPE_LEFT, ApplicationAction.PREVIOUS);
        saveSettingsAction(ApplicationInput.PLAYER_SWIPE_RIGHT, ApplicationAction.NEXT);
        saveSettingsAction(ApplicationInput.PLAYER_VOLUME, ApplicationAction.MUTE_OR_UNMUTE);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON, ApplicationAction.VOLUME_UP);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON, ApplicationAction.VOLUME_DOWN);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_PLAY_BUTTON, ApplicationAction.PAUSE_OR_RESUME);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON, ApplicationAction.FORWARDS_8);
        saveSettingsAction(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON, ApplicationAction.BACKWARDS_8);
        saveSettingsAction(ApplicationInput.EARPHONES_UNPLUG, ApplicationAction.PAUSE);
        saveSettingsAction(ApplicationInput.EXTERNAL_PLAY, ApplicationAction.PAUSE);

        saveCachingPolicy(AppSettings.TabCachingPolicies.ALBUMS_ONLY);

        saveSearchQuery("");
        saveSearchQueryFilter(SearchFilter.Title);
        
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        String userPlaylistsEmpty = Serializing.serializeObject(new ArrayList<>());

        if (userPlaylistsEmpty != null)
        {
            editor.putString("user_playlists", userPlaylistsEmpty);
        }
        
        editor.apply();
    }
    
    public boolean isFirstApplicationLaunch()
    {
        return _firstTimeLaunch;
    }

    private void saveStorageVersion(@NonNull String version)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("storage_version", version);

        editor.apply();
    }

    private @NonNull String getStorageVersion()
    {
        String storageVersion = getSharedPreferences().getString("storage_version", "");
        
        if (storageVersion == null)
        {
            return "";
        }
        
        return storageVersion;
    }

    public void resetPlayerState()
    {
        AudioPlayOrder defaultPlayOrder = AudioPlayOrder.FORWARDS;

        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.putString("player_play_order", defaultPlayOrder.name());

        editor.apply();
    }

    public void savePlayerState()
    {
        Player player = Player.getShared();
        BaseAudioPlaylist playlist = player.getPlaylist();

        if (playlist == null)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Cannot save player state yet, its not initialized yet!");
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences().edit();
        MutableAudioPlaylist playlistToSave;

        try {
            playlistToSave = AudioPlaylistBuilder.buildMutableFromImmutable(playlist);
        } catch (Exception e) {
            Log.v(GeneralStorage.class.getCanonicalName(), "Failed to save player state, playlist could not be serialized!");
            return;
        }

        editor.putString("player_current_playlist", Serializing.serializeObject(playlistToSave));

        editor.putString("player_play_order", player.getPlayOrder().name());

        editor.putInt("player_current_position", player.getCurrentPositionMSec());

        editor.apply();

        Log.v(GeneralStorage.class.getCanonicalName(), "Saved audio player state to storage.");
    }

    public @Nullable MutableAudioPlaylist retrievePlayerStateCurrentPlaylist()
    {
        SharedPreferences preferences = getSharedPreferences();

        String playlistData = preferences.getString("player_current_playlist", "");

        // Restore playlist
        Object result = Serializing.deserializeObject(playlistData);

        if (!(result instanceof MutableAudioPlaylist))
        {
            return null;
        }

        return (MutableAudioPlaylist)result;
    }

    public AudioPlayOrder retrievePlayerStatePlayOrder()
    {
        SharedPreferences preferences = getSharedPreferences();

        String playOrderData = preferences.getString("player_play_order", "");

        try {
            return AudioPlayOrder.valueOf(playOrderData);
        } catch (Exception e) {

        }

        return AudioPlayOrder.FORWARDS;
    }

    public int retrievePlayerStatePlayPosition()
    {
        SharedPreferences preferences = getSharedPreferences();

        int positionMSec = preferences.getInt("player_current_position", 0);

        return positionMSec;
    }

    public void savePlayerPlayHistoryState()
    {
        SharedPreferences preferences = getSharedPreferences();
        Player player = Player.getShared();

        if (!player.hasPlaylist())
        {
            return;
        }
        
        SharedPreferences.Editor editor = preferences.edit();
        
        ArrayList<BaseAudioTrack> history = new ArrayList<>(player.playHistory.getPlayHistory());

        editor.putString("player_play_history", Serializing.serializeObject(history));
        
        editor.apply();
    }

    public void clearPlayerPlayHistoryState()
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("player_play_history", null);

        editor.apply();

        Log.v(GeneralStorage.class.getCanonicalName(), "Cleared audio player history from storage.");
    }

    public @Nullable List<BaseAudioTrack> retrievePlayerPlayHistoryState()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        String data = preferences.getString("player_play_history", "");

        if (data == null)
        {
            return null;
        }

        try {
            return AudioTrackBuilder.buildArrayListFromSerializedData(data);
        } catch (Exception e) {
            Log.v(GeneralStorage.class.getCanonicalName(), "Failed to retrieve player play history from storage: " + e.toString());
        }

        return null;
    }

    public void saveSearchQuery(String searchQuery)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("searchQuery", searchQuery);
        
        editor.apply();
    }

    public String retrieveSearchQuery()
    {
        return getSharedPreferences().getString("searchQuery", "");
    }

    public void saveSearchQueryFilter(SearchFilter searchFilter)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("searchQueryFilter", searchFilter.name());

        editor.apply();
    }

    public SearchFilter retrieveSearchQueryFilter()
    {
        SharedPreferences preferences = getSharedPreferences();

        String searchFilter = preferences.getString("searchQueryFilter", "");

        try {
            return SearchFilter.valueOf(searchFilter);
        }
        catch (Exception e) {

        }

        return SearchFilter.Title;
    }

    public void saveSettingsAction(ApplicationInput input, ApplicationAction action)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString(input.name(), action.name());
        
        editor.apply();
        
        _keyBinds.put(input, action);
    }
    
    public ApplicationAction getSettingsAction(ApplicationInput input)
    {
        ApplicationAction cachedAction = _keyBinds.get(input);
        
        if (cachedAction != null)
        {
            return cachedAction;
        }

        SharedPreferences preferences = getSharedPreferences();
        
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

    public Map<ApplicationInput, ApplicationAction> retrieveAllSettingsActionValues()
    {
        if (_keyBindsFullyRetrieved)
        {
            return CollectionUtilities.copy(_keyBinds);
        }

        _keyBindsFullyRetrieved = true;

        _keyBinds.clear();

        SharedPreferences preferences = getSharedPreferences();

        for (ApplicationInput input : ApplicationInput.values())
        {
            String value = preferences.getString(input.name(), "");

            try {
                ApplicationAction action = ApplicationAction.valueOf(value);
                _keyBinds.put(input, action);
            }
            catch (Exception e) {

            }
        }

        return CollectionUtilities.copy(_keyBinds);
    }
    
    public int getPlayerPlayedHistoryCapacity()
    {
        return getSharedPreferences().getInt("player_history_capacity", 1);
    }
    
    public void savePlayerPlayedHistoryCapacity(int value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("player_history_capacity", value >= 0 ? value : 0);
        editor.apply();
    }

    public @NonNull List<BaseAudioPlaylist> getUserPlaylists()
    {
        SharedPreferences preferences = getSharedPreferences();

        try {
            String data = preferences.getString("user_playlists", "");

            List<BaseAudioPlaylist> result = AudioPlaylistBuilder.buildArrayListFromSerializedData(data);

            return result;
        }
        catch (Exception e)
        {

        }
        
        return new ArrayList<>();
    }

    public void saveUserPlaylists(@NonNull List<BaseAudioPlaylist> playlists)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_playlists", Serializing.serializeObject(new ArrayList<>(playlists)));
        editor.apply();
    }

    public void clearUserPlaylists()
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_playlists", null);
        editor.apply();
    }

    public AppSettings.AppTheme getAppThemeValue()
    {
        SharedPreferences preferences = getSharedPreferences();
        
        try {
            return AppSettings.AppTheme.valueOf(preferences.getString("app_theme_value", ""));
        } 
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read AppThemeUtility value from storage");
        }
        
        return AppSettings.AppTheme.LIGHT;
    }
    
    public static AppSettings.AppTheme getAppTheme(@NonNull Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);

        try {
            return AppSettings.AppTheme.valueOf(preferences.getString("app_theme_value", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read AppThemeUtility value from storage");
        }

        return AppSettings.AppTheme.LIGHT;
    }

    public void saveAppThemeValue(AppSettings.AppTheme value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("app_theme_value", value.name());
        editor.apply();
    }

    public AppSettings.AlbumSorting getAlbumSortingValue()
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

    public void saveAlbumSortingValue(AppSettings.AlbumSorting value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("album_sorting", value.name());
        editor.apply();
    }

    public AppSettings.TrackSorting getTrackSortingValue()
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

    public void saveTrackSortingValue(AppSettings.TrackSorting value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("track_sorting", value.name());
        editor.apply();
    }
    
    public AppSettings.ShowVolumeBar getShowVolumeBarValue()
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

    public void saveShowVolumeBarValue(AppSettings.ShowVolumeBar value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("show_volume_bar", value.name());
        editor.apply();
    }

    public AppSettings.OpenPlayerOnPlay getOpenPlayerOnPlayValue()
    {
        SharedPreferences preferences = getSharedPreferences();

        try {
            return AppSettings.OpenPlayerOnPlay.valueOf(preferences.getString("open_player_on_play", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read OpenPlayerOnPlay value from storage");
        }

        return AppSettings.OpenPlayerOnPlay.NO;
    }

    public void saveOpenPlayerOnPlayValue(AppSettings.OpenPlayerOnPlay value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("open_player_on_play", value.name());
        editor.apply();
    }

    public int getCurrentlySelectedNavigationTab()
    {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getInt("selected_navigation_tab", 0);
    }

    public void saveCurrentlySelectedNavigationTab(int tabId)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selected_navigation_tab", tabId);
        editor.apply();
    }

    public void clearCurrentlySelectedNavigationTab()
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selected_navigation_tab", 0);
        editor.apply();
    }
    
    public AppSettings.TabCachingPolicies getCachingPolicy()
    {
        SharedPreferences preferences = getSharedPreferences();

        try {
            return AppSettings.TabCachingPolicies.valueOf(preferences.getString("caching_policy", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read TabCachingPolicies value from storage");
        }

        return AppSettings.TabCachingPolicies.NO_CACHING;
    }
    
    public void saveCachingPolicy(AppSettings.TabCachingPolicies value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("caching_policy", value.name());
        editor.apply();
    }

    public AudioPlayerTimerValue getAudioIdleStopTimer()
    {
        SharedPreferences preferences = getSharedPreferences();

        try {
            return AudioPlayerTimerValue.valueOf(preferences.getString("audio_idle_stop_timer", ""));
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Error: could not read AudioPlayerTimerValue value from storage");
        }

        return AudioPlayerTimerValue.NONE;
    }

    public void saveAudioIdleStopTimer(AudioPlayerTimerValue value)
    {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("audio_idle_stop_timer", value.name());
        editor.apply();
    }
}
