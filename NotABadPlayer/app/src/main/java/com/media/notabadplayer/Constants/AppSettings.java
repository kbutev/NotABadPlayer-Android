package com.media.notabadplayer.Constants;

public class AppSettings
{
    public enum AppTheme {
        LIGHT, DARK, MIX
    }
    
    public enum AlbumSorting {
        TITLE
    }
    
    public enum TrackSorting {
        TRACK_NUMBER, TITLE, LONGEST, SHORTEST
    }
    
    public enum ShowVolumeBar {
        NO, LEFT_SIDE, RIGHT_SIDE
    }

    public enum OpenPlayerOnPlay {
        NO, YES, PLAYLIST_ONLY, SEARCH_ONLY;

        public boolean openForPlaylist() {
            return this == YES || this == PLAYLIST_ONLY;
        }

        public boolean openForSearch() {
            return this == YES || this == SEARCH_ONLY;
        }
    }
    
    public enum TabCachingPolicies {
        NO_CACHING, ALBUMS_ONLY, CACHE_ALL;
        
        public boolean cacheAlbumsTab()
        {
            return this == ALBUMS_ONLY || this == CACHE_ALL;
        }

        public boolean cacheListsTab()
        {
            return this == CACHE_ALL;
        }

        public boolean cacheSearchTab()
        {
            return this == CACHE_ALL;
        }

        public boolean cacheSettingsTab()
        {
            return this == CACHE_ALL;
        }
    }
}
