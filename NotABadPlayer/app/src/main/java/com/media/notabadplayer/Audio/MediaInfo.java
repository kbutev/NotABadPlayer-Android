package com.media.notabadplayer.Audio;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;

public class MediaInfo {
    private Context _context;
    private ArrayList<AlbumInfo> _albums = new  ArrayList<AlbumInfo>();
    private HashMap<String, ArrayList<AudioTrack>> _albumSongs = new HashMap<>();
    
    public MediaInfo(Context context)
    {
        _context = context;
    }
    
    public int getAlbumsCount()
    {
        return 1;
    }
    
    public void load()
    {
        if (_albums.size() > 0)
        {
            return;
        }
        
        Cursor cursor = _context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        
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
            
            AlbumInfo a = new AlbumInfo(String.valueOf(albumID), title, cover);
            _albums.add(a);
        }
        
        cursor.close();
    }
    
    public ArrayList<AlbumInfo> getAlbums()
    {
        if (_albums.size() > 0)
        {
            return _albums;
        }
        
        load();
        
        return _albums;
    }
    
    public ArrayList<AudioTrack> getAlbumSongs(AlbumInfo album)
    {
        if (!_albumSongs.containsKey(album.albumID))
        {
            _albumSongs.put(album.albumID, new ArrayList<AudioTrack>());
        }
        else
        {
            return _albumSongs.get(album.albumID);
        }
        
        ArrayList<AudioTrack> albumSongs = _albumSongs.get(album.albumID);
        
        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };
        String selection = "is_music != 0";
        
        if (Integer.parseInt(album.albumID) > 0) 
        {
            selection = selection + " and album_id = " + album.albumID;
        }
        
        String orderBy = null;
        
        Cursor cursor = _context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, orderBy);

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        
        while (cursor.moveToNext())
        {
            String filePath = cursor.getString(dataColumn);
            String title = cursor.getString(titleColumn);
            int trackNum = cursor.getInt(trackNumColumn);
            double duration = cursor.getLong(durationColumn) / 1000;
            
            albumSongs.add(new AudioTrack(filePath, title, trackNum, duration));
        }
        
        return albumSongs;
    }
}
