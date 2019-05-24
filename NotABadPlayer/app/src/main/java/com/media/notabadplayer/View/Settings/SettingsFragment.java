package com.media.notabadplayer.View.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private Spinner _themePicker;

    private Spinner _trackSortingPicker;

    private Spinner _showVolumeBarPicker;
    
    private Spinner _keybindPlayerVU;
    private Spinner _keybindPlayerVD;
    private Spinner _keybindPlayerNext;
    private Spinner _keybindPlayerPrev;
    private Spinner _keybindPlayerRecall;
    private Spinner _keybindPlayerSwipeLeft;
    private Spinner _keybindPlayerSwipeRight;
    private Spinner _keybindQPlayerVU;
    private Spinner _keybindQPlayerVD;
    private Spinner _keybindQPlayerNext;
    private Spinner _keybindQPlayerPrev;
    private Spinner _keybindEarphonesUnplug;
    private Spinner _keybindExternalPlay;
    
    private Button _resetSettingsButton;
    
    private AppSettings.AppTheme _appTheme;
    
    public SettingsFragment()
    {

    }

    public static @NonNull SettingsFragment newInstance()
    {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Setup UI
        _themePicker = root.findViewById(R.id.themePicker);
        _trackSortingPicker = root.findViewById(R.id.trackSortingPicker);
        _showVolumeBarPicker = root.findViewById(R.id.showVolumeBarPicker);
        _keybindPlayerVU = root.findViewById(R.id.keybindPlayerVU);
        _keybindPlayerVD = root.findViewById(R.id.keybindPlayerVD);
        _keybindPlayerNext = root.findViewById(R.id.keybindPlayerNext);
        _keybindPlayerPrev = root.findViewById(R.id.keybindPlayerPrev);
        _keybindPlayerRecall = root.findViewById(R.id.keybindPlayerPrevPlaylist);
        _keybindPlayerSwipeLeft = root.findViewById(R.id.keybindPlayerSwipeLeft);
        _keybindPlayerSwipeRight = root.findViewById(R.id.keybindPlayerSwipeRight);
        _keybindQPlayerVU = root.findViewById(R.id.keybindQPlayerVU);
        _keybindQPlayerVD = root.findViewById(R.id.keybindQPlayerVD);
        _keybindQPlayerNext = root.findViewById(R.id.keybindQPlayerNext);
        _keybindQPlayerPrev = root.findViewById(R.id.keybindQPlayerPrev);
        _keybindEarphonesUnplug = root.findViewById(R.id.keybindEarphonesUnplug);
        _keybindExternalPlay = root.findViewById(R.id.keybindExternalPlay);
        _resetSettingsButton = root.findViewById(R.id.resetButton);
        
        // Init UI
        initUI();
        
        // Select correct values
        selectProperValues();
        
        // App theme retrieve and store
        _appTheme = GeneralStorage.getShared().getAppThemeValue();
        
        return root;
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

    private void initUI()
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        // Appearance pickers
        ArrayList<String> themeValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.AppTheme.values().length; e++)
        {
            themeValues.add(AppSettings.AppTheme.values()[e].name());
        }

        SettingsListAdapter themeAdapter = new SettingsListAdapter(context, themeValues);
        _themePicker.setAdapter(themeAdapter);
        _themePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.AppTheme selectedValue = AppSettings.AppTheme.values()[position];
                _presenter.onAppThemeChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayList<String> trackSortingValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.TrackSorting.values().length; e++)
        {
            trackSortingValues.add(AppSettings.TrackSorting.values()[e].name());
        }

        SettingsListAdapter trackSortingAdapter = new SettingsListAdapter(getContext(), trackSortingValues);
        _trackSortingPicker.setAdapter(trackSortingAdapter);
        _trackSortingPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.TrackSorting selectedValue = AppSettings.TrackSorting.values()[position];
                AppSettings.AlbumSorting albumSorting = GeneralStorage.getShared().getAlbumSortingValue();
                _presenter.onAppSortingChange(albumSorting, selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayList<String> showVolumeBarValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.ShowVolumeBar.values().length; e++)
        {
            showVolumeBarValues.add(AppSettings.ShowVolumeBar.values()[e].name());
        }

        SettingsListAdapter showVolumeBarAdapter = new SettingsListAdapter(getContext(), showVolumeBarValues);
        _showVolumeBarPicker.setAdapter(showVolumeBarAdapter);
        _showVolumeBarPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.ShowVolumeBar selectedValue = AppSettings.ShowVolumeBar.values()[position];
                _presenter.onAppAppearanceChange(AppSettings.ShowStars.NO, selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Keybinds pickers
        SettingsKeybindListAdapter keybindPlayerVUAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerVU.setAdapter(keybindPlayerVUAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerVU, ApplicationInput.PLAYER_VOLUME_UP_BUTTON);

        SettingsKeybindListAdapter keybindPlayerVDAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerVD.setAdapter(keybindPlayerVDAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerVD, ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);

        SettingsKeybindListAdapter keybindPlayerNextAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerNext.setAdapter(keybindPlayerNextAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerNext, ApplicationInput.PLAYER_NEXT_BUTTON);

        SettingsKeybindListAdapter keybindPlayerPrevAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerPrev.setAdapter(keybindPlayerPrevAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerPrev, ApplicationInput.PLAYER_PREVIOUS_BUTTON);

        SettingsKeybindListAdapter keybindPlayerRecallAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerRecall.setAdapter(keybindPlayerRecallAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerRecall, ApplicationInput.PLAYER_RECALL);

        SettingsKeybindListAdapter keybindPlayerSwipeLeftAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerSwipeLeft.setAdapter(keybindPlayerSwipeLeftAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerSwipeLeft, ApplicationInput.PLAYER_SWIPE_LEFT);

        SettingsKeybindListAdapter keybindPlayerSwipeRightAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerSwipeRight.setAdapter(keybindPlayerSwipeRightAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerSwipeRight, ApplicationInput.PLAYER_SWIPE_RIGHT);

        SettingsKeybindListAdapter keybindQPlayerVUAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerVU.setAdapter(keybindQPlayerVUAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerVU, ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);

        SettingsKeybindListAdapter keybindQPlayerVDAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerVD.setAdapter(keybindQPlayerVDAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerVD, ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);

        SettingsKeybindListAdapter keybindQPlayerNextAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerNext.setAdapter(keybindQPlayerNextAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerNext, ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);

        SettingsKeybindListAdapter keybindQPlayerPrevAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerPrev.setAdapter(keybindQPlayerPrevAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerPrev, ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);

        SettingsKeybindListAdapter keybindEarphonesUnplugAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindEarphonesUnplug.setAdapter(keybindEarphonesUnplugAdapter);
        setKeybindsOnItemSelectedListener(_keybindEarphonesUnplug, ApplicationInput.EARPHONES_UNPLUG);
        
        SettingsKeybindListAdapter keybindEarphonesExternalPlay = new SettingsKeybindListAdapter(getContext());
        _keybindExternalPlay.setAdapter(keybindEarphonesExternalPlay);
        setKeybindsOnItemSelectedListener(_keybindExternalPlay, ApplicationInput.EXTERNAL_PLAY);
        
        // Reset
        _resetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_resetSettingsButton.isClickable())
                {
                    return;
                }

                showResetSettingsDialog();
            }
        });
    }
    
    private void setKeybindsOnItemSelectedListener(Spinner spinner, final ApplicationInput input)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ApplicationAction action = ApplicationAction.values()[position];
                _presenter.onKeybindChange(action, input);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void selectProperValues()
    {
        selectProperAppTheme();
        selectProperAlbumSorting();
        selectProperTrackSorting();
        selectProperShowVolumeBarValue();
        selectProperKeybinds();
    }
    
    private void selectProperAppTheme()
    {
        AppSettings.AppTheme value = GeneralStorage.getShared().getAppThemeValue();
        
        for (int e = 0; e < AppSettings.AppTheme.values().length; e++)
        {
            if (value == AppSettings.AppTheme.values()[e])
            {
                _themePicker.setSelection(e);
            }
        }
    }

    private void selectProperAlbumSorting()
    {
        
    }

    private void selectProperTrackSorting()
    {
        AppSettings.TrackSorting value = GeneralStorage.getShared().getTrackSortingValue();

        for (int e = 0; e < AppSettings.TrackSorting.values().length; e++)
        {
            if (value == AppSettings.TrackSorting.values()[e])
            {
                _trackSortingPicker.setSelection(e);
            }
        }
    }

    private void selectProperShowVolumeBarValue()
    {
        AppSettings.ShowVolumeBar value = GeneralStorage.getShared().getShowVolumeBarValue();

        for (int e = 0; e < AppSettings.ShowVolumeBar.values().length; e++)
        {
            if (value == AppSettings.ShowVolumeBar.values()[e])
            {
                _showVolumeBarPicker.setSelection(e);
            }
        }
    }
    
    private void selectProperKeybinds()
    {
        ApplicationAction PLAYER_VOLUME_UP_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
        _keybindPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_UP_BUTTON));
        
        ApplicationAction PLAYER_VOLUME_DOWN_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
        _keybindPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_DOWN_BUTTON));
        
        ApplicationAction PLAYER_NEXT_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_NEXT_BUTTON);
        _keybindPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_NEXT_BUTTON));
        
        ApplicationAction PLAYER_PREVIOUS_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_PREVIOUS_BUTTON);
        _keybindPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_PREVIOUS_BUTTON));

        ApplicationAction PLAYER_RECALL = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_RECALL);
        _keybindPlayerRecall.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_RECALL));

        ApplicationAction PLAYER_SWIPE_LEFT = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_SWIPE_LEFT);
        _keybindPlayerSwipeLeft.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_SWIPE_LEFT));

        ApplicationAction PLAYER_SWIPE_RIGHT = GeneralStorage.getShared().getSettingsAction(ApplicationInput.PLAYER_SWIPE_RIGHT);
        _keybindPlayerSwipeRight.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_SWIPE_RIGHT));
        
        ApplicationAction QUICK_PLAYER_VOLUME_UP_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);
        _keybindQPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_UP_BUTTON));

        ApplicationAction QUICK_PLAYER_VOLUME_DOWN_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);
        _keybindQPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_DOWN_BUTTON));

        ApplicationAction QUICK_PLAYER_NEXT_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);
        _keybindQPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_NEXT_BUTTON));

        ApplicationAction QUICK_PLAYER_PREVIOUS_BUTTON = GeneralStorage.getShared().getSettingsAction(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);
        _keybindQPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_PREVIOUS_BUTTON));

        ApplicationAction EARPHONES_UNPLUG = GeneralStorage.getShared().getSettingsAction(ApplicationInput.EARPHONES_UNPLUG);
        _keybindEarphonesUnplug.setSelection(SettingsKeybindListAdapter.getCountForAction(EARPHONES_UNPLUG));

        ApplicationAction EXTERNAL_PLAY = GeneralStorage.getShared().getSettingsAction(ApplicationInput.EXTERNAL_PLAY);
        _keybindExternalPlay.setSelection(SettingsKeybindListAdapter.getCountForAction(EXTERNAL_PLAY));
    }
    
    private void showResetSettingsDialog()
    {
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                _presenter.onAppSettingsReset();
            }
        };
        
        AlertWindows.showAlert(getContext(), 0, R.string.settings_dialog_reset, R.string.yes, action, R.string.no);
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void enableInteraction()
    {
        _resetSettingsButton.setClickable(true);
    }

    @Override
    public void disableInteraction()
    {
        _resetSettingsButton.setClickable(false);
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
        
    }
    
    @Override
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs)
    {
        
    }

    @Override
    public void appSettingsReset()
    {
        selectProperValues();
    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        FragmentActivity a = getActivity();

        if (a == null)
        {
            return;
        }

        // Check if the fragment already have the correct app theme
        if (_appTheme == GeneralStorage.getShared().getAppThemeValue())
        {
            return;
        }
        
        // Reload
        Fragment fragment = this;
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(fragment);
        ft.attach(fragment);
        ft.commit();
        
        // Retrieve and store again
        _appTheme = GeneralStorage.getShared().getAppThemeValue();
    }
    
    @Override
    public void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }
    
    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }
    
    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {
        
    }
    
    class SettingsListAdapter extends BaseAdapter
    {
        private ArrayList<String> _values;
        
        private Context _context;
        
        public SettingsListAdapter(@NonNull Context context, ArrayList<String> values)
        {
            this._values = values;
            this._context = context;
        }
        
        public int getCount()
        {
            return _values.size();
        }
        
        public Object getItem(int position)
        {
            return _values.get(position);
        }
        
        public long getItemId(int position)
        {
            return position;
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View listItem = LayoutInflater.from(_context).inflate(R.layout.item_settings_option, parent, false);

            TextView title = listItem.findViewById(R.id.title);
            String string = _values.get(position);
            string = string.replaceAll("_", " ");
            title.setText(string);

            return listItem;
        }
    }
}