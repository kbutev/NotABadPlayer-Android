package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.HashSet;

public class AudioTrackDateBuilder {
    private static AudioTrackDateBuilderCache _cache = new AudioTrackDateBuilderCache();

    public static @NonNull AudioTrackDate buildDefault()
    {
        return getCache().getDefault();
    }

    public static @NonNull AudioTrackDate build(@NonNull Date added, @NonNull Date modified, @Nullable Date firstPlayed, @Nullable Date lastPlayed)
    {
        return getCache().getFlyweight(added, modified, firstPlayed, lastPlayed);
    }

    private static synchronized @NonNull AudioTrackDateBuilderCache getCache()
    {
        if (_cache == null)
        {
            _cache = new AudioTrackDateBuilderCache();
        }

        return _cache;
    }
}

class AudioTrackDateBuilderCache {
    private Date _cacheInitDate = new Date();

    private AudioTrackDate _default = new AudioTrackDate(_cacheInitDate, _cacheInitDate, null, null);

    private HashSet<AudioTrackDate> _audioDatesCache = new HashSet<>();
    private HashSet<Date> _datesCache = new HashSet<>();

    AudioTrackDateBuilderCache()
    {
        _datesCache.add(_cacheInitDate);
    }

    @NonNull AudioTrackDate getDefault()
    {
        return _default;
    }

    @NonNull AudioTrackDate getFlyweight(@NonNull Date added, @NonNull Date modified, @Nullable Date firstPlayed, @Nullable Date lastPlayed)
    {
        Date addedFlyweight = getDateFlyweight(added);
        Date modifiedFlyweight = getDateFlyweight(modified);
        Date firstPlayedFlyweight = getDateFlyweight(firstPlayed);
        Date lastPlayedFlyweight = getDateFlyweight(lastPlayed);

        // Return from cache, if already cached
        AudioTrackDate audioDate = new AudioTrackDate(addedFlyweight, modifiedFlyweight, firstPlayedFlyweight, lastPlayedFlyweight);

        for (AudioTrackDate date : _audioDatesCache)
        {
            if (date.equals(audioDate))
            {
                return date;
            }
        }

        // Otherwise add the given value and return it
        _audioDatesCache.add(audioDate);

        return audioDate;
    }

    private Date getDateFlyweight(@Nullable Date value)
    {
        if (value == null)
        {
            return null;
        }

        // Return from cache, if already cached
        for (Date date : _datesCache)
        {
            // Compare only the time because thats what we care for
            // Calling equals() might be slower
            if (date.getTime() == value.getTime())
            {
                return date;
            }
        }

        // Otherwise add the given value and return it
        _datesCache.add(value);

        return value;
    }
}


