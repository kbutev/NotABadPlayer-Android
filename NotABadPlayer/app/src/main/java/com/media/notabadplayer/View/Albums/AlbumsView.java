package com.media.notabadplayer.View.Albums;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.MVP.BaseRootView;

import java.util.List;

public interface AlbumsView extends BaseRootView {
    void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums);

    void onDeviceLibraryChanged();

    void onFetchDataErrorEncountered(@NonNull Exception error);
    void onPlayerErrorEncountered(@NonNull Exception error);
}
