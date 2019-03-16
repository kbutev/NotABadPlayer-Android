package com.media.notabadplayer.View.Player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class PlayerFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    private static float SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED = 80;
    private static float SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED = 400;
    
    private boolean _resumedOnce = false;

    private BasePresenter _presenter;

    AudioPlayer _player = AudioPlayer.getShared();
    
    private Handler _handler = new Handler();
    
    private LinearLayout _layout;
    private ImageView _imageCover;
    private TextView _labelTitle;
    private TextView _labelAlbum;
    private TextView _labelArtist;
    private SeekBar _mediaBar;
    private TextView _labelDurationCurrent;
    private TextView _labelDurationTotal;
    private Button _buttonRecall;
    private Button _buttonBack;
    private Button _buttonPlay;
    private Button _buttonForward;
    private Button _buttonPlayOrder;

    private float _layoutTouchMotionLastXPosition = -1;
    private float _layoutTouchMotionLastYPosition = -1;
    
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
        
        if (!_resumedOnce)
        {
            _resumedOnce = true;
            _presenter.start();
        }
        
        // Save current audio state
        saveCurrentAudioState();
    }
    
    @Override
    public void onPause()
    {
       super.onPause();

        // Save current audio state
        saveCurrentAudioState();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _player.detachObserver(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_player, container, false);
        
        // Setup UI
        _layout = root.findViewById(R.id.layout);
        _imageCover = root.findViewById(R.id.cover);
        _labelTitle = root.findViewById(R.id.songTitle);
        _labelAlbum = root.findViewById(R.id.songAlbum);
        _labelArtist = root.findViewById(R.id.songArtist);
        _mediaBar = root.findViewById(R.id.mediaBar);
        _labelDurationCurrent = root.findViewById(R.id.durationCurrent);
        _labelDurationTotal = root.findViewById(R.id.durationTotal);
        _buttonRecall = root.findViewById(R.id.mediaButtonRecall);
        _buttonBack = root.findViewById(R.id.mediaButtonBack);
        _buttonPlay = root.findViewById(R.id.mediaButtonPlay);
        _buttonForward = root.findViewById(R.id.mediaButtonForward);
        _buttonPlayOrder = root.findViewById(R.id.mediaButtonPlayOrder);
        
        // Init UI
        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        final Fragment fragment = this;
        
        _layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return updateSwipeDown(event);
            }
        });
        
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
        
        _buttonRecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonRecall);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.PLAYER_RECALL);
            }
        });
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UIAnimations.animateButtonTAP(getContext(), _buttonPlay);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.PLAYER_PLAY_BUTTON);
            }
        });
        
        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UIAnimations.animateButtonTAP(getContext(), _buttonBack);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.PLAYER_PREVIOUS_BUTTON);
            }
        });
        
        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UIAnimations.animateButtonTAP(getContext(), _buttonForward);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.PLAYER_NEXT_BUTTON);
            }
        });
        
        _buttonPlayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonPlayOrder);
                KeyBinds.getShared().performAction(ApplicationAction.CHANGE_PLAY_ORDER);
                
                // Save current audio state
                saveCurrentAudioState();
            }
        });
        
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
        
        // Update play button state
        updatePlayButtonState();
    }
    
    private void updateUIState()
    {
        int currentPosition = _player.getPlayer().getCurrentPosition() / 1000;
        _mediaBar.setProgress(currentPosition);
        _labelDurationCurrent.setText(AudioTrack.secondsToString(currentPosition));
        
        AudioPlaylist playlist = AudioPlayer.getShared().getPlaylist();
        
        if (playlist != null)
        {
            AudioPlayOrder order = playlist.getPlayOrder();
            
            switch (order)
            {
                case FORWARDS:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_forwards);
                    break;
                case ONCE_FOREVER:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_repeat);
                    break;
                case SHUFFLE:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_shuffle);
                    break;
                default:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_forwards);
                    break;
            }
        }
    }
    
    private void saveCurrentAudioState()
    {
        GeneralStorage.getShared().savePlayerState(getContext());
        GeneralStorage.getShared().savePlayerPlayHistoryState(getContext());
    }
    
    private void updateMediaInfo(AudioTrack playingTrack)
    {
        if (playingTrack != null)
        {
            if (!playingTrack.artCover.isEmpty())
            {
                _imageCover.setImageURI(Uri.parse(Uri.decode(playingTrack.artCover)));
            }
            else
            {
                _imageCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_art_none));
            }
            
            _labelTitle.setText(playingTrack.title);
            _labelAlbum.setText(playingTrack.albumTitle);
            _labelArtist.setText(playingTrack.artist);
            _mediaBar.setMax((int) playingTrack.durationInSeconds);
            _labelDurationTotal.setText(playingTrack.duration);
            
            updatePlayButtonState();
        }
    }
    
    private void updatePlayButtonState()
    {
        if (_player.isPlaying())
        {
            _buttonPlay.setBackgroundResource(R.drawable.media_pause);
        }
        else
        {
            _buttonPlay.setBackgroundResource(R.drawable.media_play);
        }
    }
    
    private boolean updateSwipeDown(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float currentX = event.getX();
            float currentY = event.getY();

            if (_layoutTouchMotionLastYPosition == -1)
            {
                _layoutTouchMotionLastXPosition = currentX;
                _layoutTouchMotionLastYPosition = currentY;
                return false;
            }
            
            float diffX = currentX - _layoutTouchMotionLastXPosition;
            float diffY = currentY - _layoutTouchMotionLastYPosition;

            if (Math.abs(diffY) > SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED &&
                diffY > 0 &&
                Math.abs(diffX) <= SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED)
            {
                _layoutTouchMotionLastXPosition = -1;
                _layoutTouchMotionLastYPosition = -1;
                swipeDown();
                return true;
            }
        }
        
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            _layoutTouchMotionLastXPosition = -1;
            _layoutTouchMotionLastYPosition = -1;
        }
        
        return false;
    }
    
    private void swipeDown()
    {
        if (getActivity() != null)
        {
            getActivity().finish();
        }
    }
    
    @Override
    public void setPresenter(BasePresenter presenter) {
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

    }
    
    @Override
    public void openPlayerScreen(AudioPlaylist playlist) 
    {
        updateMediaInfo(playlist.getPlayingTrack());
    }

    @Override
    public void searchQueryResults(String searchQuery, ArrayList<AudioTrack> songs)
    {

    }
    
    @Override
    public void onPlayerPlay(AudioTrack current)
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_pause);
        
        updateMediaInfo(current);
    }
    
    @Override
    public void onPlayerFinish()
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_play);
    }
    
    @Override
    public void onPlayerStop()
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_play);
    }
    
    @Override
    public void onPlayerPause(AudioTrack track)
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_play);
    }
    
    @Override
    public void onPlayerResume(AudioTrack track)
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_pause);
    }
    
    @Override
    public void onPlayerVolumeChanged()
    {
        
    }

    @Override
    public void appThemeChanged()
    {

    }

    @Override
    public void appSortingChanged()
    {

    }
}
