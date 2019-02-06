package com.media.notabadplayer.Audio;

public class AudioTrack {
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
        this.duration = secondsToString((int)durationInSeconds);
    }
    
    public static AudioTrack createFromString(String data)
    {
        if (data == null)
        {
            return null;
        }
        
        String values[] = data.split("\n");
        
        if (values.length != 7)
        {
            return null;
        }
        
        return new AudioTrack(values[0], values[1], values[2], values[3], values[4], Integer.parseInt(values[5]), Double.parseDouble(values[6]));
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
    
    @Override
    public String toString()
    {
        return filePath + "\n" + title + "\n" + artist + "\n" + albumTitle + "\n" + artCover + "\n" + trackNum + "\n" + String.valueOf(durationInSeconds);
    }
    
    public static String timeDescription(String pDescription, int pTime)
    {
        final String preformatedTime = secondsToString(pTime);
        final String timeForReturn = putTimeInXX(pDescription,preformatedTime);
        return timeForReturn;
    }
    
    public static String secondsToString(int pTime) 
    {
        final int hr = pTime/60/60;
        final int min = (pTime - (hr*60*60)) / 60;
        final int sec = (pTime - (hr*60*60) - (min*60));
        
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
}