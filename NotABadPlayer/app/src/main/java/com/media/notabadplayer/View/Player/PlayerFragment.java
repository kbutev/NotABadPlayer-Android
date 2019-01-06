package com.media.notabadplayer.View.Player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.MediaPlayerObserver;
import com.media.notabadplayer.Audio.MediaTrack;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Controlls.ApplicationInput;
import com.media.notabadplayer.Controlls.KeyBinds;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class PlayerFragment extends Fragment implements BaseView, MediaPlayerObserver
{
    private BasePresenter _presenter;

    AudioPlayer _player = AudioPlayer.getShared();
    
    private Handler _handler = new Handler();
    
    private ImageView _imageCover;
    private TextView _labelTitle;
    private TextView _labelArtist;
    private SeekBar _mediaBar;
    private TextView _labelDurationCurrent;
    private TextView _labelDurationTotal;
    private Button _buttonBack;
    private Button _buttonPlay;
    private Button _buttonForward;
    
    public PlayerFragment()
    {

    }
    
    public static PlayerFragment newInstance()
    {
        return new PlayerFragment();
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
    }
    
    @Override
    public void onPause()
    {
       super.onPause();
        _player.detachObserver(this); 
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_player, container, false);
        
        // Setup UI
        _imageCover = root.findViewById(R.id.cover);
        _labelTitle = root.findViewById(R.id.songTitle);
        _labelArtist = root.findViewById(R.id.songArtist);
        _mediaBar = root.findViewById(R.id.mediaBar);
        _labelDurationCurrent = root.findViewById(R.id.durationCurrent);
        _labelDurationTotal = root.findViewById(R.id.durationTotal);
        _buttonBack = root.findViewById(R.id.mediaButtonBack);
        _buttonPlay = root.findViewById(R.id.mediaButtonPlay);
        _buttonForward = root.findViewById(R.id.mediaButtonForward);
        
        // Init UI
        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        _mediaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                {
                    return;
                }
                
                _player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyBinds.getShared().respondToInput(ApplicationInput.PLAYER_PLAY_BUTTON);
                
                if (_player.isPlaying())
                {
                    _buttonPlay.setText("Pause");
                }
                else
                {
                    _buttonPlay.setText("Resume");
                }
            }
        });

        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyBinds.getShared().respondToInput(ApplicationInput.PLAYER_PREVIOUS_BUTTON);
            }
        });

        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyBinds.getShared().respondToInput(ApplicationInput.PLAYER_NEXT_BUTTON);
            }
        });
    }
    
    private void updateUIState()
    {
        int currentPosition = _player.getPlayer().getCurrentPosition() / 1000;
        _mediaBar.setProgress(currentPosition);
        _labelDurationCurrent.setText(MediaTrack.secondsToString(currentPosition));
    }
    
    @Override
    public void setPresenter(BasePresenter presenter) {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover) 
    {

    }

    @Override
    public void onMediaAlbumsLoad(ArrayList<AlbumInfo> albums) 
    {

    }

    @Override
    public void onAlbumSongsLoad(ArrayList<MediaTrack> songs)
    {

    }

    @Override
    public void openPlayerScreen()
    {

    }

    @Override
    public void openPlayerScreen(MediaTrack track) 
    {

    }
  
    @Override
    public void onPlayerPlay(MediaTrack current)
    {
        _imageCover.setImageURI(Uri.parse(Uri.decode(current.artCover)));
        _labelTitle.setText(current.title);
        _labelArtist.setText(current.artist);
        _mediaBar.setMax((int)current.durationInSeconds);
        _labelDurationTotal.setText(current.duration);
        
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (getActivity() != null)
            {
                updateUIState();
                
                _handler.postDelayed(this, 100);
            }
          }
        });
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
    public void onPlayerPause()
    {
        
    }
    
    @Override
    public void onPlayerResume()
    {
        
    }
    
    @Override
    public void onPlayerVolumeChanged()
    {
        
    }
}
