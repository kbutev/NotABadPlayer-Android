package com.media.notabadplayer.View.Albums;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    AudioPlayer _player = AudioPlayer.getShared();
    
    private BasePresenter _presenter;
    
    private GridView _table;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);
        
        _table = root.findViewById(R.id.albumSongs);

        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                _presenter.onAlbumsItemClick(position);
            }
        });
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }
    
    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover) 
    {
        
    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums)
    {

    }
    
    @Override
    public void onAlbumSongsLoad(ArrayList<AudioTrack> songs)
    {
        _table.setAdapter(new AlbumListAdapter(getContext(), songs));
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
}