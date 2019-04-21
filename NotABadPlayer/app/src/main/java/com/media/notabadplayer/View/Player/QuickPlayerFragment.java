package com.media.notabadplayer.View.Player;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class QuickPlayerFragment extends Fragment implements BaseView, AudioPlayerObserver {
    private static int MEDIA_BAR_MAX_VALUE = 100;
    
    AudioPlayer _player = AudioPlayer.getShared();
    
    private BasePresenter _presenter;
    
    private Runnable _runnable;
    private Handler _handler = new Handler();
    
    private View _header;
    private ImageView _imageCover;
    private TextView _labelTitle;
    private SeekBar _mediaBar;
    private TextView _labelDurationCurrent;
    private TextView _labelDurationTotal;
    private Button _buttonBack;
    private Button _buttonPlay;
    private Button _buttonForward;
    private Button _buttonPlaylist;
    private Button _buttonPlayOrder;
    
    public QuickPlayerFragment()
    {

    }

    public static @NonNull QuickPlayerFragment newInstance()
    {
        return new QuickPlayerFragment();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quick_player, container, false);
        
        // Setup UI
        _header = root.findViewById(R.id.header);
        _imageCover = root.findViewById(R.id.cover);
        _labelTitle = root.findViewById(R.id.labelTitle);
        _mediaBar = root.findViewById(R.id.mediaBar);
        _labelDurationCurrent = root.findViewById(R.id.durationCurrent);
        _labelDurationTotal = root.findViewById(R.id.durationTotal);
        _buttonBack = root.findViewById(R.id.mediaButtonBack);
        _buttonPlay = root.findViewById(R.id.mediaButtonPlay);
        _buttonForward = root.findViewById(R.id.mediaButtonForward);
        _buttonPlaylist = root.findViewById(R.id.mediaButtonPlaylist);
        _buttonPlayOrder = root.findViewById(R.id.mediaButtonPlayOrder);
        
        // Init UI
        initUI();
        
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        _presenter.start();

        _player.attachObserver(this);

        startLooping();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (AudioPlayer.getShared().hasPlaylist())
        {
            updateMediaInfo(AudioPlayer.getShared().getPlaylist().getPlayingTrack());
        }
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _player.detachObserver(this);
    }
    
    private void initUI()
    {
        _header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerScreen();
            }
        });
        
        _mediaBar.setMax(MEDIA_BAR_MAX_VALUE);
        _mediaBar.setProgress(1); // Set to a non-zero value, to prevent weird UI drawable glitch
        _mediaBar.setEnabled(false);
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonPlay);
                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_PLAY_BUTTON, getContext());
            }
        });
    
        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonBack);
                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON, getContext());
            }
        });
    
        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonForward);
                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON, getContext());
            }
        });

        _buttonPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonPlaylist);
                _presenter.onOpenPlaylistButtonClick(getContext());
            }
        });

        _buttonPlayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonPlayOrder);
                _presenter.onPlayOrderButtonClick(getContext());

                // Save current audio state
                saveCurrentAudioState();
            }
        });
        
        updatePlayButtonState();
    }
    
    private void openPlayerScreen()
    {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("playlist", Serializing.serializeObject(AudioPlayer.getShared().getPlaylist()));
        startActivity(intent);
        
        // Transition animation
        Activity a = getActivity();
        
        if (a != null)
        {
            a.overridePendingTransition(R.anim.player_slide_up, R.anim.player_slide_down);
        }
    }
    
    private void updateUIState()
    {
        if (_player.getPlaylist() == null)
        {
            return;
        }
        
        if (getActivity() == null)
        {
            return;
        }
        
        if (!getActivity().hasWindowFocus())
        {
            return;
        }

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
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
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
        // Reload
        Fragment fragment = this;
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(fragment);
        ft.attach(fragment);
        ft.commit();
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
