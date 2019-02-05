package com.media.notabadplayer.Utilities;

import com.media.notabadplayer.Audio.MediaAlbum;
import com.media.notabadplayer.Audio.MediaTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MediaSorting {
    public static ArrayList<MediaTrack> sortTracksByTrackNumber(ArrayList<MediaTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<MediaTrack>() {
            @Override
            public int compare(MediaTrack o1, MediaTrack o2) {
                return Integer.parseInt(o1.trackNum) - Integer.parseInt(o2.trackNum);
            }
        });
        
        return tracks;
    }

    public static ArrayList<MediaAlbum> sortAlbumsByTitle(ArrayList<MediaAlbum> albums)
    {
        Collections.sort(albums, new Comparator<MediaAlbum>() {
            @Override
            public int compare(MediaAlbum o1, MediaAlbum o2) {
                return o1.albumTitle.compareTo(o2.albumTitle);
            }
        });

        return albums;
    }
}
