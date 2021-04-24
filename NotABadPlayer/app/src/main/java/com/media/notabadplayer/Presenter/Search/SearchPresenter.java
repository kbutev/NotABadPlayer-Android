package com.media.notabadplayer.Presenter.Search;

import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.View.Search.SearchView;

import java.util.List;

public interface SearchPresenter extends BasePresenter {
    void setView(@NonNull SearchView view);

    @NonNull List<BaseAudioTrack> getSearchResults();
    void onSearchResultClick(int index);
    void onSearchQuery(@NonNull String searchValue, com.media.notabadplayer.Constants.SearchFilter filter);
}
