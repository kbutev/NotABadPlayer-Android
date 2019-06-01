package com.media.notabadplayer.Storage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.AudioTrackSource;
import com.media.notabadplayer.Utilities.MediaSorting;

public class AudioStorage implements AudioInfo {
    public static int ALBUM_TRACK_CACHE_CAPACITY = 30;
    
    private final Context _context;
    
    private final ArrayList<AudioAlbum> _albums = new ArrayList<>();
    
    private final SortedMap<String, List<AudioTrack>> _albumTracks = new TreeMap<>();
    
    public AudioStorage(@NonNull Context context)
    {
        _context = context;
    }
    
    public void load()
    {
        synchronized (_albums)
        {
            Log.v(AudioStorage.class.getCanonicalName(), "Loading albums from media store...");

            _albums.clear();

            Cursor cursor = _context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    null, null, null, null);

            if (cursor == null)
            {
                return;
            }

            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int albumArtColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

            while (cursor.moveToNext())
            {
                long albumID = cursor.getLong(albumIdColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String cover = cursor.getString(albumArtColumn);

                AudioAlbum a = new AudioAlbum(String.valueOf(albumID), artist, title, cover != null ? cover : "");
                _albums.add(a);
            }

            cursor.close();

            MediaSorting.sortAlbumsByTitle(_albums);

            Log.v(AudioStorage.class.getCanonicalName(), "Successfully loaded " +  String.valueOf(_albums.size()) + " albums from media store.");
        }
    }
    
    public @NonNull List<AudioAlbum> getAlbums()
    {
        synchronized (_albums)
        {
            if (_albums.size() > 0)
            {
                return Collections.unmodifiableList(_albums);
            }

            load();

            return Collections.unmodifiableList(_albums);
        }
    }
    
    public @Nullable AudioAlbum getAlbumByID(@NonNull String identifier)
    {
        List<AudioAlbum> albums = getAlbums();
        
        for (AudioAlbum album: albums)
        {
            if (album.albumID.equals(identifier))
            {
                return album;
            }
        }
        
        return null;
    }

    synchronized public @NonNull List<AudioTrack> getAlbumTracks(@NonNull AudioAlbum album)
    {
        if (_albumTracks.containsKey(album.albumID))
        {
            return _albumTracks.get(album.albumID);
        }
        
        ArrayList<AudioTrack> albumTracks = new ArrayList<>();
        
        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };
        
        String selection = "is_music != 0";
        
        if (Integer.parseInt(album.albumID) > 0) 
        {
            selection = selection + " and album_id = " + album.albumID;
        }
        
        Cursor cursor = _context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, null);
        
        if (cursor == null)
        {
            return new ArrayList<>();
        }

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        
        while (cursor.moveToNext())
        {
            String filePath = cursor.getString(dataColumn);
            String title = cursor.getString(titleColumn);
            String artist = cursor.getString(artistColumn);
            String albumTitle = cursor.getString(albumTitleColumn);
            int trackNum = cursor.getInt(trackNumColumn);
            double duration = cursor.getLong(durationColumn) / 1000.0;
            
            albumTracks.add(new AudioTrack(filePath, 
                            title,
                            artist,
                            albumTitle,
                            album.albumID,
                            album.albumCover,
                            trackNum, 
                            duration,
                            AudioTrackSource.createAlbumSource(album.albumID)));
        }

        cursor.close();

        storeTracksIntoCache(album.albumID, albumTracks);
        
        return albumTracks;
    }
    
    private void storeTracksIntoCache(@NonNull String albumID, List<AudioTrack> tracks)
    {
        _albumTracks.put(albumID, tracks);
        
        if (_albumTracks.size() > AudioStorage.ALBUM_TRACK_CACHE_CAPACITY)
        {
            String firstKey = _albumTracks.firstKey();
            
            _albumTracks.remove(firstKey);
        }
    }
    
    public @NonNull List<AudioTrack> searchForTracks(@NonNull String query)
    {
        ArrayList<AudioTrack> albumTracks = new ArrayList<>();
        
        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };
        
        String selection = MediaStore.Audio.Media.TITLE + " LIKE ?";
        String[] selectionArgs = new String[] {""};

        String[] words = query.split(" ");
        
        for (int i = 0; i < words.length; i++) 
        {
            selectionArgs[0] += "%" + words[i] + "%";
        }
        
        String orderBy = null;

        Cursor cursor = _context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, orderBy);
        
        if (cursor == null)
        {
            return new ArrayList<>();
        }
        
        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        
        while (cursor.moveToNext())
        {
            String filePath = cursor.getString(dataColumn);
            String title = cursor.getString(titleColumn);
            String artist = cursor.getString(artistColumn);
            String albumTitle = cursor.getString(albumTitleColumn);
            int trackNum = cursor.getInt(trackNumColumn);
            double duration = cursor.getLong(durationColumn) / 1000.0;
            String albumID = cursor.getString(albumIDColumn);
            AudioAlbum album = getAlbumByID(albumID);
            
            if (album == null)
            {
                continue;
            }
            
            String albumCover = album.albumCover;
            
            albumTracks.add(new AudioTrack(filePath,
                            title, 
                            artist, 
                            albumTitle,
                            albumID,
                            albumCover,
                            trackNum,
                            duration,
                            AudioTrackSource.createAlbumSource(album.albumID)));
        }

        cursor.close();
        
        return albumTracks;
    }

    public AudioTrack findTrackByPath(@NonNull Uri path)
    {
        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        Cursor cursor = _context.getContentResolver().query(
                path,
                projection, null, null, null);
        
        if (cursor == null)
        {
            return null;
        }
        
        cursor.moveToNext();

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        
        String filePath = cursor.getString(dataColumn);
        String title = cursor.getString(titleColumn);
        String artist = cursor.getString(artistColumn);
        String albumTitle = cursor.getString(albumTitleColumn);
        int trackNum = cursor.getInt(trackNumColumn);
        double duration = cursor.getLong(durationColumn) / 1000.0;
        String albumId = cursor.getString(albumIDColumn);
        AudioAlbum album = getAlbumByID(albumId);

        if (album == null)
        {
            return null;
        }
        
        String albumCover = album.albumCover;
        
        cursor.close();
        
        return new AudioTrack(filePath,
                              title,
                              artist,
                              albumTitle,
                              albumId,
                              albumCover,
                              trackNum,
                              duration,
                              AudioTrackSource.createAlbumSource(album.albumID));
    }
}
