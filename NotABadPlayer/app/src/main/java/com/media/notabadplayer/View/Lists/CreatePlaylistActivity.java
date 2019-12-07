package com.media.notabadplayer.View.Lists;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Function;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.AppThemeUtility;

public class CreatePlaylistActivity extends AppCompatActivity
{
    private List<AudioAlbum> _albums;
    
    private @Nullable BaseAudioPlaylist _playlist;
    private @NonNull ArrayList<BaseAudioTrack> _playlistTracks = new ArrayList<>();
    
    private Button _cancelButton;
    private Button _doneButton;
    private EditText _nameField;
    private ListView _addedTracksList;
    private CreatePlaylistTracksAdapter _addedTracksAdapter;
    private ListView _albumsList;
    private CreatePlaylistAlbumsAdapter _albumsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        
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
        
        // Model
        _albums = Player.getShared().getAudioInfo().getAlbums();
        
        // UI
        _cancelButton = findViewById(R.id.buttonCancel);
        _doneButton = findViewById(R.id.buttonDone);
        _nameField = findViewById(R.id.playlistNameField);
        _addedTracksList = findViewById(R.id.addedTracksList);
        _albumsList = findViewById(R.id.albumsList);
        
        // Setup UI
        initUI();
    }
    
    private void initUI()
    {
        _cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });

        _doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlaylist();
            }
        });
        
        Function<BaseAudioTrack, Void> onTrackClick = new Function<BaseAudioTrack, Void>() {
            @Override
            public Void apply(@NullableDecl BaseAudioTrack input) {
                if (input == null)
                {
                    return null;
                }

                if (!playlistTracksContain(input))
                {
                    addToPlaylist(input);
                    _albumsAdapter.selectTrack(input);
                }
                else
                {
                    removeFromPlaylist(input);
                    _albumsAdapter.deselectTrack(input);
                }
                
                updateAddedTracks();
                
                return null;
            }
        };

        _albumsAdapter = new CreatePlaylistAlbumsAdapter(this, Player.getShared().getAudioInfo(), _albums, onTrackClick);
        _albumsList.setAdapter(_albumsAdapter);
        
        _albumsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (_albumsAdapter.getSelectedAlbumPosition() != position)
                {
                    scrollToAlbumAndSelectAfterDelay(position);
                }
                else
                {
                    scrollToAlbumAfterDeselectAfterDelay();
                }
            }
        });

        _addedTracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaseAudioTrack track = _playlistTracks.get(position);
                removeFromPlaylist(track);
                _albumsAdapter.deselectTrack(track);
                updateAddedTracks();
            }
        });
        
        updateAddedTracks();
    }
    
    private void updateAddedTracks()
    {
        _addedTracksAdapter = new CreatePlaylistTracksAdapter(this, _playlistTracks);
        _addedTracksList.setAdapter(_addedTracksAdapter);
        
        _albumsAdapter.notifyDataSetChanged();
    }
    
    private boolean playlistTracksContain(@Nullable BaseAudioTrack track)
    {
        if (track == null)
        {
            return false;
        }

        return _playlistTracks.contains(track);
    }
    
    private void addToPlaylist(@NonNull BaseAudioTrack track)
    {
        if (_playlistTracks.contains(track))
        {
            return;
        }
        
        String name = _nameField.getText().toString();
        
        if (name.length() == 0)
        {
            name = "Playlist";
        }

        ArrayList<BaseAudioTrack> playlistTracks = new ArrayList<>(_playlistTracks);
        playlistTracks.add(track);

        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
        node.setName(name);
        node.setTracks(playlistTracks);

        // Try to build
        try {
            _playlist = node.build();
            _playlistTracks.add(track);
        } catch (Exception e) {
            Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to rebuild playlist");
        }
    }

    private void removeFromPlaylist(@NonNull BaseAudioTrack track)
    {
        String name = _nameField.getText().toString();

        if (name.length() == 0)
        {
            name = "Playlist";
        }

        ArrayList<BaseAudioTrack> playlistTracks = new ArrayList<>(_playlistTracks);
        playlistTracks.remove(track);

        if (!playlistTracks.isEmpty())
        {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(name);
            node.setTracks(playlistTracks);

            try {
                _playlist = node.build();
                _playlistTracks.remove(track);
            } catch (Exception e) {
                Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to rebuild playlist");
            }
        }
        else
        {
            _playlist = null;
            _playlistTracks.clear();
        }
    }
    
    private void savePlaylist()
    {
        if (_playlist == null || _playlistTracks.size() == 0)
        {
            hideKeyboard();
            showNoTracksDialog();
            return;
        }
        
        List<BaseAudioPlaylist> playlists = GeneralStorage.getShared().getUserPlaylists();

        String name = _nameField.getText().toString();
        
        // Must not have empty name
        if (name.isEmpty())
        {
            showInvalidNameDialog();
            return;
        }
        
        // Name must not be taken already
        for (int e = 0; e < playlists.size(); e++)
        {
            if (playlists.get(e).getName().equals(name))
            {
                showNameTakenDialog();
                return;
            }
        }
        
        // Successful save
        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
        node.setName(name);
        node.setTracks(_playlistTracks);

        // Try to build
        try {
            BaseAudioPlaylist playlist = node.build();

            playlists.add(playlist);
        } catch (Exception e) {
            Log.v(CreatePlaylistActivity.class.getCanonicalName(), "Failed to save playlist to storage");
            showUnknownErrorDialog();
            return;
        }

        GeneralStorage.getShared().saveUserPlaylists(playlists);
        
        quit();
    }
    
    private void scrollToAlbumAndSelectAfterDelay(final int position)
    {
        final boolean hasSelectedAlbum = _albumsAdapter.getSelectedAlbumPosition() >= 0;
        
        if (hasSelectedAlbum)
        {
            if (position < _albumsList.getCount())
            {
                _albumsAdapter.deselectAlbum();
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
                            _albumsAdapter.selectAlbum(position);

                            _albumsAdapter.notifyDataSetChanged();
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
        if (_albumsAdapter.getSelectedAlbumPosition() < 0)
        {
            return;
        }
        
        final int selectedAlbum = _albumsAdapter.getSelectedAlbumPosition();

        _albumsAdapter.deselectAlbum();
        _albumsList.setSelection(selectedAlbum);
    }

    private void showNoTracksDialog()
    {
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_no_tracks, R.string.ok, null);
    }
    
    private void showInvalidNameDialog()
    {
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_invalid_name, R.string.ok, null);
    }

    private void showNameTakenDialog()
    {
        AlertWindows.showAlert(this, 0, R.string.playlist_dialog_taken_name, R.string.ok, null);
    }

    private void showUnknownErrorDialog()
    {
        AlertWindows.showAlert(this, 0, R.string.error_unknown, R.string.ok, null);
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

    private void quit()
    {
        hideKeyboard();

        onBackPressed();
    }
}
