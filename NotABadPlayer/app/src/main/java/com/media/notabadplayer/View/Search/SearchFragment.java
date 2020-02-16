package com.media.notabadplayer.View.Search;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Constants.SearchFilter;
import com.media.notabadplayer.Presenter.PlaylistPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Other.TrackListFavoritesChecker;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Playlist.PlaylistFragment;

public class SearchFragment extends Fragment implements BaseView, AudioPlayerObserver, TrackListFavoritesChecker
{
    Player _player = Player.getShared();
    
    private BasePresenter _presenter;
    
    private EditText _searchField;
    private ImageButton _searchFieldClearButton;
    private TextView _searchState;
    private ListView _searchResultsList;
    private ProgressBar _progressIndicator;
    private RadioButton _searchByTrack;
    private RadioButton _searchByAlbum;
    private RadioButton _searchByArtist;
    
    private @Nullable SearchListAdapter _searchResultsAdapter;
    private @Nullable Parcelable _searchResultsState;

    private SearchFilter _searchFilter;
    
    public SearchFragment()
    {
        _searchFilter = SearchFilter.Title;
    }
    
    public static @NonNull SearchFragment newInstance(@NonNull BasePresenter presenter)
    {
        SearchFragment fragment = new SearchFragment();
        fragment._presenter = presenter;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        
        // Setup UI
        _searchField = root.findViewById(R.id.searchField);
        _searchFieldClearButton = root.findViewById(R.id.searchFieldClearButton);
        _searchState = root.findViewById(R.id.searchState);
        _searchResultsList = root.findViewById(R.id.searchResultsList);
        _progressIndicator = root.findViewById(R.id.progressIndicator);
        _searchByTrack = root.findViewById(R.id.searchByTrack);
        _searchByAlbum = root.findViewById(R.id.searchByAlbum);
        _searchByArtist = root.findViewById(R.id.searchByArtist);
        
        initUI();
        
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        _presenter.start();
        
        _player.observers.attach(this);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        hideProgressIndicator();

        if (_searchResultsAdapter != null)
        {
            _searchResultsList.setAdapter(_searchResultsAdapter);
        }
        
        if (_searchResultsState != null)
        {
            _searchResultsList.onRestoreInstanceState(_searchResultsState);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        enableInteraction();
    }

    @Override
    public void onPause()
    {
        _searchResultsState = _searchResultsList.onSaveInstanceState();
        
        super.onPause();
        
        disableInteraction();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        _player.observers.detach(this);
    }
    
    private void initUI()
    {
        _searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((actionId & EditorInfo.IME_MASK_ACTION) != 0)
                {
                    performSearch();
                }
                
                return false;
            }
        });
        
        _searchFieldClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_searchFieldClearButton.isClickable())
                {
                    return;
                }

                _searchField.setText("");
            }
        });
        
        _searchResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!_searchResultsList.isClickable() || _searchResultsAdapter == null)
                {
                    return;
                }

                _searchResultsAdapter.selectItem(view, position);

                _presenter.onSearchResultClick(position);
            }
        });

        _searchByTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    _searchFilter = SearchFilter.Title;
                    performSearch();
                }
            }
        });

        _searchByAlbum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    _searchFilter = SearchFilter.Album;
                    performSearch();
                }
            }
        });

        _searchByArtist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    _searchFilter = SearchFilter.Artist;
                    performSearch();
                }
            }
        });
    }
    
    public void enableInteraction()
    {
        _searchFieldClearButton.setClickable(true);
        _searchResultsList.setClickable(true);
    }

    public void disableInteraction()
    {
        _searchFieldClearButton.setClickable(false);
        _searchResultsList.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options)
    {
        FragmentActivity a = getActivity();

        if (a == null)
        {
            return;
        }

        FragmentManager manager = a.getSupportFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();

        String newEntryName = playlist.getName();
        String lastEntryName = backStackCount > 0 ? manager.getBackStackEntryAt(backStackCount-1).getName() : "";

        // Do nothing, if the last entry name is equal to the new entry name
        if (lastEntryName != null && lastEntryName.equals(newEntryName))
        {
            return;
        }

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }
        
        BasePresenter presenter = new PlaylistPresenter(playlist, audioInfo, options);
        PlaylistFragment view = PlaylistFragment.newInstance(presenter, options);
        presenter.setView(view);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.scale_up, R.anim.long_hold, R.anim.hold, R.anim.hold);
        transaction.add(R.id.mainLayout, view, newEntryName);
        transaction.addToBackStack(newEntryName);
        transaction.hide(this);
        transaction.commit();
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<BaseAudioPlaylist> playlists)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {
        Activity a = getActivity();
        
        if (a == null)
        {
            return;
        }
        
        Intent intent = new Intent(a, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("playlist", Serializing.serializeObject(playlist));
        startActivity(intent);
    }

    @Override
    public void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {
        
    }

    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        _searchField.setText(searchQuery);

        if (_searchFilter != filter)
        {
            _searchFilter = filter;
            checkCorrectSearchFilterRadioButton(filter);
        }

        _searchState.setVisibility(View.VISIBLE);

        if (searchState != null)
        {
            showProgressIndicator();

            _searchState.setText(searchState);
        }
        else
        {
            hideProgressIndicator();

            if (songs.size() > 0)
            {
                _searchState.setText(String.valueOf(songs.size()) + " " + getResources().getString(R.string.search_results_tip));
            }
            else
            {
                _searchState.setText(R.string.search_results_tip_no_results);
            }
        }

        _searchResultsAdapter = new SearchListAdapter(context, songs, this);
        _searchResultsList.setAdapter(_searchResultsAdapter);
        _searchResultsList.invalidateViews();
    }

    @Override
    public void onPlayerPlay(@NonNull BaseAudioTrack current)
    {
        if (_searchResultsAdapter == null)
        {
            return;
        }

        boolean result = _searchResultsAdapter.isItemSelectedForTrack(current);

        if (!result)
        {
            _searchResultsList.invalidateViews();
        }
    }

    @Override
    public void onPlayerFinish()
    {
        _searchResultsList.invalidateViews();
    }

    @Override
    public void onPlayerStop()
    {

    }

    @Override
    public void onPlayerPause(@NonNull BaseAudioTrack track)
    {

    }

    @Override
    public void onPlayerResume(@NonNull BaseAudioTrack track)
    {

    }

    @Override
    public void onPlayOrderChange(AudioPlayOrder order)
    {

    }

    @Override
    public void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage)
    {

    }

    @Override
    public void onResetAppSettings()
    {
        
    }

    @Override
    public void onAppThemeChanged(AppSettings.AppTheme appTheme)
    {

    }

    @Override
    public void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onDeviceLibraryChanged()
    {

    }

    @Override
    public void onFetchDataErrorEncountered(@NonNull Exception error)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {
        Context context = getContext();
        
        if (context == null) {
            return;
        }
        
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                _searchResultsList.invalidateViews();
            }
        };

        AlertWindows.showAlert(context, R.string.error_invalid_file, R.string.error_invalid_file_play, R.string.ok, action);
    }

    @Override
    public boolean isMarkedFavorite(@NonNull BaseAudioTrack track)
    {
        return GeneralStorage.getShared().favorites.isMarkedFavorite(track);
    }

    private void performSearch()
    {
        _presenter.onSearchQuery(_searchField.getText().toString(), _searchFilter);
    }
    
    private void showProgressIndicator()
    {
        _progressIndicator.setVisibility(View.VISIBLE);
    }
    
    private void hideProgressIndicator()
    {
        _progressIndicator.setVisibility(View.GONE);
    }

    private void checkCorrectSearchFilterRadioButton(SearchFilter filter)
    {
        switch (filter)
        {
            case Title:
                _searchByTrack.setChecked(true);
                break;
            case Album:
                _searchByAlbum.setChecked(true);
                break;
            case Artist:
                _searchByArtist.setChecked(true);
                break;
        }
    }
}