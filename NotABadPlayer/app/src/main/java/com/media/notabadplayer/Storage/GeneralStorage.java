package com.media.notabadplayer.Storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;

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
    
    synchronized public boolean isFirstApplicationLaunch(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        boolean firstTime = preferences.getBoolean("firstTime", true);
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstTime", false);
        editor.apply();
        
        return firstTime;
    }
    
    synchronized public void saveCurrentAudioState(Context context)
    {
        AudioPlayer player = AudioPlayer.getShared();
        
        if (!player.hasPlaylist())
        {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        AudioPlaylist playlist = player.getPlaylist();
        
        ArrayList<String> tracksArray = playlist.getTracksAsStrings();
        
        editor.putInt("tracks_size", tracksArray.size());
        
        for (int e = 0; e < tracksArray.size(); e++)
        {
            editor.putString("track_" + String.valueOf(e), tracksArray.get(e));
        }
        
        editor.putString("playingTrack", playlist.getPlayingTrackAsString());
        
        editor.putString("playOrder", playlist.getPlayOrder().toString());
        
        editor.putInt("playerPosition", player.getCurrentPositionMSec());
        
        editor.apply();
    }

    synchronized public void restoreAudioState(Application application, Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GeneralStorage.class.getCanonicalName(), Context.MODE_PRIVATE);
        
        ArrayList<String> tracksArray = new ArrayList<>();
        int tracksSize = preferences.getInt("tracks_size", 0);

        for (int e = 0; e < tracksSize; e++)
        {
            String track = preferences.getString("track_" + String.valueOf(e), "");
            
            if (track != null && !track.isEmpty())
            {
                tracksArray.add(track);
            }
        }
        
        if (tracksArray.isEmpty())
        {
            return;
        }
        
        AudioPlayer player = AudioPlayer.getShared();
        
        String track = preferences.getString("playingTrack", "");
        
        ArrayList<AudioTrack> tracks = new ArrayList<>();

        for (int e = 0; e < tracksArray.size(); e++)
        {
            tracks.add(AudioTrack.createFromString(tracksArray.get(e)));
        }
        
        AudioPlayOrder order = AudioPlayOrder.FORWARDS;
        
        try
        {
            String playOrder = preferences.getString("playOrder", "");
            order = AudioPlayOrder.valueOf(playOrder);
        }
        catch (Exception e)
        {
            Log.v(GeneralStorage.class.getCanonicalName(), "Could not retrieve the saved play order from storage");
        }
        
        AudioPlaylist playlist = new AudioPlaylist(tracks, AudioTrack.createFromString(track));

        player.playPlaylist(application, playlist);
        player.getPlaylist().setPlayOrder(order);
        
        int positionMSec = preferences.getInt("playerPosition", 0);
        
        player.seekTo(positionMSec);
        
        // Always pause by default when restoring state from storage
        player.pause();
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
        editor.putString(ApplicationInput.PLAYER_NEXT_BUTTON.name(), ApplicationAction.NEXT.name());
        editor.putString(ApplicationInput.PLAYER_PREVIOUS_BUTTON.name(), ApplicationAction.PREVIOUS.name());
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
}
