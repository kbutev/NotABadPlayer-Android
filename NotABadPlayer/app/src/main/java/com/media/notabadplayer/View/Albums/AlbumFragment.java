package com.media.notabadplayer.View.Albums;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.MediaTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;

    private ImageView _albumCover;
    private TextView _albumTitle;
    private GridView _albumSongs;
    
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
        _presenter.start();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);
        
        // Setup UI
        _albumCover = root.findViewById(R.id.albumCover);
        _albumTitle = root.findViewById(R.id.albumTitle);
        _albumSongs = root.findViewById(R.id.albumSongs);
        _albumSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                _presenter.onAlbumsItemClick(position);
            }
        });
        
        return root;
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }
    
    @Override
    public void openAlbumScreen(com.media.notabadplayer.Audio.MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover) 
    {
        
    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums)
    {

    }
    
    @Override
    public void onAlbumSongsLoad(ArrayList<MediaTrack> songs)
    {
        if (false)
        {
            MediaTrack firstTrack = songs.get(0);
            
            String uri = Uri.decode(firstTrack.artCover);
            
            if (uri != null)
            {
                _albumCover.setImageURI(Uri.parse(uri));
                _albumCover.setVisibility(View.VISIBLE);
            }
            
            _albumTitle.setText(firstTrack.albumTitle);
        }
        
        _albumSongs.setAdapter(new AlbumListAdapter(getContext(), songs));
    }
    
    @Override
    public void openPlayerScreen(MediaTrack track)
    {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("track", track.toString());
        startActivity(intent);
    }
}