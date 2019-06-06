package com.media.notabadplayer.View;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;

public interface BaseView {
    void enableInteraction();
    void disableInteraction();

    void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist);
    
    void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums);

    void onPlaylistLoad(@NonNull AudioPlaylist playlist);

    void onUserPlaylistsLoad(@NonNull List<AudioPlaylist> playlists);
    
    void openPlayerScreen(@NonNull AudioPlaylist playlist);
    void updatePlayerScreen(@NonNull AudioPlaylist playlist);
    
    void searchQueryResults(@NonNull String searchQuery, @NonNull List<AudioTrack> songs, @Nullable String searchTip);
    
    void appSettingsReset();
    void appThemeChanged(AppSettings.AppTheme appTheme);
    void appTrackSortingChanged(AppSettings.TrackSorting trackSorting);
    void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar);
    
    void onPlayerErrorEncountered(@NonNull Exception error);
}
