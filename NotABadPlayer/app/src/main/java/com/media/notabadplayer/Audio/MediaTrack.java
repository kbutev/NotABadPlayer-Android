package com.media.notabadplayer.Audio;

public class MediaTrack {
    public final String filePath;
    public final String title;
    public final String artist;
    public final String albumTitle;
    public final String artCover;
    public final String trackNum;
    public final double durationInSeconds;
    public final String duration;
    
    public MediaTrack(String filePath, String title, String artist, String albumTitle, String artCover, int trackNum, double durationInSeconds)
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
    
    public static MediaTrack createFromString(String data)
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
        
        return new MediaTrack(values[0], values[1], values[2], values[3], values[4], Integer.parseInt(values[5]), Double.parseDouble(values[6]));
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
        final int min = pTime/60;
        final int sec = pTime-(min*60);

        final String strMin = placeZeroIfNeeded(min);
        final String strSec = placeZeroIfNeeded(sec);
        return String.format("%s:%s",strMin,strSec);
    }
  
    public static String placeZeroIfNeeded(int number) 
    {
        return (number >=10)? Integer.toString(number):String.format("0%s",Integer.toString(number));
    }
  
    public static String putTimeInXX(String pDescription, String pTime)
    {
        String[] apartDescription = pDescription.split("XX");

        StringBuilder descriptionForReturn = new StringBuilder();
        for (int i = 0; i < apartDescription.length; i++) {
            descriptionForReturn.append(apartDescription[i]);
            if (i == 0) {
                descriptionForReturn.append(pTime);
            }
        }
        return descriptionForReturn.toString();
    }
}
