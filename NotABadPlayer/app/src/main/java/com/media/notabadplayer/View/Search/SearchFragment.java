package com.media.notabadplayer.View.Search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements BaseView
{
    private boolean _resumedOnce = false;
    
    private BasePresenter _presenter;
    
    private EditText _searchField;
    private ImageButton _searchFieldClearButton;
    private TextView _searchTip;
    private ListView _searchResults;
    private ListAdapter _searchResultsAdapter;
    private Parcelable _searchResultsState;
    
    public SearchFragment()
    {
        
    }
    
    public static SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        
        // Setup UI
        _searchField = root.findViewById(R.id.searchField);
        _searchFieldClearButton = root.findViewById(R.id.searchFieldClearButton);
        _searchTip = root.findViewById(R.id.searchTip);
        _searchResults = root.findViewById(R.id.searchResults);
        
        initUI();
        
        return root;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if (!_resumedOnce)
        {
            _resumedOnce = true;
            
            _presenter.start();

            // Retrieve saved search query, if there is one
            String searchQuery = GeneralStorage.getShared().retrieveSearchQuery(getContext());

            if (!_searchField.getText().toString().equals(searchQuery))
            {
                _searchField.setText(searchQuery);
                _presenter.onSearchQuery(_searchField.getText().toString());
            }
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
    public void onPause()
    {
        _searchResultsState = _searchResults.onSaveInstanceState();
        super.onPause();
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
                _searchField.setText("");
            }
        });
        
        _searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                _presenter.onSearchResultClick(position);
            }
        });
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover) {
        
    }

    @Override
    public void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums)
    {

    }

    @Override
    public void onAlbumSongsLoad(ArrayList<AudioTrack> songs)
    {

    }
    
    @Override
    public void openPlayerScreen(AudioPlaylist playlist)
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
    public void searchQueryResults(String searchQuery, ArrayList<AudioTrack> songs)
    {
        _searchTip.setVisibility(View.VISIBLE);
        
        if (songs.size() > 0)
        {
            _searchTip.setText(String.valueOf(songs.size() + " " + getResources().getString(R.string.search_results_tip)));
        }
        else
        {
            _searchTip.setText(R.string.search_results_tip_no_results);
        }

        _searchResultsAdapter = new SearchListAdapter(getContext(), songs);
        _searchResults.setAdapter(_searchResultsAdapter);
        _searchResults.invalidateViews();
        
        // Save search query
        GeneralStorage.getShared().saveSearchQuery(getContext(), searchQuery);
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
}