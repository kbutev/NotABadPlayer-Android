package com.media.notabadplayer.View;

import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(@NonNull String albumID, @NonNull String albumArtist, @NonNull String albumTitle, @NonNull String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums);
    void onAlbumSongsLoad(ArrayList<AudioTrack> songs);
    
    void openPlayerScreen(AudioPlaylist playlist);
    
    void searchQueryResults(String searchQuery, ArrayList<AudioTrack> songs);
    
    void appSettingsReset();
    void appThemeChanged(AppSettings.AppTheme appTheme);
    void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting);
    void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar);
}
