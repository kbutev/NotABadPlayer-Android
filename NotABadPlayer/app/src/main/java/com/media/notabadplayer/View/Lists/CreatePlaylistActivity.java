package com.media.notabadplayer.View.Lists;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.BaseCreatePlaylistPresenter;
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.CreatePlaylistPresenter;
import com.media.notabadplayer.Presenter.CreatePlaylistPresenter.CreatePlaylistPresenterDelegate;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.AppThemeUtility;

public class CreatePlaylistActivity extends AppCompatActivity implements CreatePlaylistPresenterDelegate
{
    private @NonNull BaseCreatePlaylistPresenter _presenter;
    
    private Button _cancelButton;
    private Button _doneButton;
    private EditText _nameField;
    private ListView _addedTracksList;
    private ListView _albumsList;

    Function<Integer, Void> onAlbumTrackClick;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        _presenter = new CreatePlaylistPresenter(this, this);
        
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
        _albumsList = findViewById(R.id.albumsList);
        
        // Setup UI
        initUI();

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

        // On album track click
        onAlbumTrackClick = new Function<Integer, Void>() {
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

        // On added track click
        _addedTracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _presenter.onAddedTrackClicked(position);
            }
        });
    }

    @Override
    public void updateAddedTracksDataSource(@NonNull CreatePlaylistTracksAdapter adapter)
    {
        _addedTracksList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateAlbumsDataSource(@NonNull CreatePlaylistAlbumsAdapter adapter)
    {
        _albumsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public @NonNull Function<Integer, Void> onAlbumTrackClick()
    {
        return onAlbumTrackClick;
    }
    
    @Override
    public void goBack()
    {
        hideKeyboard();

        onBackPressed();
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
