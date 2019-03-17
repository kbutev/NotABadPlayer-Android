package com.media.notabadplayer.View.Lists;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.R;

public class CreatePlaylistActivity extends AppCompatActivity
{
    private AudioPlaylist _playlist;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Content
        setContentView(R.layout.activity_create_playlist);
        
        // UI
        initUI();
    }
    
    private void initUI()
    {
        
    }
    
    private void savePlaylist()
    {
        
    }
}
