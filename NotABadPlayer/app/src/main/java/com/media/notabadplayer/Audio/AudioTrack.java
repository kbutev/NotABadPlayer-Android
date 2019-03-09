package com.media.notabadplayer.Audio;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AudioTrack implements Serializable {
    public final String filePath;
    public final String title;
    public final String artist;
    public final String albumTitle;
    public final String artCover;
    public final String trackNum;
    public final double durationInSeconds;
    public final String duration;
    
    public AudioTrack(String filePath, String title, String artist, String albumTitle, String artCover, int trackNum, double durationInSeconds)
    {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.albumTitle = albumTitle;
        this.artCover = artCover;
        this.trackNum = String.valueOf(trackNum);
        this.durationInSeconds = durationInSeconds;
        this.duration = secondsToString(durationInSeconds);
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
