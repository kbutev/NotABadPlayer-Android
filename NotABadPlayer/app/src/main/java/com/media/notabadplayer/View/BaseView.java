package com.media.notabadplayer.View;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Constants.AppSettings;

public interface BaseView {
    void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options);
    
    void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums);

    void onPlaylistLoad(@NonNull BaseAudioPlaylist playlist);

    void onUserPlaylistsLoad(@NonNull List<BaseAudioPlaylist> playlists);
    
    void openPlayerScreen(@NonNull BaseAudioPlaylist playlist);
    void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist);
    
    void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState);

    void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage);
    void onResetAppSettings();
    void onAppThemeChanged(AppSettings.AppTheme appTheme);
    void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting);
    void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar);
    void onDeviceLibraryChanged();
    
    void onFetchDataErrorEncountered(@NonNull Exception error);
    void onPlayerErrorEncountered(@NonNull Exception error);
}
