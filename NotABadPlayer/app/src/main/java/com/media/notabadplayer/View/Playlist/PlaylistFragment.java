package com.media.notabadplayer.View.Playlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.LooperService;
import com.media.notabadplayer.Utilities.LooperClient;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements BaseView, AudioPlayerObserver, LooperClient
{
    AudioPlayer _player = AudioPlayer.getShared();
    
    private BasePresenter _presenter;
    
    private GridView _table;
    private PlaylistListAdapter _tableAdapter;
    private Parcelable _tableState;
    
    private TextView _albumTitleHeader;
    
    public PlaylistFragment()
    {
        
    }
    
    public static @NonNull PlaylistFragment newInstance()
    {
        return new PlaylistFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);

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

                if (position != 0)
                {
                    _presenter.onPlaylistItemClick(position);

                    UIAnimations.animateAlbumItemTAP(getContext(), view);
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
                UIAnimations.animateViewFadeOut(getContext(), _albumTitleHeader);
            }
        }
        else
        {
            if (_albumTitleHeader.getVisibility() != View.VISIBLE)
            {
                _albumTitleHeader.setVisibility(View.VISIBLE);
                UIAnimations.animateViewFadeIn(getContext(), _albumTitleHeader);
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

    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void enableInteraction()
    {
        _table.setClickable(true);
    }

    @Override
    public void disableInteraction()
    {
        _table.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioAlbum album)
    {

    }
    
    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist)
    {

    }
    
    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
    {

    }
    
    @Override
    public void onAlbumSongsLoad(@NonNull ArrayList<AudioTrack> songs)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        // Table update
        _tableAdapter = new PlaylistListAdapter(context, songs);
        _table.setAdapter(_tableAdapter);

        // Update album title header
        _albumTitleHeader.setText(songs.get(0).albumTitle);
        
        // Scroll down to the currently playing track
        AudioPlaylist audioPlaylist = AudioPlayer.getShared().getPlaylist();
        
        if (audioPlaylist != null && audioPlaylist.getName().equals(songs.get(0).albumTitle))
        {
            AudioTrack playingTrack = audioPlaylist.getPlayingTrack();
            
            int index = songs.indexOf(playingTrack);
            
            if (index >= 0)
            {
                _table.setSelection(index);
            }
        }
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
        _tableAdapter = new PlaylistListAdapter(context, playlist, false);
        _table.setAdapter(_tableAdapter);

        // Update album title header
        _albumTitleHeader.setText(playlist.getName());
        
        // Scroll down to the currently playing track
        AudioPlaylist audioPlaylist = AudioPlayer.getShared().getPlaylist();

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
        
    }
    
    @Override
    public void onPlayerPlay(AudioTrack current)
    {
        _table.invalidateViews();
    }

    @Override
    public void onPlayerFinish()
    {
        
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
        _tableAdapter.sortTracks(trackSorting);
        _table.invalidateViews();
        
        if (_tableState != null)
        {
            _table.onRestoreInstanceState(_tableState);
        }
    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

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
