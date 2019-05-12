package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

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
    
    public static String secondsToString(double durationInSeconds) 
    {
        final int time = (int)durationInSeconds;
        final int hr = time/60/60;
        final int min = (time - (hr*60*60)) / 60;
        final int sec = (time - (hr*60*60) - (min*60));
        
        if (hr == 0)
        {
            if (min < 10)
            {
                final String strMin = Integer.toString(min);
                final String strSec = parseToStringWithLeadingZero(sec);
                
                return String.format("%s:%s", strMin, strSec);
            }
            
            final String strMin = parseToStringWithLeadingZero(min);
            final String strSec = parseToStringWithLeadingZero(sec);

            return String.format("%s:%s", strMin, strSec);
        }
        
        final String strHr = parseToStringWithLeadingZero(hr);
        final String strMin = parseToStringWithLeadingZero(min);
        final String strSec = parseToStringWithLeadingZero(sec);

        return String.format("%s:%s:%s", strHr, strMin, strSec);
    }
    
    public static String parseToStringWithLeadingZero(int number) 
    {
        return String.format(Locale.getDefault(), "%02d", number);
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
