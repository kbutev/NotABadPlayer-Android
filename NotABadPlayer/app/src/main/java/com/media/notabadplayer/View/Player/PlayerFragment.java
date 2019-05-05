package com.media.notabadplayer.View.Player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;

public class PlayerFragment extends Fragment implements BaseView, AudioPlayerObserver
{
    private static int MEDIA_BAR_MAX_VALUE = 100;
    private static int VOLUME_BAR_MAX_VALUE = 100;
    
    private BasePresenter _presenter;

    AudioPlayer _player = AudioPlayer.getShared();
    
    private Runnable _runnable = null;
    private Handler _handler = new Handler();
    
    private PlayerLayout _layout;
    private ImageView _imageCover;
    private SeekBar _volumeBar;
    private ImageView _volumeIcon;
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
    
    public PlayerFragment()
    {

    }
    
    public static @NonNull PlayerFragment newInstance()
    {
        return new PlayerFragment();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        
        AppSettings.ShowVolumeBar showBarState = GeneralStorage.getShared().getShowVolumeBarValue();
        
        if (showBarState == AppSettings.ShowVolumeBar.NO)
        {
            _volumeBar = root.findViewById(R.id.volumeBarLeftSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarLeftIcon);
            root.findViewById(R.id.volumeBarLeft).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.volumeBarRight).setVisibility(View.INVISIBLE);
        }
        
        if (showBarState == AppSettings.ShowVolumeBar.LEFT_SIDE)
        {
            _volumeBar = root.findViewById(R.id.volumeBarLeftSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarLeftIcon);
            root.findViewById(R.id.volumeBarLeft).setVisibility(View.VISIBLE);
            root.findViewById(R.id.volumeBarRight).setVisibility(View.INVISIBLE);
        }
        
        if (showBarState == AppSettings.ShowVolumeBar.RIGHT_SIDE)
        {
            _volumeBar = root.findViewById(R.id.volumeBarRightSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarRightIcon);
            root.findViewById(R.id.volumeBarRight).setVisibility(View.VISIBLE);
            root.findViewById(R.id.volumeBarLeft).setVisibility(View.INVISIBLE);
        }
        
        // Init UI
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

        saveCurrentAudioState();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        saveCurrentAudioState();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        _player.observers.detach(this);
    }
    
    private void initUI()
    {
        _volumeBar.setMax(VOLUME_BAR_MAX_VALUE);
        _volumeBar.setProgress(AudioPlayer.getShared().getVolume());
        _volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                {
                    return;
                }

                UIAnimations.animateImageTAP(getContext(), _volumeIcon);
                
                AudioPlayer.getShared().setVolume(progress);
                _volumeBar.setProgress(progress);
                
                // Reset the motion state of the layout, to prevent swipe down
                _layout.resetMotionState();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        
        _mediaBar.setMax(MEDIA_BAR_MAX_VALUE);
        _mediaBar.setProgress(1); // Set to a non-zero value, to prevent weird UI drawable glitch
        _mediaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                {
                    return;
                }
                
                double progressD = (double)progress / MEDIA_BAR_MAX_VALUE;
                double duration = _player.getDurationMSec();
                double newPosition = progressD * duration;
                _player.seekTo((int)newPosition);
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
                if (!_buttonRecall.isClickable())
                {
                    return;
                }

                UIAnimations.animateButtonTAP(getContext(), _buttonRecall);
                _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_RECALL);
            }
        });
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonPlay.isClickable())
                {
                    return;
                }

                UIAnimations.animateButtonTAP(getContext(), _buttonPlay);
                _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_PLAY_BUTTON);
            }
        });
        
        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonBack.isClickable())
                {
                    return;
                }

                UIAnimations.animateButtonTAP(getContext(), _buttonBack);
                _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_PREVIOUS_BUTTON);
            }
        });
        
        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonForward.isClickable())
                {
                    return;
                }

                UIAnimations.animateButtonTAP(getContext(), _buttonForward);
                _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_NEXT_BUTTON);
            }
        });
        
        _buttonPlayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonPlayOrder.isClickable())
                {
                    return;
                }

                UIAnimations.animateButtonTAP(getContext(), _buttonPlayOrder);
                _presenter.onPlayOrderButtonClick();
                
                // Save current audio state
                saveCurrentAudioState();
            }
        });

        _layout.setSwipeDownCallback(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {
                swipeDown();
                return null;
            }
        });
        
        // Update play button state
        updatePlayButtonState();
    }
    
    private void updateUIState()
    {
        // Current volume
        if (_volumeBar.getVisibility() == View.VISIBLE)
        {
            int currentVolume = AudioPlayer.getShared().getVolume();
            int progress = _volumeBar.getProgress();
            
            if (currentVolume != progress)
            {
                _volumeBar.setProgress(currentVolume);
                
                UIAnimations.animateImageTAP(getContext(), _volumeIcon);
            }
        }
        
        // Current position
        double duration = _player.getDurationMSec();
        double currentPosition = _player.getCurrentPositionMSec();
        double newPosition = (currentPosition / duration) * MEDIA_BAR_MAX_VALUE;
        _mediaBar.setProgress((int)newPosition);
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
                case FORWARDS_REPEAT:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_forwards_repeat);
                    break;
                case ONCE_FOREVER:
                    _buttonPlayOrder.setBackgroundResource(R.drawable.media_sort_repeat_forever);
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
        GeneralStorage.getShared().savePlayerState();
        GeneralStorage.getShared().savePlayerPlayHistoryState();
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
    
    private void swipeDown()
    {
        if (getActivity() != null)
        {
            getActivity().finish();
        }
    }
    
    private void startLooping()
    {
        if (getActivity() == null)
        {
            return;
        }
        
        _runnable = new Runnable() {
            @Override
            public void run() {
                loop();
            }
        };
        
        getActivity().runOnUiThread(_runnable);
    }
    
    private void loop()
    {
        FragmentActivity a = getActivity();
        
        if (a != null)
        {
            if (a.hasWindowFocus())
            {
                updateUIState();
            }
            
            _handler.postDelayed(_runnable, 200);
        }
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter) {
        _presenter = presenter;
    }

    @Override
    public void enableInteraction()
    {
        _buttonRecall.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonForward.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonPlay.setClickable(true);
        _buttonPlayOrder.setClickable(true);
    }

    @Override
    public void disableInteraction()
    {
        _buttonRecall.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonForward.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonPlay.setClickable(false);
        _buttonPlayOrder.setClickable(false);
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

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist, boolean sortTracks)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull AudioPlaylist playlist) 
    {
        updateMediaInfo(playlist.getPlayingTrack());
    }

    @Override
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs)
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

    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }
}
