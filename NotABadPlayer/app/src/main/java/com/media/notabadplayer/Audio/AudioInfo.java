package com.media.notabadplayer.Audio;

import java.util.List;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface AudioInfo {
    @NonNull List<AudioAlbum> getAlbums();
    @Nullable AudioAlbum getAlbumByID(String identifier);
    @NonNull List<AudioTrack> getAlbumTracks(AudioAlbum album);
    @NonNull List<AudioTrack> searchForTracks(String query);
    @Nullable AudioTrack findTrackByPath(Uri path);
}
