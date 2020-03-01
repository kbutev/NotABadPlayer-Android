package com.media.notabadplayer.View.Lists;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import com.google.common.base.Function;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.BaseCreatePlaylistPresenter;
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.CreatePlaylistPresenter;
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.CreatePlaylistPresenterDelegate;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.AppThemeUtility;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Other.TrackListFavoritesChecker;
import com.media.notabadplayer.View.Other.TrackListHighlightedChecker;
import com.media.notabadplayer.View.Search.SearchFragment;

import java.util.List;

public class CreatePlaylistActivity extends AppCompatActivity implements CreatePlaylistPresenterDelegate, TrackListHighlightedChecker, TrackListFavoritesChecker
{
    private AudioLibrary _audioLibrary = AudioLibrary.getShared();
    
    private BaseCreatePlaylistPresenter _presenter;
    
    private Button _cancelButton;
    private Button _doneButton;
    private EditText _nameField;
    
    private ListView _addedTracksList;
    private RadioButton _switchToSearchTracks;
    private RadioButton _switchToSearchAlbumTracks;
    private ListView _albumsList;
    private @Nullable SearchFragment _searchFragment;

    private Function<Integer, Void> _onAlbumTrackClick;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        
        @Nullable BaseAudioPlaylist editPlaylist = loadPlaylistFromIntent();

        _presenter = new CreatePlaylistPresenter(this, _audioLibrary, this, editPlaylist);
        
        // Never restore this activity, navigate back to the main activity
        if (savedInstanceState != null)
        {
            finish();
            return;
        }
        
        // App theme
        AppThemeUtility.setTheme(this, GeneralStorage.getShared().getAppThemeValue());
        
        // Content
        setContentView(R.layout.activity_create_playlist);
        
        // UI
        _cancelButton = findViewById(R.id.buttonCancel);
        _doneButton = findViewById(R.id.buttonDone);
        _nameField = findViewById(R.id.playlistNameField);
        
        _addedTracksList = findViewById(R.id.addedTracksList);
        _switchToSearchTracks = findViewById(R.id.switchSearchTracks);
        _switchToSearchAlbumTracks = findViewById(R.id.switchSearchAlbumTracks);
        _albumsList = findViewById(R.id.albumsList);
        
        // Setup UI
        initUI();
        
        // Update name field
        if (editPlaylist != null)
        {
            _nameField.setText(editPlaylist.getName());
            _nameField.setEnabled(false);
            _doneButton.setText(getResources().getString(R.string.playlist_update));
        }

