package com.media.notabadplayer.View.Player;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Presenter.Albums.AlbumPresenter;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.Albums.AlbumFragment;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class QuickPlayerFragment extends Fragment implements BaseView, AudioPlayerObserver {
    AudioPlayer _player = AudioPlayer.getShared();
    
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
    private Button _mediaButtonPlaylist;
    
    public QuickPlayerFragment()
    {

    }

    public static QuickPlayerFragment newInstance()
    {
        return new QuickPlayerFragment();
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
        
        if (AudioPlayer.getShared().hasPlaylist())
        {
            updateMediaInfo(AudioPlayer.getShared().getPlaylist().getPlayingTrack());
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
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
        _mediaButtonPlaylist = root.findViewById(R.id.mediaButtonPlaylist);
        
        // Init UI
        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        final Fragment fragment = this;
        
        _header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerScreen();
            }
        });
        
        _mediaBar.setEnabled(false);
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonPlay);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.QUICK_PLAYER_PLAY_BUTTON);
            }
        });
    
        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonBack);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);
            }
        });
    
        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _buttonForward);
                KeyBinds.getShared().evaluateInput(fragment.getContext(), ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);
            }
        });
        
        _mediaButtonPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.animateButtonTAP(getContext(), _mediaButtonPlaylist);
                
                AudioPlaylist playlist = AudioPlayer.getShared().getPlaylist();
                
                if (playlist != null)
                {
                    openPlaylistScreen(playlist);
                }
            }
        });

        startLooping();
        
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
    
    private void openPlaylistScreen(AudioPlaylist playlist)
    {
        AlbumFragment f = AlbumFragment.newInstance();
        f.setPresenter(new AlbumPresenter(f, playlist));
        FragmentActivity a = getActivity();
        FragmentManager manager = a.getSupportFragmentManager();
        
        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }
        
        FragmentTransaction transaction = manager.beginTransaction().replace(R.id.mainLayout, f);
        transaction.addToBackStack(AlbumsFragment.class.getCanonicalName()).commit();
    }
    
    private void updateUIState()
    {
        int currentPosition = _player.getPlayer().getCurrentPosition() / 1000;
        _mediaBar.setProgress(currentPosition);
        _labelDurationCurrent.setText(AudioTrack.secondsToString(currentPosition));
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
        if (getActivity() != null)
        {
            if (getActivity().hasWindowFocus())
            {
                updateUIState();
            }
            
            _handler.postDelayed(_runnable, 200);
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter)
    {

    }

    @Override
    public void openAlbumScreen(@NonNull String albumID, @NonNull String albumArtist, @NonNull String albumTitle, @NonNull String albumCover) {
        
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
