package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

public class OpenPlaylistOptions {
    public boolean openOriginalSourcePlaylist = false;
    
    // Interface
    public boolean displayHeader = true;
    public boolean displayFavoriteIcon = true;
    public boolean displayTrackNumber = true;
    public boolean displayDescriptionDuration = true;
    public boolean displayDescriptionAlbumTitle = false;

    public static @NonNull OpenPlaylistOptions buildDefault()
    {
        return new OpenPlaylistOptions();
    }

    public static @NonNull OpenPlaylistOptions buildFavorites()
    {
        OpenPlaylistOptions options = new OpenPlaylistOptions();
        options.openOriginalSourcePlaylist = true;
        options.displayDescriptionAlbumTitle = true;
        options.displayTrackNumber = false;
        return options;
    }

    public static @NonNull OpenPlaylistOptions buildRecentlyAdded()
    {
        OpenPlaylistOptions options = new OpenPlaylistOptions();
        options.openOriginalSourcePlaylist = true;
        options.displayDescriptionAlbumTitle = true;
        options.displayTrackNumber = false;
        return options;
    }
}
