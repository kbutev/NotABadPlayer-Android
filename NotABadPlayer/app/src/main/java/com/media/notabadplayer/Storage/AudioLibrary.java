package com.media.notabadplayer.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioTrackBuilder;
import com.media.notabadplayer.Audio.Model.AudioTrackSource;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.BaseAudioTrackBuilderNode;
import com.media.notabadplayer.Constants.SearchFilter;
import com.media.notabadplayer.PlayerApplication;
import com.media.notabadplayer.Utilities.MediaSorting;

// Provides simple interface to the audio library of the user.
// Before using the audio library, you MUST call initialize().
// Dependant on storage access permission:
// Make sure you have access to user storage before using the audio library.
public class AudioLibrary extends ContentObserver implements AudioInfo {
    public static int ALBUM_TRACK_CACHE_CAPACITY = 30;
    public static int RECENTLY_ADDED_CAP = 100;
    public static int RECENTLY_ADDED_PREDICATE_DAYS_DIFFERENCE = 30;

    private static AudioLibrary singleton;

    private final Object _lock = new Object();

    private Application _context;

    // List of all albums
    private boolean _albumsLoaded;
    private final ArrayList<AudioAlbum> _albums = new ArrayList<>();

    // Album id : list of audio tracks
    private final SortedMap<String, List<BaseAudioTrack>> _albumTracks = new TreeMap<>();

    // Alerted when the device library is changed
    private final HashSet<ChangesListener> _changesListeners = new HashSet<>();

    // List of recently added tracks
    private boolean _recentlyAddedLoaded;
    private final ArrayList<BaseAudioTrack> _recentlyAdded = new ArrayList<>();

    public interface ChangesListener {
        void onMediaLibraryChanged();
    }

    private AudioLibrary()
    {
        // The handler decides which thread the callbacks will be performed on
        super(new Handler(Looper.getMainLooper()));

        Log.v(AudioLibrary.class.getCanonicalName(), "Initializing...");

        _albumsLoaded = false;
        _recentlyAddedLoaded = false;

        _context = PlayerApplication.getShared();

        Log.v(AudioLibrary.class.getCanonicalName(), "Initialized!");
    }

    public synchronized static AudioLibrary getShared()
    {
        if (singleton == null)
        {
            singleton = new AudioLibrary();
        }

        return singleton;
    }

    private @NonNull Application getContext()
    {
        if (_context == null)
        {
            throw new UncheckedExecutionException(new Exception("AudioLibrary cannot be used before being initialized, initialize() has never been called"));
        }

        return _context;
    }

    // # Init

    @Override
    public void loadIfNecessary()
    {
        synchronized (_lock)
        {
            if (_albumsLoaded)
            {
                return;
            }
        }

        load();
    }

    @Override
    public void load()
    {
        // Load whatever is needed to operate the library
        // Albums will be loaded and stored, since they hold huge amount of information

        synchronized (_lock)
        {
            Context context = getContext();

            Log.v(AudioLibrary.class.getCanonicalName(), "Loading albums from media store...");

            _albums.clear();

            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
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

            Log.v(AudioLibrary.class.getCanonicalName(), "Successfully loaded " +  String.valueOf(_albums.size()) + " albums from media store.");

            _albumsLoaded = true;
        }
    }

    // # Album info

    @Override
    public @NonNull List<AudioAlbum> getAlbums()
    {
        loadIfNecessary();

        synchronized (_lock)
        {
            return Collections.unmodifiableList(_albums);
        }
    }

    @Override
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

    @Override
    public @NonNull List<BaseAudioTrack> getAlbumTracks(@NonNull AudioAlbum album)
    {
        synchronized (_lock)
        {
            if (_albumTracks.containsKey(album.albumID))
            {
                return _albumTracks.get(album.albumID);
            }
        }

        Context context = getContext();

        ArrayList<BaseAudioTrack> albumTracks = new ArrayList<>();

        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
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

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, null);

