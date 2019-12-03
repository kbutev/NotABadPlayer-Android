package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

import com.media.notabadplayer.Audio.Utilities.HashUtilities;

class AudioTrackDate implements Serializable {
    private final @NonNull Date added;
    private final @NonNull Date modified;
    private final @Nullable Date firstPlayed;
    private final @Nullable Date lastPlayed;

    AudioTrackDate(@NonNull Date added, @NonNull Date modified, @Nullable Date firstPlayed, @Nullable Date lastPlayed)
    {
        this.added = added;
        this.modified = modified;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof AudioTrackDate))
        {
            return false;
        }

        AudioTrackDate otherDate = (AudioTrackDate) other;

        if (!this.added.equals(otherDate.added))
        {
            return false;
        }

        if (!this.modified.equals(otherDate.modified))
        {
            return false;
        }

        if (safeEquals(this.firstPlayed, otherDate.firstPlayed))
        {
            return false;
        }

        if (safeEquals(this.lastPlayed, otherDate.lastPlayed))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return HashUtilities.hashObjects(added, modified, firstPlayed, lastPlayed);
    }

    public @NonNull Date getAdded()
    {
        return added;
    }

    public @NonNull Date getModified()
    {
        return modified;
    }

    public @Nullable Date getFirstPlayed()
    {
        return firstPlayed;
    }

    public @Nullable Date getLastPlayed()
    {
        return lastPlayed;
    }

    private boolean safeEquals(@Nullable Date one, @Nullable Date other)
    {
        if (one == null)
        {
            return other == null;
        }

        if (other == null)
        {
            // one does not equal null, so return false
            return false;
        }

        return one.equals(other);
    }
}
