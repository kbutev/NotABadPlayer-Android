package com.media.notabadplayer.View.Albums;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.media.notabadplayer.Audio.MediaAlbum;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Audio.MediaPlayer;
import com.media.notabadplayer.Audio.MediaPlayerObserver;
import com.media.notabadplayer.Audio.MediaTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements BaseView, MediaPlayerObserver
{
    MediaPlayer _player = MediaPlayer.getShared();
    
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
    public void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumArtist, String albumTitle, String albumCover) 
    {
        
    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<MediaAlbum> albums)
    {

    }
    
    @Override
    public void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.MediaTrack> songs)
    {
        _table.setAdapter(new AlbumListAdapter(getContext(), songs));
    }
    
    @Override
    public void openPlayerScreen(com.media.notabadplayer.Audio.MediaPlayerPlaylist playlist)
    {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        ArrayList<String> tracks = new ArrayList<>();
        
        for (int e = 0; e < playlist.size(); e++)
        {
            tracks.add(playlist.getTrack(e).toString());
        }
        
        intent.putExtra("tracks", tracks);
        
        if (playlist.getPlayingTrack() != null)
        {
            intent.putExtra("playingTrack", playlist.getPlayingTrack().toString());
        }
        
        startActivity(intent);
    }

    @Override
    public void onPlayerPlay(com.media.notabadplayer.Audio.MediaTrack current)
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
    public void onPlayerPause(MediaTrack track)
    {
        
    }

    @Override
    public void onPlayerResume(MediaTrack track)
    {
        
    }

    @Override
    public void onPlayerVolumeChanged()
    {

    }
}