        if (cursor == null)
        {
            return new ArrayList<>();
        }

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
        int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        while (cursor.moveToNext())
        {
            String filePath = cursor.getString(dataColumn);
            Date dateAdded = buildDateFromDatabaseLong(cursor.getLong(dateAddedColumn));
            Date dateModified = buildDateFromDatabaseLong(cursor.getLong(dateModifiedColumn));
            String title = cursor.getString(titleColumn);
            String artist = cursor.getString(artistColumn);
            String albumTitle = cursor.getString(albumTitleColumn);
            int trackNum = cursor.getInt(trackNumColumn);
            double duration = cursor.getLong(durationColumn) / 1000.0;

            BaseAudioTrackBuilderNode node = AudioTrackBuilder.start();
            node.setFilePath(filePath);
            node.setTitle(title);
            node.setArtist(artist);
            node.setAlbumTitle(albumTitle);
            node.setAlbumID(album.albumID);
            node.setArtCover(album.albumCover);
            node.setTrackNum(trackNum);
            node.setDuration(duration);
            node.setSource(AudioTrackSource.createAlbumSource(album.albumID));

            node.setDateAdded(dateAdded);
            node.setDateModified(dateModified);

            try {
                BaseAudioTrack result = node.build();
                albumTracks.add(result);
            } catch (Exception e) {

            }
        }

        cursor.close();

        storeTracksIntoCache(album.albumID, albumTracks);

