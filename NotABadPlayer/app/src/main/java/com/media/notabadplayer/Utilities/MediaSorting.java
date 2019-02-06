package com.media.notabadplayer.Utilities;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MediaSorting {
    public static ArrayList<AudioTrack> sortTracksByTrackNumber(ArrayList<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return Integer.parseInt(o1.trackNum) - Integer.parseInt(o2.trackNum);
            }
        });
        
        return tracks;
    }

    public static ArrayList<AudioAlbum> sortAlbumsByTitle(ArrayList<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return o1.albumTitle.compareTo(o2.albumTitle);
            }
        });

        return albums;
    }
}
