package com.media.notabadplayer.Audio;

import java.util.List;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.SearchFilter;

public interface AudioInfo {
    @NonNull List<AudioAlbum> getAlbums();
    @Nullable AudioAlbum getAlbumByID(@NonNull String identifier);
    @NonNull List<AudioTrack> getAlbumTracks(@NonNull AudioAlbum album);
    @NonNull List<AudioTrack> searchForTracks(@NonNull String query, SearchFilter filter);
    @Nullable AudioTrack findTrackByPath(@NonNull Uri path);
}
