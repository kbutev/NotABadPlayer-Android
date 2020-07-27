package com.media.notabadplayer.View.Lists;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;
import com.google.common.base.Function;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.ListsPresenter;
import com.media.notabadplayer.Presenter.PlaylistPresenter;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Playlist.PlaylistFragment;
import com.media.notabadplayer.View.BaseView;

public class ListsFragment extends Fragment implements BaseView {
    private BasePresenter _presenter;
    
    private Button _createPlaylistButton;
    private Button _editPlaylistButton;
    private Button _donePlaylistButton;
    private ListView _playlistsList;
    private ListAdapter _playlistsAdapter;

    private ProgressBar _progressIndicator;
    
    private boolean _playlistsLoaded = false;
    
    private Function<Integer, Void> onRemoveButtonClick = new Function<Integer, Void>() {
        @Override
        public Void apply(Integer integer) {
            if (integer != 0)
            {
                deletePlaylistOnIndex(integer);
            }

            return null;
        }
    };
    
    public ListsFragment()
    {

    }
    
    public static @NonNull ListsFragment newInstance(@NonNull BasePresenter presenter)
    {
        ListsFragment fragment = new ListsFragment();
        fragment._presenter = presenter;
        return fragment;
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlists, container, false);
        
        _createPlaylistButton = root.findViewById(R.id.createPlaylistButton);
        _editPlaylistButton = root.findViewById(R.id.editPlaylistButton);
        _donePlaylistButton = root.findViewById(R.id.donePlaylistButton);
        _playlistsList = root.findViewById(R.id.playlistsList);
        _progressIndicator = root.findViewById(R.id.progressIndicator);
        
        // Setup UI
        initUI();
        
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        _presenter.start();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();

        enableInteraction();
        
        // Request data from the presenter every time we resume
        fetchNewData();
        
        // Always make sure that we are not in edit mode when resuming
        endEditMode();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        disableInteraction();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            fetchNewData();
        }
    }
    
    private void initUI()
    {
        _createPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_createPlaylistButton.isClickable())
                {
                    return;
                }

                openCreatePlaylistScreen(null);
            }
        });

        _editPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_editPlaylistButton.isClickable())
                {
                    return;
                }

                enterEditMode();
            }
        });

        _donePlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_donePlaylistButton.isClickable())
                {
                    return;
                }

                endEditMode();
            }
        });
        
        _playlistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!_playlistsList.isClickable())
                {
                    return;
                }

                if (!_playlistsAdapter.isInEditMode())
                {
                    _presenter.onPlaylistItemClick(position);
                }
                else
                {
                    _presenter.onPlaylistItemEdit(position);
                }
            }
        });
    }
    
    private void enterEditMode()
    {
        // Until the playlists are loaded, these requests will be ignored
        if (!_playlistsLoaded)
        {
            return;
        }
        
        if (_playlistsAdapter == null)
        {
            return;
        }
        
        _playlistsAdapter.enterEditMode();
        _editPlaylistButton.setVisibility(View.GONE);
        _donePlaylistButton.setVisibility(View.VISIBLE);
    }

    private void endEditMode()
    {
        if (_playlistsAdapter == null)
        {
            return;
        }
        
        _playlistsAdapter.leaveEditMode();
        _donePlaylistButton.setVisibility(View.GONE);
        _editPlaylistButton.setVisibility(View.VISIBLE);
    }
    
    private void deletePlaylistOnIndex(int position)
    {
        _presenter.onPlaylistItemDelete(position);
    }
    
    public void enableInteraction()
    {
        _createPlaylistButton.setClickable(true);
        _editPlaylistButton.setClickable(true);
        _donePlaylistButton.setClickable(true);
        _playlistsList.setClickable(true);
    }

    public void disableInteraction()
    {
        _createPlaylistButton.setClickable(false);
        _editPlaylistButton.setClickable(false);
        _donePlaylistButton.setClickable(false);
        _playlistsList.setClickable(false);
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
        Context context = getContext();

        if (context == null)
        {
            return;
        }
        
        boolean isInEditMode = _playlistsAdapter != null && _playlistsAdapter.isInEditMode();
        
        // Update adapter
        _playlistsLoaded = true;
        _playlistsAdapter = new ListAdapter(context, playlists, onRemoveButtonClick);
        _playlistsList.setAdapter(_playlistsAdapter);
        
        if (isInEditMode) 
        {
            _playlistsAdapter.enterEditMode();
        }

        _playlistsList.invalidateViews();
        
        // Hide progress
        hideProgressIndicator();
    }
    
    @Override
    public void openPlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {

    }

    @Override
    public void openCreatePlaylistScreen(@Nullable BaseAudioPlaylist playlistToEdit)
    {
        Activity a = getActivity();

        if (a == null)
        {
            return;
        }

        // Until the playlists are loaded, these requests will be ignored
        if (!_playlistsLoaded)
        {
            return;
        }

        Intent intent = new Intent(a, CreatePlaylistActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (playlistToEdit != null) 
        {
            intent.putExtra("playlist", Serializing.serializeObject(playlistToEdit));
            Log.v(ListsFragment.class.getCanonicalName(), "Open create list activity by editing playlist '" + playlistToEdit.getName() + "'");
        } else 
            {
            Log.v(ListsFragment.class.getCanonicalName(), "Open create list activity");
        }

        startActivity(intent);
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
        if (getView() == null)
        {
            return;
        }
        
        // Retry until we succeed
        retryFetchNewDataAfterDelay();
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
                // Do nothing
            }
        };

        AlertWindows.showAlert(context, R.string.error_invalid_file, R.string.error_invalid_file_play, R.string.ok, action);
    }

    private void showProgressIndicator()
    {
        _progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator()
    {
        _progressIndicator.setVisibility(View.GONE);
    }

    private void fetchNewData() {
        _presenter.fetchData();
    }

    private void retryFetchNewDataAfterDelay() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                fetchNewData();
            }
        };
        mainHandler.postDelayed(myRunnable, 100);
    }
}
