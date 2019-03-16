package com.media.notabadplayer.View;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums);
    void onAlbumSongsLoad(ArrayList<AudioTrack> songs);
    
    void openPlayerScreen(AudioPlaylist playlist);
    
    void searchQueryResults(String searchQuery, ArrayList<AudioTrack> songs);

    void appSettingsReset();
    void appThemeChanged(AppSettings.AppTheme appTheme);
    void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting);
    void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar);
}
