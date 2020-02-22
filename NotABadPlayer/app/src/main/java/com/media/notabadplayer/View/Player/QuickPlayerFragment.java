package com.media.notabadplayer.View.Player;

import android.app.Activity;
import android.content.Intent;
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
import java.util.List;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.QuickPlayerObserver;
import com.media.notabadplayer.Audio.QuickPlayerService;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.StringUtilities;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class QuickPlayerFragment extends Fragment implements BaseView, QuickPlayerObserver {
    private static int MEDIA_BAR_MAX_VALUE = 100;
    
    Player _player = Player.getShared();
    
    private BasePresenter _presenter;
    private BaseView _rootView;
    
    private QuickPlayerLayout _layout;
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
    
    public static @NonNull QuickPlayerFragment newInstance(@NonNull BasePresenter presenter, @NonNull BaseView rootView)
    {
        QuickPlayerFragment fragment = new QuickPlayerFragment();
        fragment._presenter = presenter;
        fragment._rootView = rootView;
        return fragment;
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quick_player, container, false);
        
        // Setup UI
        _layout = root.findViewById(R.id.layout);
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

        QuickPlayerService.getShared().attach(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        
        if (Player.getShared().hasPlaylist())
        {
            updateMediaInfo(Player.getShared().getPlaylist().getPlayingTrack());
        }
        else
        {
            // Player may not be loaded yet, retrieve some dummy values from the general storage
            BaseAudioPlaylist playlist = GeneralStorage.getShared().retrievePlayerStateCurrentPlaylist();
            
            if (playlist != null)
            {
                updateMediaInfo(playlist.getPlayingTrack());
            }
        }

        updatePlayOrderButtonState();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        enableInteraction();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        disableInteraction();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        QuickPlayerService.getShared().detach(this);
    }
    
    private void initUI()
    {
        _mediaBar.setMax(MEDIA_BAR_MAX_VALUE);
        _mediaBar.setProgress(1); // Set to a non-zero value, to prevent weird UI drawable glitch
        _mediaBar.setProgress(0);
        _mediaBar.setEnabled(false);
        
        _buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonPlay.isClickable())
                {
                    return;
                }

                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_PLAY_BUTTON);

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonPlay);
            }
        });
    
        _buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonBack.isClickable())
                {
                    return;
                }

                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonBack);
            }
        });
    
        _buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonForward.isClickable())
                {
                    return;
                }

                _presenter.onPlayerButtonClick(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonForward);
            }
        });

        _buttonPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonPlaylist.isClickable())
                {
                    return;
                }

                _presenter.onOpenPlaylistButtonClick();

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonPlaylist);
            }
        });

        _buttonPlayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_buttonPlayOrder.isClickable())
                {
                    return;
                }

                _presenter.onPlayOrderButtonClick();

                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonPlayOrder);
            }
        });

        _layout.setSwipeUpCallback(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {
                swipeUp();
                return null;
            }
        });
        
        updatePlayButtonState();

        // Activate marquee
        activateTitleMarquee(true);
    }

    private void activateTitleMarquee(boolean value) {
        _labelTitle.setSelected(value);
    }

    private void updateSoftUIState()
    {
        if (!_player.hasPlaylist())
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

        // Seek bar update
        double duration = _player.getDurationMSec();
        double currentPosition = _player.getCurrentPositionMSec();
        double newPosition = (currentPosition / duration) * MEDIA_BAR_MAX_VALUE;
        
        _mediaBar.setProgress((int)newPosition);
        
        _labelDurationCurrent.setText(StringUtilities.secondsToString(currentPosition));
    }
    
    private void clearMediaInfo()
    {
        _imageCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_art_none));

        _labelTitle.setText(R.string.player_nothing_playing);
        _labelDurationTotal.setText(R.string.player_zero_timer);
    }

    private void updateMediaInfo(@Nullable BaseAudioTrack playingTrack)
    {
        if (playingTrack != null)
        {
            if (!playingTrack.getArtCover().isEmpty())
            {
                _imageCover.setImageURI(Uri.parse(Uri.decode(playingTrack.getArtCover())));
            }
            else
            {
                _imageCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_art_none));
            }

            if (!_labelTitle.getText().equals(playingTrack.getTitle())) {
                _labelTitle.setText(playingTrack.getTitle());
            }
            
            _labelDurationTotal.setText(playingTrack.getDuration());
            
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
        AudioPlayOrder order = Player.getShared().getPlayOrder();
        
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
    
    private void swipeUp()
    {
        _presenter.onOpenPlayer(Player.getShared().getPlaylist());
    }

    public void enableInteraction()
    {
        _buttonPlaylist.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonForward.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonPlay.setClickable(true);
        _buttonPlayOrder.setClickable(true);
    }

    public void disableInteraction()
    {
        _buttonPlaylist.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonForward.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonPlay.setClickable(false);
        _buttonPlayOrder.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options)
    {
        // Forward request to the application's root view
        if (_rootView != null)
        {
            _rootView.openPlaylistScreen(audioInfo, playlist, options);
        }
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<BaseAudioPlaylist> playlists)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String serializedData = Serializing.serializeObject(playlist);
        intent.putExtra("playlist", serializedData);
        startActivity(intent);

        // Transition animation
        Activity a = getActivity();
        
        if (a != null)
        {
            a.overridePendingTransition(R.anim.player_slide_up, R.anim.player_slide_down);
        }
    }

    @Override
    public void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {

    }

    @Override
    public void onPlayerPlay(@NonNull BaseAudioTrack current)
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

        if (!Player.getShared().hasPlaylist())
        {
            clearMediaInfo();
        }
    }
    
    @Override
    public void onPlayerPause(@NonNull BaseAudioTrack track)
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_play);
        updateMediaInfo(track);
    }
    
    @Override
    public void onPlayerResume(@NonNull BaseAudioTrack track)
    {
        _buttonPlay.setBackgroundResource(R.drawable.media_pause);
        updateMediaInfo(track);
    }

    @Override
    public void onPlayOrderChange(AudioPlayOrder order)
    {
        updatePlayOrderButtonState();
    }
    
    @Override
    public void updateTime(double currentTime, double totalDuration)
    {
        FragmentActivity a = getActivity();

        if (a != null)
        {
            if (a.hasWindowFocus())
            {
                updateSoftUIState();
            }
        }
    }

    @Override
    public void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage)
    {

    }
    
    @Override
    public void onResetAppSettings()
    {

    }

    @Override
    public void onAppThemeChanged(AppSettings.AppTheme appTheme)
    {

    }

    @Override
    public void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onDeviceLibraryChanged()
    {

    }

    @Override
    public void onFetchDataErrorEncountered(@NonNull Exception error)
    {

    }
    
    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
}
