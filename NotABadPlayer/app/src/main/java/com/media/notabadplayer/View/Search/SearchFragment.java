package com.media.notabadplayer.View.Search;

import java.util.List;
import android.app.Activity;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.PlaylistPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Playlist.PlaylistFragment;

public class SearchFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    Player _player = Player.getShared();
    
    private BasePresenter _presenter;
    
    private EditText _searchField;
    private ImageButton _searchFieldClearButton;
    private TextView _searchTip;
    private ListView _searchResults;
    private ProgressBar _progressIndicator;
    
    private SearchListAdapter _searchResultsAdapter;
    private Parcelable _searchResultsState;
    
    public SearchFragment()
    {
        
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
        _searchTip = root.findViewById(R.id.searchTip);
        _searchResults = root.findViewById(R.id.searchResultsList);
        _progressIndicator = root.findViewById(R.id.progressIndicator);
        
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
    
    public void enableInteraction()
    {
        _searchFieldClearButton.setClickable(true);
        _searchResults.setClickable(true);
    }

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
        
        BasePresenter presenter = new PlaylistPresenter(playlist, audioInfo);
        PlaylistFragment view = PlaylistFragment.newInstance(presenter);
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
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<AudioPlaylist> playlists)
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
    public void searchQueryResults(@NonNull String searchQuery, @NonNull List<AudioTrack> songs, @Nullable String searchTip)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        _searchField.setText(searchQuery);

        _searchTip.setVisibility(View.VISIBLE);

        if (searchTip != null)
        {
            showProgressIndicator();
            
            _searchTip.setText(searchTip);
        }
        else
        {
            hideProgressIndicator();
            
            if (songs.size() > 0)
            {
                _searchTip.setText(String.valueOf(songs.size() + " " + getResources().getString(R.string.search_results_tip)));
            }
            else
            {
                _searchTip.setText(R.string.search_results_tip_no_results);
            }
        }

        _searchResultsAdapter = new SearchListAdapter(context, songs);
        _searchResults.setAdapter(_searchResultsAdapter);
        _searchResults.invalidateViews();
    }

    @Override
    public void onPlayerPlay(@NonNull AudioTrack current)
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
    public void onPlayerPause(@NonNull AudioTrack track)
    {

    }

    @Override
    public void onPlayerResume(@NonNull AudioTrack track)
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
    public void appSettingsReset()
    {
        
    }

    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {

    }

    @Override
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onFetchDataErrorEncountered(@NonNull Exception error)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
    
    private void showProgressIndicator()
    {
        _progressIndicator.setVisibility(View.VISIBLE);
    }
    
    private void hideProgressIndicator()
    {
        _progressIndicator.setVisibility(View.GONE);
    }
}