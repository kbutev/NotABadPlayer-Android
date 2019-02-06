package com.media.notabadplayer.Storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioTrack;

import java.util.ArrayList;

public class GeneralStorage
{
    private static GeneralStorage singleton;
    
    private GeneralStorage()
    {
        
    }
    
    public static synchronized GeneralStorage getShared()
    {
        if (singleton == null)
        {
            singleton = new GeneralStorage();
        }
        
        return singleton;
    }
    
    public void saveCurrentAudioState(Context context)
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
        
        editor.apply();
    }
    
    public void restoreAudioState(Application application, Context context)
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
        
        AudioPlayer.getShared().playPlaylist(application, playlist);
        AudioPlayer.getShared().getPlaylist().setPlayOrder(order);
        AudioPlayer.getShared().pause();
    }
}
