package com.media.notabadplayer.View.Playlist;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.LooperService;
import com.media.notabadplayer.Utilities.LooperClient;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

public class PlaylistFragment extends Fragment implements BaseView, AudioPlayerObserver, LooperClient
{
    Player _player = Player.getShared();
    
    private BasePresenter _presenter;
    
    private GridView _table;
    private @Nullable PlaylistListAdapter _tableAdapter;
    private Parcelable _tableState;
    
    private TextView _albumTitleHeader;
    
    public PlaylistFragment()
    {
        
    }
    
    public static @NonNull PlaylistFragment newInstance(@NonNull BasePresenter presenter)
    {
        PlaylistFragment fragment = new PlaylistFragment();
        fragment._presenter = presenter;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        _table = root.findViewById(R.id.albumSongsGrid);

        _albumTitleHeader = root.findViewById(R.id.albumTitleHeader);

        initUI();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        _presenter.start();

        _player.observers.attach(this);

        startLooping();
    }
    
    @Override
    public void onStart()
    {
        super.onStart();

        if (_tableState != null)
        {
            _table.onRestoreInstanceState(_tableState);
        }
        
        _table.invalidateViews();
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
        _tableState = _table.onSaveInstanceState();
        
        super.onPause();

        disableInteraction();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        _player.observers.detach(this);

        stopLooping();
    }
    
    private void initUI()
    {
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!_table.isClickable())
                {
                    return;
                }

                // Index zero is reserved for the header
                // Decrement the value and make sure its a valid index
                // Index zero is the header - ignore
                if (position == 0)
                {
                    return;
                }

                position--;

                if (position >= 0 && _tableAdapter != null)
                {
                    _tableAdapter.selectItem(view, position);
                    
                    _presenter.onPlaylistItemClick(position);
                }
            }
        });
    }

    private void updateUIState()
    {
        if (_tableAdapter == null)
        {
            return;
        }
        
        if (_tableAdapter.isHeaderVisible(_table))
        {
            if (_albumTitleHeader.getVisibility() != View.GONE)
            {
                _albumTitleHeader.setVisibility(View.GONE);
            }
        }
        else
        {
            if (_albumTitleHeader.getVisibility() != View.VISIBLE)
            {
                _albumTitleHeader.setVisibility(View.VISIBLE);
                UIAnimations.getShared().animateFadeIn(getContext(), _albumTitleHeader);
            }
        }
    }

    private void startLooping()
    {
        LooperService.getShared().subscribe(this);
    }

    private void stopLooping()
    {
        LooperService.getShared().unsubscribe(this);
    }

    public void enableInteraction()
    {
        _table.setClickable(true);
    }

    public void disableInteraction()
    {
        _table.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist)
    {

    }
    
    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        // Table update
        _tableAdapter = new PlaylistListAdapter(context, playlist);
        _table.setAdapter(_tableAdapter);

        // Update album title header
        _albumTitleHeader.setText(playlist.getName());
        
        // Scroll down to the currently playing track
        AudioPlaylist audioPlaylist = Player.getShared().getPlaylist();

        if (audioPlaylist != null && audioPlaylist.getName().equals(playlist.getName()))
        {
            AudioTrack playingTrack = audioPlaylist.getPlayingTrack();

            int index = playlist.getTracks().indexOf(playingTrack);

            if (index >= 0)
            {
                _table.setSelection(index);
            }
        }
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
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<AudioTrack> songs, @Nullable String searchState)
    {
        
    }
    
    @Override
    public void onPlayerPlay(@NonNull AudioTrack current)
    {
        if (_tableAdapter == null)
        {
            return;
        }

        boolean result = _tableAdapter.isItemSelectedForTrack(current);
        
        if (!result)
        {
            _table.invalidateViews();
        }
    }
    
    @Override
    public void onPlayerFinish()
    {
        _table.invalidateViews();
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
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                _table.invalidateViews();
            }
        };
        
        AlertWindows.showAlert(getContext(), R.string.error_invalid_file, R.string.error_invalid_file_play, R.string.ok, action);
    }

    @Override
    public void loop()
    {
        FragmentActivity a = getActivity();

        if (a != null)
        {
            if (a.hasWindowFocus())
            {
                updateUIState();
            }
        }
    }
}
