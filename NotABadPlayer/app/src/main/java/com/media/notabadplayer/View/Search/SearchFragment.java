package com.media.notabadplayer.View.Search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.Playlist.PlaylistPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Playlist.PlaylistFragment;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    AudioPlayer _player = AudioPlayer.getShared();
    
    private BasePresenter _presenter;
    
    private EditText _searchField;
    private ImageButton _searchFieldClearButton;
    private TextView _searchTip;
    private ListView _searchResults;
    private SearchListAdapter _searchResultsAdapter;
    private Parcelable _searchResultsState;
    
    public SearchFragment()
    {
        
    }
    
    public static @NonNull SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        
        // Setup UI
        _searchField = root.findViewById(R.id.searchField);
        _searchFieldClearButton = root.findViewById(R.id.searchFieldClearButton);
        _searchTip = root.findViewById(R.id.searchTip);
        _searchResults = root.findViewById(R.id.searchResultsList);
        
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

        // Retrieve saved search query, if there is one
        String searchQuery = GeneralStorage.getShared().retrieveSearchQuery();

        if (!_searchField.getText().toString().equals(searchQuery))
        {
            _searchField.setText(searchQuery);
            _presenter.onSearchQuery(_searchField.getText().toString());
        }
        
        if (_searchResultsAdapter != null)
        {
            _searchResults.setAdapter(_searchResultsAdapter);
        }
        
        if (_searchResultsState != null)
        {
            _searchResults.onRestoreInstanceState(_searchResultsState);
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
        _searchResultsState = _searchResults.onSaveInstanceState();
        
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
                    _presenter.onSearchQuery(_searchField.getText().toString());
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
        
        _searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!_searchResults.isClickable())
                {
                    return;
                }
                
                _searchResultsAdapter.selectItem(view);

                _presenter.onSearchResultClick(position);
            }
        });
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void enableInteraction()
    {
        _searchFieldClearButton.setClickable(true);
        _searchResults.setClickable(true);
    }

    @Override
    public void disableInteraction()
    {
        _searchFieldClearButton.setClickable(false);
        _searchResults.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist)
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
        
        PlaylistFragment f = PlaylistFragment.newInstance();
        f.setPresenter(new PlaylistPresenter(f, playlist, audioInfo));

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
        transaction.replace(R.id.mainLayout, f);
        transaction.addToBackStack(newEntryName).commit();
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull AudioPlaylist playlist)
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
    public void updatePlayerScreen(@NonNull AudioPlaylist playlist)
    {
        
    }

    @Override
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        _searchTip.setVisibility(View.VISIBLE);
        
        if (songs.size() > 0)
        {
            _searchTip.setText(String.valueOf(songs.size() + " " + getResources().getString(R.string.search_results_tip)));
        }
        else
        {
            _searchTip.setText(R.string.search_results_tip_no_results);
        }

        _searchResultsAdapter = new SearchListAdapter(context, songs);
        _searchResults.setAdapter(_searchResultsAdapter);
        _searchResults.invalidateViews();
        
        // Save search query
        GeneralStorage.getShared().saveSearchQuery(searchQuery);
    }

    @Override
    public void onPlayerPlay(AudioTrack current)
    {

    }

    @Override
    public void onPlayerFinish()
    {
        _searchResults.invalidateViews();
    }

    @Override
    public void onPlayerStop()
    {

    }

    @Override
    public void onPlayerPause(AudioTrack track)
    {

    }

    @Override
    public void onPlayerResume(AudioTrack track)
    {

    }

    @Override
    public void onPlayOrderChange(AudioPlayOrder order)
    {

    }

    @Override
    public void appSettingsReset()
    {

    }

    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {

    }

    @Override
    public void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
}