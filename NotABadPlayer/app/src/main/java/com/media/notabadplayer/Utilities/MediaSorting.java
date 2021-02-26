package com.media.notabadplayer.Utilities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

public class MediaSorting {
    public static ArrayList<BaseAudioTrack> sortTracks(@NonNull List<BaseAudioTrack> tracks, AppSettings.TrackSorting sorting)
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

    public static void sortAlbums(@NonNull List<AudioAlbum> albums, AppSettings.AlbumSorting sorting)
    {
        switch (sorting)
        {
            case TITLE:
                sortAlbumsByTitle(albums);
                break;
        }
    }
    
    public static void sortTracksByTrackNumber(@NonNull List<BaseAudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<BaseAudioTrack>() {
            @Override
            public int compare(BaseAudioTrack o1, BaseAudioTrack o2) {
                return o1.getTrackNum() - o2.getTrackNum();
            }
        });
    }

    public static void sortTracksByTitle(@NonNull List<BaseAudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<BaseAudioTrack>() {
            @Override
            public int compare(BaseAudioTrack o1, BaseAudioTrack o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
    }

    public static void sortTracksByLongest(@NonNull List<BaseAudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<BaseAudioTrack>() {
            @Override
            public int compare(BaseAudioTrack o1, BaseAudioTrack o2) {
                return (int)(o2.getDurationInSeconds() - o1.getDurationInSeconds());
            }
        });
    }

    public static void sortTracksByShortest(@NonNull List<BaseAudioTrack> tracks)
    {
        Collections.sort(tracks, new Comparator<BaseAudioTrack>() {
            @Override
            public int compare(BaseAudioTrack o1, BaseAudioTrack o2) {
                return (int)(o1.getDurationInSeconds() - o2.getDurationInSeconds());
            }
        });
    }
    
    public static void sortAlbumsByTitle(@NonNull List<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return o1.albumTitle.compareTo(o2.albumTitle);
            }
        });
    }

    public static void sortAlbumsByPopularity(@NonNull List<AudioAlbum> albums)
    {
        Collections.sort(albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum o1, AudioAlbum o2) {
                return 0;
            }
        });
    }
}
