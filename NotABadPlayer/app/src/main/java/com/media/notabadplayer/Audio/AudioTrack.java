package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AudioTrack implements Serializable {
    public final @NonNull String filePath;
    public final @NonNull String title;
    public final @NonNull String artist;
    public final @NonNull String albumTitle;
    public final @NonNull String albumID;
    public final @NonNull String artCover;
    public final @NonNull String trackNum;
    public final double durationInSeconds;
    public final @NonNull String duration;
    public final @NonNull AudioTrackSource source;
    public byte stars;
    
    public AudioTrack(@NonNull String filePath,
                      @NonNull String title,
                      @NonNull String artist,
                      @NonNull String albumTitle,
                      @NonNull String albumID,
                      @NonNull String artCover,
                      int trackNum, 
                      double durationInSeconds,
                      @NonNull AudioTrackSource source)
    {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.albumTitle = albumTitle;
        this.albumID = albumID;
        this.artCover = artCover;
        this.trackNum = String.valueOf(trackNum);
        this.durationInSeconds = durationInSeconds;
        this.duration = secondsToString(durationInSeconds);
        this.source = source;
    }

    public AudioTrack(@NonNull AudioTrack originalTrack,
                      @NonNull AudioTrackSource source)
    {
        this.filePath = originalTrack.filePath;
        this.title = originalTrack.title;
        this.artist = originalTrack.artist;
        this.albumTitle = originalTrack.albumTitle;
        this.albumID = originalTrack.albumID;
        this.artCover = originalTrack.artCover;
        this.trackNum = String.valueOf(originalTrack.trackNum);
        this.durationInSeconds = originalTrack.durationInSeconds;
        this.duration = secondsToString(originalTrack.durationInSeconds);
        this.source = source;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AudioTrack)
        {
            AudioTrack other = (AudioTrack)o;
            
            return filePath.equals(other.filePath);
        }
        
        return false;
    }
    
    public static String timeDescription(String pDescription, int pTime)
    {
        final String preformatedTime = secondsToString(pTime);
        final String timeForReturn = putTimeInXX(pDescription,preformatedTime);
        return timeForReturn;
    }
    
    public static String secondsToString(double pTime) 
    {
        final int time = (int)pTime;
        final int hr = time/60/60;
        final int min = (time - (hr*60*60)) / 60;
        final int sec = (time - (hr*60*60) - (min*60));
        
        if (hr == 0)
        {
            final String strMin = placeZeroIfNeeded(min);
            final String strSec = placeZeroIfNeeded(sec);

            return String.format("%s:%s", strMin, strSec);
        }
        
        final String strHr = placeZeroIfNeeded(hr);
        final String strMin = placeZeroIfNeeded(min);
        final String strSec = placeZeroIfNeeded(sec);

        return String.format("%s:%s:%s", strHr, strMin, strSec);
    }
    
    public static String placeZeroIfNeeded(int number) 
    {
        return (number >= 10)? Integer.toString(number):String.format("0%s", Integer.toString(number));
    }
    
    public static String putTimeInXX(String pDescription, String pTime)
    {
        String[] apartDescription = pDescription.split("XX");
        
        StringBuilder descriptionForReturn = new StringBuilder();
        
        for (int i = 0; i < apartDescription.length; i++)
        {
            descriptionForReturn.append(apartDescription[i]);
            
            if (i == 0) 
            {
                descriptionForReturn.append(pTime);
            }
        }
        
        return descriptionForReturn.toString();
    }
    
    private void writeObject(@NonNull ObjectOutputStream out) throws IOException 
    {
        out.defaultWriteObject();
    }
    
    private void readObject(@NonNull ObjectInputStream in) throws IOException,ClassNotFoundException 
    {
        in.defaultReadObject();
    }
}
