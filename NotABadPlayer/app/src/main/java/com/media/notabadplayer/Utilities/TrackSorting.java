package com.media.notabadplayer.Utilities;

import com.media.notabadplayer.Audio.MediaTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TrackSorting {
    public static ArrayList<MediaTrack> sortByTrackNumber(ArrayList<MediaTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<MediaTrack>() {
            @Override
            public int compare(MediaTrack o1, MediaTrack o2) {
                return Integer.parseInt(o1.trackNum) - Integer.parseInt(o2.trackNum);
            }
        });
        
        return tracks;
    }
}
