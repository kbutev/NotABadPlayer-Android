package com.media.notabadplayer.Audio.Model;

public class OpenPlaylistOptions {
    // Interface
    public boolean displayHeader = true;
    public boolean displayFavoriteIcon = true;
    public boolean displayTrackNumber = true;
    public boolean displayDescriptionDuration = true;
    public boolean displayDescriptionAlbumTitle = false;

    public static OpenPlaylistOptions buildDefault()
    {
        return new OpenPlaylistOptions();
    }

    public static OpenPlaylistOptions buildFavorites()
    {
        OpenPlaylistOptions options = new OpenPlaylistOptions();
        options.displayDescriptionAlbumTitle = true;
        return options;
    }
}
