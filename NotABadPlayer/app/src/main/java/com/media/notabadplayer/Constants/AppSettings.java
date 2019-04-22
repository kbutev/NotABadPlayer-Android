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
    
    public enum ShowStars {
        YES, PLAYER_ONLY, TRACK_ONLY, NO
    }
    
    public enum ShowVolumeBar {
        NO, LEFT_SIDE, RIGHT_SIDE
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
