package com.media.notabadplayer.View.Player;

import java.util.ArrayList;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.LooperService;
import com.media.notabadplayer.Utilities.LooperClient;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerFragment extends Fragment implements BaseView, AudioPlayerObserver, LooperClient
{
    private static int MEDIA_BAR_MAX_VALUE = 100;
    private static int VOLUME_BAR_MAX_VALUE = 100;
    
    private BasePresenter _presenter;

    AudioPlayer _player = AudioPlayer.getShared();
    
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
    
    public static @NonNull PlayerFragment newInstance(@NonNull BasePresenter presenter)
    {
        PlayerFragment fragment = new PlayerFragment();
        fragment._presenter = presenter;
        return fragment;
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

        stopLooping();
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

                UIAnimations.getShared().imageAnimations.animateTap(getContext(), _volumeIcon);
                
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

                // Reset the motion state of the layout, to prevent swipe left or right
                _layout.resetMotionState();
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

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonRecall);
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

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonPlay);
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

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonBack);
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

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonForward);
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

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonPlayOrder);
                _presenter.onPlayOrderButtonClick();
                
                // Save current audio state
                saveCurrentAudioState();
            }
        });

        _layout.setSwipeLeftCallback(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {
                swipeLeft();
                return null;
            }
        });

        _layout.setSwipeRightCallback(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {
                swipeRight();
                return null;
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
        
        // Update play order button state
        updatePlayOrderButtonState();
    }

    public void updateUIState(@NonNull AudioTrack track)
    {
        updateMediaInfo(track);
        updateSoftUIState();
    }

    public void updateSoftUIState()
    {
        // Seek bar update
        double duration = _player.getDurationMSec();
        double currentPosition = _player.getCurrentPositionMSec();
        double newMediaBarPosition = (currentPosition / duration) * MEDIA_BAR_MAX_VALUE;
        
        if (_mediaBar.getProgress() != (int)newMediaBarPosition)
        {
            _mediaBar.setProgress((int)newMediaBarPosition);
        }
        
        _labelDurationCurrent.setText(AudioTrack.secondsToString(currentPosition));
        
        // Volume bar update
        if (_volumeBar.getVisibility() == View.VISIBLE)
        {
            int currentVolume = AudioPlayer.getShared().getVolume();
            int progress = _volumeBar.getProgress();

            if (currentVolume != progress)
            {
                _volumeBar.setProgress(currentVolume);

                UIAnimations.getShared().imageAnimations.animateTap(getContext(), _volumeIcon);
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

    private void updatePlayOrderButtonState()
    {
        AudioPlayOrder order = AudioPlayer.getShared().getPlayOrder();
        
        switch (order)
        {
            case FORWARDS:
                _buttonPlayOrder.setBackgroundResource(R.drawable.media_order_forwards);
                break;
            case FORWARDS_REPEAT:
                _buttonPlayOrder.setBackgroundResource(R.drawable.media_order_forwards_repeat);
                break;
            case ONCE_FOREVER:
                _buttonPlayOrder.setBackgroundResource(R.drawable.media_order_repeat_forever);
                break;
            case SHUFFLE:
                _buttonPlayOrder.setBackgroundResource(R.drawable.media_order_shuffle);
                break;
            default:
                _buttonPlayOrder.setBackgroundResource(R.drawable.media_order_forwards);
                break;
        }
    }

    private void swipeLeft()
    {
        _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_SWIPE_LEFT);
    }

    private void swipeRight()
    {
        _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_SWIPE_RIGHT);
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
        LooperService.getShared().subscribe(this);
    }

    private void stopLooping()
    {
        LooperService.getShared().unsubscribe(this);
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
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums) 
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull AudioPlaylist playlist) 
    {
        
    }

    @Override
    public void updatePlayerScreen(@NonNull AudioPlaylist playlist)
    {
        updateMediaInfo(playlist.getPlayingTrack());
    }
    
    @Override
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs, @Nullable String searchTip)
    {

    }
    
    @Override
    public void onPlayerPlay(AudioTrack current)
    {
        updateMediaInfo(current);
        updatePlayButtonState();
    }
    
    @Override
    public void onPlayerFinish()
    {
        updatePlayButtonState();
    }
    
    @Override
    public void onPlayerStop()
    {
        updatePlayButtonState();
    }
    
    @Override
    public void onPlayerPause(AudioTrack track)
    {

        updatePlayButtonState();
    }
    
    @Override
    public void onPlayerResume(AudioTrack track)
    {
        updatePlayButtonState();
    }

    @Override
    public void onPlayOrderChange(AudioPlayOrder order)
    {
        updatePlayOrderButtonState();
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
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FragmentActivity a = getActivity();
                
                if (a != null)
                {
                    a.finish();
                }
            }
        };
        
        AlertWindows.showAlert(getContext(), R.string.error, R.string.error_invalid_file_play, R.string.ok, action);
    }

    @Override
    public void loop()
    {
        updateSoftUIState();
    }
}
