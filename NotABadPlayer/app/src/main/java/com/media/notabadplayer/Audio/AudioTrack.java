package com.media.notabadplayer.Audio;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioTrack {
    public final String filePath;
    public final String title;
    public final String trackNum;
    public final String duration;
    
    public AudioTrack(String filePath, String title, int trackNum, double durationInSeconds)
    {
        this.filePath = filePath;
        this.title = title;
        this.trackNum = String.valueOf(trackNum);
        this.duration = secondsToString((int)durationInSeconds);
    }

    public AudioTrack(String filePath, String title, String trackNum, String durationInSeconds)
    {
        this.filePath = filePath;
        this.title = title;
        this.trackNum = trackNum;
        this.duration = durationInSeconds;
    }

    private String timeDescription(String pDescription, int pTime)
    {
        final String preformatedTime = secondsToString(pTime);
        final String timeForReturn = putTimeInXX(pDescription,preformatedTime);
        return timeForReturn;
    }

    private String secondsToString(int pTime) 
    {
        final int min = pTime/60;
        final int sec = pTime-(min*60);

        final String strMin = placeZeroIfNeede(min);
        final String strSec = placeZeroIfNeede(sec);
        return String.format("%s:%s",strMin,strSec);
    }

    private String placeZeroIfNeede(int number) 
    {
        return (number >=10)? Integer.toString(number):String.format("0%s",Integer.toString(number));
    }

    private String putTimeInXX(String pDescription, String pTime)
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