        // Presenter start
        _presenter.start();
    }
    
    private void initUI()
    {
        _cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        _doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _presenter.onSaveUserPlaylist();
            }
        });
        
        _nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                _presenter.onPlaylistNameChanged(s.toString());
            }
        });
        
        // On added track click
        _addedTracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _presenter.onAddedTrackClicked(position);
            }
        });
        
        // Switch
        _switchToSearchTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchTracksView();
            }
        });
        
        _switchToSearchAlbumTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumTracksView();
            }
        });
        
        if (_switchToSearchTracks.isChecked()) {
            showSearchTracksView();
        } else {
            showAlbumTracksView();
        }

        // On album track click
        _onAlbumTrackClick = new Function<Integer, Void>() {
            @Override
            public Void apply(@NullableDecl Integer index) {
                _presenter.onAlbumTrackClick(index != null ? index : 0);
                return null;
            }
        };

        // On album click
        _albumsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedAlbumPosition = _presenter.getSelectedAlbumIndex();
                if (selectedAlbumPosition != position)
                {
                    scrollToAlbumAndSelectAfterDelay(position);
                }
                else
                {
                    scrollToAlbumAfterDeselectAfterDelay();
                }
            }
        });
    }
    
    private @Nullable BaseAudioPlaylist loadPlaylistFromIntent()
    {
        String intentData = getIntent().getStringExtra("playlist");
        return (BaseAudioPlaylist) Serializing.deserializeObject(intentData);
    }

    @Override
    public void updateAddedTracks(@NonNull CreatePlaylistTracksAdapter adapter)
    {
        _addedTracksList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateAlbums(@NonNull CreatePlaylistAlbumsAdapter adapter)
    {
        _albumsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshSearchTracks()
    {
        if (_searchFragment == null)
        {
            return;
        }

        _searchFragment.forceResultsUIRefresh();
    }

    @Override
    public @NonNull Function<Integer, Void> onAlbumTrackClick()
    {
        return _onAlbumTrackClick;
    }

    @Override
    public void onSearchResultClick(int index)
    {
        refreshSearchTracks();
    }

    @Override
    public void showNoTracksDialog()
    {
        hideKeyboard();
        
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_no_tracks, R.string.ok, null);
    }

    @Override
    public void showInvalidNameDialog()
    {
        hideKeyboard();
        
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_invalid_name, R.string.ok, null);
    }

    @Override
    public void showNameTakenDialog()
    {
        hideKeyboard();
        
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_taken_name, R.string.ok, null);
    }

    @Override
    public void showUnknownErrorDialog()
    {
        hideKeyboard();
        
        AlertWindows.showAlert(this, 0, R.string.error_unknown, R.string.ok, null);
    }
    
    // BaseView

    @Override
    public void goBack()
    {
        hideKeyboard();

        onBackPressed();
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
        
    }

    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {
        if (_searchFragment == null)
        { 
            return;
        }
        
        _searchFragment.updateSearchQueryResults(searchQuery, filter, songs, searchState);
    }

    @Override
    public void openCreatePlaylistScreen(@Nullable BaseAudioPlaylist playlistToEdit)
    {

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
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar showVolumeBar)
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
    
    // TrackListHighlightedChecker, TrackListFavoritesChecker

    @Override
    public boolean shouldBeHighlighted(@NonNull BaseAudioTrack track)
    {
        return _presenter.isTrackAdded(track);
    }

    @Override
    public boolean isMarkedFavorite(@NonNull BaseAudioTrack track)
    {
        return GeneralStorage.getShared().favorites.isMarkedFavorite(track);
    }
    
    // Operations
    
    private void setVisibilityOfSearchList(boolean visible)
    {
        if (_searchFragment != null) {
            if (_searchFragment.getView() != null) {
                _searchFragment.getView().setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
    
    private void showSearchTracksView()
    {
        setVisibilityOfSearchList(true);
        _albumsList.setVisibility(View.GONE);
        
        if (_searchFragment == null)
        {
            createSearchFragment();
        }
    }
    
    private void createSearchFragment()
    {
        View view = findViewById(R.id.searchList);
        view.setVisibility(View.VISIBLE);
        
        SearchFragment fragment = SearchFragment.newInstance(_presenter, this, this, false);
        _searchFragment = fragment;

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.searchList, fragment).commit();
        
        _searchFragment.hideFiltersView();
    }

    private void showAlbumTracksView()
    {
        setVisibilityOfSearchList(false);
        _albumsList.setVisibility(View.VISIBLE);
    }

    private void scrollToAlbumAndSelectAfterDelay(final int position)
    {
        int selectedAlbumPosition = _presenter.getSelectedAlbumIndex();
        final boolean hasSelectedAlbum = selectedAlbumPosition >= 0;

        if (hasSelectedAlbum)
        {
            if (position < _albumsList.getCount())
            {
                _presenter.onCloseAlbum();
                _albumsList.setSelection(position);
            }
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (hasSelectedAlbum)
                    {
                        Thread.sleep(50);
                    }

                    // Update UI on main
                    Handler mainHandler = new Handler(getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            _presenter.onOpenAlbumAt(position);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                catch (Exception e)
                {

                }
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();
    }

    private void scrollToAlbumAfterDeselectAfterDelay()
    {
        int selectedAlbumPosition = _presenter.getSelectedAlbumIndex();
        
        if (selectedAlbumPosition < 0)
        {
            return;
        }

        _presenter.onCloseAlbum();
        _albumsList.setSelection(selectedAlbumPosition);
    }

    private void hideKeyboard()
    {
        View view = this.getCurrentFocus();

        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
