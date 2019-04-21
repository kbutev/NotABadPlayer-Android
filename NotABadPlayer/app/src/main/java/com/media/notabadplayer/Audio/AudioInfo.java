package com.media.notabadplayer.Audio;

import java.util.ArrayList;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface AudioInfo {
    @NonNull ArrayList<AudioAlbum> getAlbums();
    @Nullable AudioAlbum getAlbumByID(String identifier);
    @NonNull ArrayList<AudioTrack> getAlbumTracks(AudioAlbum album);
    @NonNull ArrayList<AudioTrack> searchForTracks(String query);
    @Nullable AudioTrack findTrackByPath(Uri path);
}
