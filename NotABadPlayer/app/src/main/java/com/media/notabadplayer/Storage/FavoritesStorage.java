package com.media.notabadplayer.Storage;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Utilities.Serializing;

import javax.annotation.Nullable;

public class FavoritesStorage {
    public static int CAPACITY = 1000;
    public static String STORAGE_KEY = "FavoritesStorage.data.key";

    private @NonNull final Object _lock = new Object();
    
    private final @NonNull SharedPreferences _preferences;
    
    private boolean _favoritesLoaded = false;
    private ArrayList<FavoriteStorageItem> _items = new ArrayList<>();
    
    private @NonNull Date _lastTimeUpdate = new Date();
    
    FavoritesStorage(@NonNull SharedPreferences preferences) {
        this._preferences = preferences;
    }
    
    public @NonNull List<FavoriteStorageItem> getItems() {
        synchronized (_lock)
        {
            return new ArrayList<>(_items);
        }
    }
    
    public @NonNull Date getLastTimeUpdate() {
        updateLocalStorageIfNecessary();
        
        synchronized (_lock) {
            return _lastTimeUpdate;
        }
    }

    public boolean isMarkedFavorite(@NonNull BaseAudioTrack track) {
        return getMarkedFavoriteItem(track) !=  null;
    }

    public @Nullable FavoriteStorageItem getMarkedFavoriteItem(@NonNull BaseAudioTrack track) {
        updateLocalStorageIfNecessary();
        
        FavoriteStorageItem item = new FavoriteStorageItem(track);
        
        synchronized (this) {
            for (FavoriteStorageItem element : _items) {
                if (element.equals(item)) {
                    return element;
                }
            }
        }
        
        return null;
    }

    public @NonNull FavoriteStorageItem markFavoriteForced(@NonNull BaseAudioTrack track) {
        try {
            return markFavorite(track, true);
        } catch (Exception e) {
            // This should not happen
            return null;
        }
    }

    public @NonNull FavoriteStorageItem markFavorite(@NonNull BaseAudioTrack track) throws Exception {
        return markFavorite(track, false);
    }
    
    public @NonNull FavoriteStorageItem markFavorite(@NonNull BaseAudioTrack track, boolean forced) throws Exception {
        updateLocalStorageIfNecessary();
        
        FavoriteStorageItem already = getMarkedFavoriteItem(track);
        
        if (already != null) {
            return already;
        }

        FavoriteStorageItem item = new FavoriteStorageItem(track);
        
        synchronized (_lock) {
            // Make sure the capacity is not exceeded
            if (_items.size() > CAPACITY) {
                if (!forced) {
                    throw new RuntimeException("Capacity exceeded");
                }
                
                _items.remove(0);
            }

            _items.add(item);
            
            _lastTimeUpdate = new Date();
        }
        
        saveLocalStorage();
        
        return item;
    }
    
    public void unmarkFavorite(@NonNull BaseAudioTrack track) {
        updateLocalStorageIfNecessary();
        
        FavoriteStorageItem item = getMarkedFavoriteItem(track);
        
        if (item == null) {
            return;
        }
        
        synchronized (_lock) {
            _items.remove(item);
            
            _lastTimeUpdate = new Date();
        }
        
        saveLocalStorage();
    }
    
    private void updateLocalStorageIfNecessary() {
        synchronized (_lock) {
            if (_favoritesLoaded) {
                return;
            }
        }
        
        updateLocalStorage();
    }
    
    private void updateLocalStorage() {
        synchronized (_lock) {
            _favoritesLoaded = true;

            String data = _preferences.getString(STORAGE_KEY, "");
            
            if (data == null || data.isEmpty()) {
                Log.v(FavoritesStorage.class.getCanonicalName(), "Failed to unarchive favorite items from storage");
                return;
            }

            try {
                _items = deserializeTracks(data);
            } catch (Exception e) {
                Log.v(FavoritesStorage.class.getCanonicalName(), "Failed to unarchive favorite items from storage");
                return;
            }
            
            Log.v(FavoritesStorage.class.getCanonicalName(), "Retrieved " + String.valueOf(_items.size()) + " favorite items from storage");
        }
    }
    
    private void saveLocalStorage() {
        updateLocalStorageIfNecessary();
        
        synchronized (_lock) {
            String data = Serializing.serializeObject(_items);
            
            if (data == null) {
                Log.v(FavoritesStorage.class.getCanonicalName(), "Failed to archive favorite items to storage");
                return;
            }

            SharedPreferences.Editor editor = _preferences.edit();
            editor.putString(STORAGE_KEY, data);
            editor.apply();
        }
    }
    
    static public @NonNull ArrayList<FavoriteStorageItem> deserializeTracks(@NonNull String data) throws Exception {
        Object result = Serializing.deserializeObject(data);

        if (result instanceof ArrayList)
        {
            ArrayList array = (ArrayList)result;

            if (array.size() > 0)
            {
                if (array.get(0) instanceof FavoriteStorageItem)
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<FavoriteStorageItem> items = (ArrayList<FavoriteStorageItem>)result;
                    return items;
                }

                throw new ClassNotFoundException("Cannot deserialize playlist, unrecognized class type");
            }
        }
        
        return new ArrayList<>();
    }
}
