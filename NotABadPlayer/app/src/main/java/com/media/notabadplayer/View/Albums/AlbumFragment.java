package com.media.notabadplayer.View.Albums;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    AudioPlayer _player = AudioPlayer.getShared();
    
    private BasePresenter _presenter;
    
    private GridView _table;
    private AlbumListAdapter _tableAdapter;
    private Parcelable _tableState;
    
    public AlbumFragment()
    {
        
    }
    
    public static AlbumFragment newInstance()
    {
        return new AlbumFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        _player.attachObserver(this);
        
        _presenter.start();

        if (_tableState != null)
        {
            _table.onRestoreInstanceState(_tableState);
        }

        _table.invalidateViews();
    }

    @Override
    public void onPause()
    {
        _tableState = _table.onSaveInstanceState();
        
        super.onPause();
        
        _player.detachObserver(this);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);
        
        _table = root.findViewById(R.id.albumSongsGrid);

        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position != 0)
                {
                    UIAnimations.animateAlbumItemTAP(getContext(), view);
                    _presenter.onAlbumsItemClick(position);
                }
            }
        });
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }
    
    @Override
    public void openAlbumScreen(@NonNull AudioAlbum album) 
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
        _tableAdapter = new AlbumListAdapter(getContext(), songs);
        _table.setAdapter(_tableAdapter);
    }
    
    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist, boolean sortTracks)
    {
        _tableAdapter = new AlbumListAdapter(getContext(), playlist, sortTracks);
        _table.setAdapter(_tableAdapter);
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
    public void onPlayerVolumeChanged()
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
}