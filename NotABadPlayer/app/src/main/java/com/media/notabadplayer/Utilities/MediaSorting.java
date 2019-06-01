package com.media.notabadplayer.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

public class MediaSorting {
    public static ArrayList<AudioTrack> sortTracks(List<AudioTrack> tracks, AppSettings.TrackSorting sorting)
    {
        switch (sorting)
        {
            case TRACK_NUMBER:
                sortTracksByTrackNumber(tracks);
                break;
            case TITLE:
                sortTracksByTitle(tracks);
                break;
            case LONGEST:
                sortTracksByLongest(tracks);
                break;
            case SHORTEST:
                sortTracksByShortest(tracks);
                break;
        }

        return new ArrayList<>(tracks);
    }

    public static void sortAlbums(List<AudioAlbum> albums, AppSettings.AlbumSorting sorting)
    {
        switch (sorting)
        {
            case TITLE:
                sortAlbumsByTitle(albums);
                break;
        }
    }
    
    public static void sortTracksByTrackNumber(List<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return Integer.parseInt(o1.trackNum) - Integer.parseInt(o2.trackNum);
            }
        });
    }

    public static void sortTracksByTitle(List<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return o1.title.compareTo(o2.title);
            }
        });
    }

    public static void sortTracksByLongest(List<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return (int)(o2.durationInSeconds - o1.durationInSeconds);
            }
        });
    }

    public static void sortTracksByShortest(List<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return (int)(o1.durationInSeconds - o2.durationInSeconds);
            }
        });
    }
    
    public static void sortAlbumsByTitle(List<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return o1.albumTitle.compareTo(o2.albumTitle);
            }
        });
    }

    public static void sortAlbumsByPopularity(List<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return 0;
            }
        });
    }
}
