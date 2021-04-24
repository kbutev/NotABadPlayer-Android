package com.media.notabadplayer.View.Search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.MVP.BaseRootView;

import java.util.List;

public interface SearchView extends BaseRootView {
    void openPlayerScreen(@NonNull BaseAudioPlaylist playlist);
    void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist);

    void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState);

    void onPlayerErrorEncountered(@NonNull Exception error);
}
