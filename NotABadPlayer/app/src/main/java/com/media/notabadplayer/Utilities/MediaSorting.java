package com.media.notabadplayer.Utilities;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MediaSorting {
    public static void sortTracks(ArrayList<AudioTrack> tracks, AppSettings.TrackSorting sorting)
    {
        switch (sorting)
        {
            case TRACK_NUMBER:
                sortTracksByTrackNumber(tracks);
                break;
            case TITLE:
                sortTracksByTitle(tracks);
                break;
            case POPULARITY:
                sortTracksByPopularity(tracks);
                break;
            case LONGEST:
                sortTracksByLength(tracks, true);
                break;
            case SHORTEST:
                sortTracksByLength(tracks, false);
                break;
        }
    }

    public static void sortAlbums(ArrayList<AudioAlbum> albums, AppSettings.AlbumSorting sorting)
    {
        switch (sorting)
        {
            case TITLE:
                sortAlbumsByTitle(albums);
                break;
            case POPULARITY:
                sortAlbumsByPopularity(albums);
                break;
        }
    }
    
    public static void sortTracksByTrackNumber(ArrayList<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return Integer.parseInt(o1.trackNum) - Integer.parseInt(o2.trackNum);
            }
        });
    }

    public static void sortTracksByTitle(ArrayList<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return o1.title.compareTo(o2.title);
            }
        });
    }

    public static void sortTracksByPopularity(ArrayList<AudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<AudioTrack>() {
            @Override
            public int compare(AudioTrack o1, AudioTrack o2) {
                return 0;
            }
        });
    }

    public static void sortTracksByLength(ArrayList<AudioTrack> tracks, boolean longest)
    {
        if (!longest)
        {
            Collections.sort(tracks, new Comparator<AudioTrack>() {
                @Override
                public int compare(AudioTrack o1, AudioTrack o2) {
                    return (int)(o1.durationInSeconds - o2.durationInSeconds);
                }
            });
        }
        else {
            Collections.sort(tracks, new Comparator<AudioTrack>() {
                @Override
                public int compare(AudioTrack o1, AudioTrack o2) {
                    return (int)(o2.durationInSeconds - o1.durationInSeconds);
                }
            });
        }
    }
    
    public static void sortAlbumsByTitle(ArrayList<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return o1.albumTitle.compareTo(o2.albumTitle);
            }
        });
    }

    public static void sortAlbumsByPopularity(ArrayList<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return 0;
            }
        });
    }
}
