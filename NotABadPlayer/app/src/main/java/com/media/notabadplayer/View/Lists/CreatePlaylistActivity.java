package com.media.notabadplayer.View.Lists;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import java.util.ArrayList;
import com.google.common.base.Function;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AppThemeSetter;

public class CreatePlaylistActivity extends AppCompatActivity
{
    private ArrayList<AudioAlbum> _albums;
    
    private @Nullable AudioPlaylist _playlist;
    private @NonNull ArrayList<AudioTrack> _playlistTracks = new ArrayList<>();
    
    private Button _cancelButton;
    private Button _doneButton;
    private EditText _nameField;
    private ListView _addedTracksList;
    private CreatePlaylistTracksAdapter _addedTracksAdapter;
    private ListView _albumsList;
    private CreatePlaylistAlbumsAdapter _albumsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // App theme
        AppThemeSetter.setTheme(this, GeneralStorage.getShared().getAppThemeValue(this));
        
        // Content
        setContentView(R.layout.activity_create_playlist);
        
        // Model
        _albums = AudioPlayer.getShared().getAudioInfo().getAlbums();
        
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
        
        Function<AudioTrack, Boolean> onTrackClick = new Function<AudioTrack, Boolean>() {
            @Override
            public Boolean apply(@NullableDecl AudioTrack input) {
                boolean result = addToPlaylist(input);
                
                if (result)
                {
                    _albumsAdapter.selectTrack(input);
                    updateAddedTracks();
                }
                
                return result;
            }
        };

        _albumsAdapter = new CreatePlaylistAlbumsAdapter(this, AudioPlayer.getShared().getAudioInfo(), _albums, onTrackClick);
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
                AudioTrack track = _playlistTracks.get(position);
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
    
    private boolean addToPlaylist(AudioTrack track)
    {
        if (_playlistTracks.contains(track))
        {
            return false;
        }
        
        String name = _nameField.getText().toString();
        
        if (name.length() == 0)
        {
            name = "Playlist";
        }

        _playlistTracks.add(track);

        _playlist = new AudioPlaylist(name, _playlistTracks);
        
        return true;
    }

    private void removeFromPlaylist(AudioTrack track)
    {
        String name = _nameField.getText().toString();

        if (name.length() == 0)
        {
            name = "Playlist";
        }

        _playlistTracks.remove(track);

        if (!_playlistTracks.isEmpty())
        {
            _playlist = new AudioPlaylist(name, _playlistTracks);
        }
        else
        {
            _playlist = null;
        }
    }
    
    private void savePlaylist()
    {
        if (_playlist == null)
        {
            showNoTracksDialog();
            return;
        }
        
        ArrayList<AudioPlaylist> playlists = GeneralStorage.getShared().getPlaylists(this);
        
        if (playlists == null)
        {
            playlists = new ArrayList<>();
        }
        
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
        playlists.add(new AudioPlaylist(name, _playlistTracks));

        GeneralStorage.getShared().savePlaylists(this, playlists);
        
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.playlist_dialog_no_tracks);
        builder1.setCancelable(true);

        builder1.setPositiveButton(R.string.ok, null);

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    
    private void showInvalidNameDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.playlist_dialog_invalid_name);
        builder1.setCancelable(true);

        builder1.setPositiveButton(R.string.ok, null);

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void showNameTakenDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.playlist_dialog_taken_name);
        builder1.setCancelable(true);

        builder1.setPositiveButton(R.string.ok, null);

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void quit()
    {
        onBackPressed();
    }
}
