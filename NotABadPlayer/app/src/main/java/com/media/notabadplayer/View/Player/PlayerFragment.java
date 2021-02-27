package com.media.notabadplayer.View.Player;

import java.util.List;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.QuickPlayerObserver;
import com.media.notabadplayer.Audio.QuickPlayerService;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.ArtImageFetcher;
import com.media.notabadplayer.Utilities.StringUtilities;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerFragment extends Fragment implements BaseView, QuickPlayerObserver
{
    private static int MEDIA_BAR_MAX_VALUE = 100;
    private static int VOLUME_BAR_MAX_VALUE = 100;
    
    private BasePresenter _presenter;

    @NonNull
    Player _player = Player.getShared();
    
    private boolean _playerIsMuted = false;
    
    private PlayerLayout _layout;
    private ImageView _imageCover;
    private SeekBar _volumeBar;
    private ImageView _volumeIcon;
    private TextView _labelTitle;
    private TextView _labelAlbum;
    private TextView _labelArtist;
    private View _markFavoriteButton;
    private ImageView _markFavoriteImage;
    private SeekBar _mediaBar;
    private TextView _labelDurationCurrent;
    private TextView _labelDurationTotal;
    private Button _buttonRecall;
    private Button _buttonBack;
    private Button _buttonPlay;
    private Button _buttonForward;
    private Button _buttonPlayOrder;
    private Button _buttonExit;

    private ArtImageFetcher _artImageFetcher;

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
        _markFavoriteButton = root.findViewById(R.id.markFavoriteButton);
        _markFavoriteImage = root.findViewById(R.id.markFavoriteImage);
        _mediaBar = root.findViewById(R.id.mediaBar);
        _labelDurationCurrent = root.findViewById(R.id.durationCurrent);
        _labelDurationTotal = root.findViewById(R.id.durationTotal);
        _buttonRecall = root.findViewById(R.id.mediaButtonRecall);
        _buttonBack = root.findViewById(R.id.mediaButtonBack);
        _buttonPlay = root.findViewById(R.id.mediaButtonPlay);
        _buttonForward = root.findViewById(R.id.mediaButtonForward);
        _buttonPlayOrder = root.findViewById(R.id.mediaButtonPlayOrder);
        _buttonExit = root.findViewById(R.id.exitPlayer);
        _artImageFetcher = new ArtImageFetcher(inflater.getContext());
        
        AppSettings.ShowVolumeBar showBarState = GeneralStorage.getShared().getShowVolumeBarValue();
        boolean isLeftVolumeOn = showBarState == AppSettings.ShowVolumeBar.LEFT_SIDE;
        boolean isRightVolumeOn = showBarState == AppSettings.ShowVolumeBar.RIGHT_SIDE;

        if (showBarState == AppSettings.ShowVolumeBar.NO)
        {
            _volumeBar = root.findViewById(R.id.volumeBarLeftSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarLeftIcon);
        }
        else if (isLeftVolumeOn)
        {
            _volumeBar = root.findViewById(R.id.volumeBarLeftSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarLeftIcon);
            hideRightSideControls(root);
        }
        else if (isRightVolumeOn) {
            _volumeBar = root.findViewById(R.id.volumeBarRightSeek);
            _volumeIcon = root.findViewById(R.id.volumeBarRightIcon);
            hideLeftSideControls(root);
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

        QuickPlayerService.getShared().attach(this);
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        QuickPlayerService.getShared().detach(this);
    }
    
    private void initUI()
    {
        Context context = getContext();
        
        if (context == null)
        {
            return;
        }
        
        _volumeBar.setMax(VOLUME_BAR_MAX_VALUE);
        _volumeBar.setProgress(Player.getShared().getVolume());
        _volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                {
                    return;
                }

                UIAnimations.getShared().imageAnimations.animateTap(getContext(), _volumeIcon);
                
                _volumeBar.setProgress(progress);
                
                // Reset the motion state of the layout, to prevent swipe down
                _layout.resetMotionState();
                
                // Alert presenter
                _presenter.onPlayerVolumeSet(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        _volumeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_volumeIcon.isClickable())
                {
                    return;
                }

                UIAnimations.getShared().imageAnimations.animateTap(getContext(), _volumeIcon);
                _presenter.onPlayerButtonClick(ApplicationInput.PLAYER_VOLUME);
            }
        });

        _playerIsMuted = _player.isMuted();
        
        if (!_playerIsMuted)
        {
            _volumeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.volume_icon));
        }
        else
        {
            _volumeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.volume_icon_muted));
        }

        _markFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = _presenter.onMarkOrUnmarkContextTrackFavorite();
                markFavorite(result);
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
            }
        });

        _buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIAnimations.getShared().buttonAnimations.animateTap(getContext(), _buttonExit);

                exit();
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
        
        // Activate marquee
        activateTitleMarquee(true);
    }

    private void activateTitleMarquee(boolean value) {
        _labelTitle.setSelected(value);
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
        
        _labelDurationCurrent.setText(StringUtilities.secondsToString(currentPosition));
        
        // Volume bar & image update
        if (_volumeBar.getVisibility() == View.VISIBLE)
        {
            int currentVolume = _player.getVolume();
            int progress = _volumeBar.getProgress();

            if (currentVolume != progress)
            {
                _volumeBar.setProgress(currentVolume);

                UIAnimations.getShared().imageAnimations.animateTap(getContext(), _volumeIcon);
            }

            if (_player.isMuted() != _playerIsMuted)
            {
                _playerIsMuted = !_playerIsMuted;

                Context context = getContext();
                
                if (context != null)
                {
                    Resources resources = context.getResources();

                    if (!_playerIsMuted)
                    {
                        _volumeIcon.setImageDrawable(resources.getDrawable(R.drawable.volume_icon));
                    }
                    else
                    {
                        _volumeIcon.setImageDrawable(resources.getDrawable(R.drawable.volume_icon_muted));
                    }
                }
            }
        }
    }
    
    private void updateMediaInfo(@NonNull BaseAudioTrack playingTrack, boolean isFavorite)
    {
        Bitmap artImage = _artImageFetcher.fetch(playingTrack.getArtCover());

        if (artImage != null)
        {
            _imageCover.setImageBitmap(artImage);
        }
        else
        {
            _imageCover.setImageDrawable(getResources().getDrawable(R.drawable.cover_art_none));
        }

        _labelTitle.setText(playingTrack.getTitle());
        _labelAlbum.setText(playingTrack.getAlbumTitle());
        _labelArtist.setText(playingTrack.getArtist());
        _labelDurationTotal.setText(playingTrack.getDuration());

        updatePlayButtonState();
        markFavorite(isFavorite);
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

    public void markFavorite(boolean favorite) {
        if (favorite)
        {
            _markFavoriteImage.setImageDrawable(getResources().getDrawable(R.drawable.shiny_star));
        } 
        else 
        {
            _markFavoriteImage.setImageDrawable(getResources().getDrawable(R.drawable.dark_star));
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
        exit();
    }

    private void exit()
    {
        if (getActivity() != null)
        {
            getActivity().finish();
        }
    }

    public void enableInteraction()
    {
        _buttonRecall.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonForward.setClickable(true);
        _buttonBack.setClickable(true);
        _buttonPlay.setClickable(true);
        _buttonPlayOrder.setClickable(true);
        _volumeIcon.setClickable(true);
    }

    public void disableInteraction()
    {
        _buttonRecall.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonForward.setClickable(false);
        _buttonBack.setClickable(false);
        _buttonPlay.setClickable(false);
        _buttonPlayOrder.setClickable(false);
        _volumeIcon.setClickable(false);
    }
    
    private boolean isStorageMarkedFavorite(@NonNull BaseAudioTrack track) {
        return GeneralStorage.getShared().favorites.isMarkedFavorite(track);
    }

    private void hideLeftSideControls(@NonNull View root) {
        root.findViewById(R.id.volumeBarLeft).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.volumeBarRight).setVisibility(View.VISIBLE);
    }

    private void hideRightSideControls(@NonNull View root) {
        root.findViewById(R.id.volumeBarLeft).setVisibility(View.VISIBLE);
        root.findViewById(R.id.volumeBarRight).setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options)
    {

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
        
    }

    @Override
    public void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {
        BaseAudioTrack playing = playlist.getPlayingTrack();
        
        updateMediaInfo(playing, isStorageMarkedFavorite(playing));
    }
    
    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {

    }

    @Override
    public void openCreatePlaylistScreen(@Nullable BaseAudioPlaylist playlistToEdit)
    {

    }
    
    @Override
    public void onPlayerPlay(@NonNull BaseAudioTrack current)
    {
        updateMediaInfo(current, isStorageMarkedFavorite(current));
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
    public void onPlayerPause(@NonNull BaseAudioTrack track)
    {
        updatePlayButtonState();
    }
    
    @Override
    public void onPlayerResume(@NonNull BaseAudioTrack track)
    {
        updatePlayButtonState();
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
        Context context = getContext();
        
        if (context == null) {
            return;
        }
        
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FragmentActivity a = getActivity();
                
                if (a != null)
                {
                    a.finish();
                }
            }
        };
        
        AlertWindows.showAlert(context, R.string.error, R.string.error_invalid_file_play, R.string.ok, action);
    }
}