        return albumTracks;
    }

    private void storeTracksIntoCache(@NonNull String albumID, @NonNull List<BaseAudioTrack> tracks)
    {
        synchronized (_lock)
        {
            _albumTracks.put(albumID, tracks);

            if (_albumTracks.size() > AudioLibrary.ALBUM_TRACK_CACHE_CAPACITY)
            {
                String firstKey = _albumTracks.firstKey();

                _albumTracks.remove(firstKey);
            }
        }
    }

    // # Search

    @Override
    public @NonNull List<BaseAudioTrack> searchForTracks(@NonNull String query, @NonNull SearchFilter filter)
    {
        if (query.isEmpty())
        {
            return new ArrayList<>();
        }

        Context context = getContext();

        ArrayList<BaseAudioTrack> albumTracks = new ArrayList<>();

        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        String selectionFilter;

        switch (filter)
        {
            case Title:
                selectionFilter = MediaStore.Audio.Media.TITLE;
                break;
            case Album:
                selectionFilter = MediaStore.Audio.Media.ALBUM;
                break;
            case Artist:
                selectionFilter = MediaStore.Audio.Media.ARTIST;
                break;
            default:
                selectionFilter = MediaStore.Audio.Media.TITLE;
                break;
        }

        String selection = selectionFilter + " LIKE ?";
        String[] selectionArgs = new String[] {""};

        String[] words = query.split(" ");

        for (int i = 0; i < words.length; i++)
        {
            selectionArgs[0] += "%" + words[i] + "%";
        }

        String orderBy = null;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, orderBy);

        if (cursor == null)
        {
            return new ArrayList<>();
        }

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
        int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        while (cursor.moveToNext())
        {
            String filePath = cursor.getString(dataColumn);
            Date dateAdded = buildDateFromDatabaseLong(cursor.getLong(dateAddedColumn));
            Date dateModified = buildDateFromDatabaseLong(cursor.getLong(dateModifiedColumn));
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

            BaseAudioTrackBuilderNode node = AudioTrackBuilder.start();
            node.setFilePath(filePath);
            node.setTitle(title);
            node.setArtist(artist);
            node.setAlbumTitle(albumTitle);
            node.setAlbumID(album.albumID);
            node.setArtCover(albumCover);
            node.setTrackNum(trackNum);
            node.setDuration(duration);
            node.setSource(AudioTrackSource.createAlbumSource(album.albumID));

            node.setDateAdded(dateAdded);
            node.setDateModified(dateModified);

            try {
                BaseAudioTrack result = node.build();
                albumTracks.add(result);
            } catch (Exception e) {

            }
        }

        cursor.close();

        return albumTracks;
    }

    @Override
    public @Nullable BaseAudioTrack findTrackByPath(@NonNull Uri path)
    {
        Context context = getContext();

        String projection[] = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        Cursor cursor = context.getContentResolver().query(
                path,
                projection, null, null, null);

        if (cursor == null)
        {
            return null;
        }

        cursor.moveToNext();

        int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
        int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        String filePath = cursor.getString(dataColumn);
        Date dateAdded = buildDateFromDatabaseLong(cursor.getLong(dateAddedColumn));
        Date dateModified = buildDateFromDatabaseLong(cursor.getLong(dateModifiedColumn));
        String title = cursor.getString(titleColumn);
        String artist = cursor.getString(artistColumn);
        String albumTitle = cursor.getString(albumTitleColumn);
        int trackNum = cursor.getInt(trackNumColumn);
        double duration = cursor.getLong(durationColumn) / 1000.0;
        String albumId = cursor.getString(albumIDColumn);
        AudioAlbum album = getAlbumByID(albumId);

        AudioTrackSource source = album != null ? AudioTrackSource.createAlbumSource(album.albumID) : AudioTrackSource.createPlaylistSource(title);

        String albumCover = album != null ? album.albumCover : "";

        cursor.close();

        BaseAudioTrackBuilderNode node = AudioTrackBuilder.start();
        node.setFilePath(filePath);
        node.setTitle(title);
        node.setArtist(artist);
        node.setAlbumTitle(albumTitle);
        node.setAlbumID(albumCover);
        node.setArtCover(albumCover);
        node.setTrackNum(trackNum);
        node.setDuration(duration);
        node.setSource(source);

        node.setDateAdded(dateAdded);
        node.setDateModified(dateModified);

        try {
            BaseAudioTrack result = node.build();
            return result;
        } catch (Exception e) {

        }

        return null;
    }

    // # Recently added

    public @NonNull List<BaseAudioTrack> getRecentlyAddedTracks()
    {
        boolean recentlyAddedLoaded;

        synchronized (_lock)
        {
            recentlyAddedLoaded = _recentlyAddedLoaded;
        }

        if (!recentlyAddedLoaded)
        {
            loadRecentlyAddedTracks();
        }

        synchronized (_lock)
        {
            return _recentlyAdded;
        }
    }

    private void loadRecentlyAddedTracks()
    {
        loadIfNecessary();

        synchronized (_lock)
        {
            _recentlyAddedLoaded = true;

            _recentlyAdded.clear();

            Context context = getContext();

            String projection[] = {
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATE_MODIFIED,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.TRACK
            };

            long daysDifference = RECENTLY_ADDED_PREDICATE_DAYS_DIFFERENCE;
            long daysAgoInMS = daysDifference * 24 * 60 * 60 * 1000;
            Date now = new Date();
            Date minimumValueDate = new Date(now.getTime() - daysAgoInMS);
            String dateComparison = String.valueOf(buildDatabaseLongDateFromDate(minimumValueDate));

            String selection = "is_music != 0";
            selection = selection + " and " + MediaStore.Audio.Media.DATE_ADDED + " >= " + dateComparison;

            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);

            if (cursor == null)
            {
                return;
            }

            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext())
            {
                String filePath = cursor.getString(dataColumn);
                Date dateAdded = buildDateFromDatabaseLong(cursor.getLong(dateAddedColumn));
                Date dateModified = buildDateFromDatabaseLong(cursor.getLong(dateModifiedColumn));
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String albumTitle = cursor.getString(albumTitleColumn);
                String albumID = cursor.getString(albumIDColumn);
                int trackNum = cursor.getInt(trackNumColumn);
                double duration = cursor.getLong(durationColumn) / 1000.0;

                AudioAlbum album = getAlbumByID(albumID);
                String albumCover = album != null ? album.albumCover : "";

                BaseAudioTrackBuilderNode node = AudioTrackBuilder.start();
                node.setFilePath(filePath);
                node.setTitle(title);
                node.setArtist(artist);
                node.setAlbumTitle(albumTitle);
                node.setAlbumID(albumID);
                node.setArtCover(albumCover);
                node.setTrackNum(trackNum);
                node.setDuration(duration);
                node.setSource(AudioTrackSource.createAlbumSource(albumID));

                node.setDateAdded(dateAdded);
                node.setDateModified(dateModified);

                try {
                    BaseAudioTrack result = node.build();
                    _recentlyAdded.add(result);
                } catch (Exception e) {

                }

                // Check for cap
                if (_recentlyAdded.size() >= RECENTLY_ADDED_CAP)
                {
                    break;
                }
            }

            cursor.close();
        }
    }

    // # Library changes

    public void registerLibraryChangesListener(@NonNull ChangesListener listener)
    {
        // Register once, the singleton itself
        if (_changesListeners.size() == 0)
        {
            getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, true, this);
        }

        _changesListeners.add(listener);
    }

    public void unregisterLibraryChangesListener(@NonNull ChangesListener listener)
    {
        _changesListeners.remove(listener);

        if (_changesListeners.size() == 0)
        {
            getContext().getContentResolver().unregisterContentObserver(this);
        }
    }

    // # Library changes

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Log.v(AudioLibrary.class.getCanonicalName(), "Device media library was changed! Reloading album data...");

        // Reload library
        load();
        loadRecentlyAddedTracks();

        // Alert listeners
        for (ChangesListener listener : _changesListeners)
        {
            listener.onMediaLibraryChanged();
        }
    }

    // # Date value parsing

    public @NonNull Date buildDateFromDatabaseLong(long value)
    {
        return new Date(value * 1000L);
    }

    public long buildDatabaseLongDateFromDate(@NonNull Date date)
    {
        return (date.getTime() / 1000L);
    }
}
