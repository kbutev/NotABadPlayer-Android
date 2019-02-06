package com.media.notabadplayer.View.Search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.Presenter.Search.SearchPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private EditText _searchField;
    private TextView _searchTip;
    private ListView _searchResults;

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
        _searchTip = root.findViewById(R.id.searchTip);
        _searchResults = root.findViewById(R.id.searchResults);
        
        initUI();
        
        return root;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        _presenter.start();
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
        intent.putExtra("tracks", playlist.getTracksAsStrings());
        intent.putExtra("playingTrack", playlist.getPlayingTrackAsString());
        startActivity(intent);
    }

    @Override
    public void searchQueryResults(ArrayList<AudioTrack> songs)
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
        
        _searchResults.setAdapter(new SearchListAdapter(getContext(), songs));
        _searchResults.invalidateViews();
    }
}