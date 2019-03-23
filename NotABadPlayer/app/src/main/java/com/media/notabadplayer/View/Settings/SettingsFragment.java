package com.media.notabadplayer.View.Settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private Spinner _themePicker;
    private SettingsListAdapter _themeAdapter;

    private Spinner _albumSortingPicker;
    private SettingsListAdapter _albumSortingAdapter;

    private Spinner _trackSortingPicker;
    private SettingsListAdapter _trackSortingAdapter;

    private Spinner _showVolumeBarPicker;
    private SettingsListAdapter _showVolumeBarAdapter;
    
    private Spinner _keybindHome;
    private SettingsKeybindListAdapter _keybindHomeAdapter;
    private Spinner _keybindPlayerVU;
    private SettingsKeybindListAdapter _keybindPlayerVUAdapter;
    private Spinner _keybindPlayerVD;
    private SettingsKeybindListAdapter _keybindPlayerVDAdapter;
    private Spinner _keybindPlayerNext;
    private SettingsKeybindListAdapter _keybindPlayerNextAdapter;
    private Spinner _keybindPlayerPrev;
    private SettingsKeybindListAdapter _keybindPlayerPrevAdapter;
    private Spinner _keybindPlayerRecall;
    private SettingsKeybindListAdapter _keybindPlayerRecallAdapter;
    private Spinner _keybindQPlayerVU;
    private SettingsKeybindListAdapter _keybindQPlayerVUAdapter;
    private Spinner _keybindQPlayerVD;
    private SettingsKeybindListAdapter _keybindQPlayerVDAdapter;
    private Spinner _keybindQPlayerNext;
    private SettingsKeybindListAdapter _keybindQPlayerNextAdapter;
    private Spinner _keybindQPlayerPrev;
    private SettingsKeybindListAdapter _keybindQPlayerPrevAdapter;
    private Spinner _keybindEarphonesUnplug;
    private SettingsKeybindListAdapter _keybindEarphonesUnplugAdapter;
    
    private Button _resetSettingsButton;
    
    private AppSettings.AppTheme _appTheme;
    
    public SettingsFragment()
    {

    }

    public static SettingsFragment newInstance()
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
        _albumSortingPicker = root.findViewById(R.id.albumSortingPicker);
        _trackSortingPicker = root.findViewById(R.id.trackSortingPicker);
        _showVolumeBarPicker = root.findViewById(R.id.showVolumeBarPicker);
        _keybindHome = root.findViewById(R.id.keybindHome);
        _keybindPlayerVU = root.findViewById(R.id.keybindPlayerVU);
        _keybindPlayerVD = root.findViewById(R.id.keybindPlayerVD);
        _keybindPlayerNext = root.findViewById(R.id.keybindPlayerNext);
        _keybindPlayerPrev = root.findViewById(R.id.keybindPlayerPrev);
        _keybindPlayerRecall = root.findViewById(R.id.keybindPlayerPrevPlaylist);
        _keybindQPlayerVU = root.findViewById(R.id.keybindQPlayerVU);
        _keybindQPlayerVD = root.findViewById(R.id.keybindQPlayerVD);
        _keybindQPlayerNext = root.findViewById(R.id.keybindQPlayerNext);
        _keybindQPlayerPrev = root.findViewById(R.id.keybindQPlayerPrev);
        _keybindEarphonesUnplug = root.findViewById(R.id.keybindEarphonesUnplug);
        _resetSettingsButton = root.findViewById(R.id.resetButton);
        
        // Init UI
        initUI();
        
        // Select correct values
        selectProperValues();
        
        // App theme retrieve and store
        _appTheme = GeneralStorage.getShared().getAppThemeValue(getContext());
        
        return root;
    }
    
    private void initUI()
    {
        // Appearance pickers
        ArrayList<String> themeValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.AppTheme.values().length; e++)
        {
            themeValues.add(AppSettings.AppTheme.values()[e].name());
        }
        
        _themeAdapter = new SettingsListAdapter(getContext(), themeValues);
        _themePicker.setAdapter(_themeAdapter);
        _themePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.AppTheme selectedValue = AppSettings.AppTheme.values()[position];
                _presenter.onAppThemeChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayList<String> albumSortingValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.AlbumSorting.values().length; e++)
        {
            albumSortingValues.add(AppSettings.AlbumSorting.values()[e].name());
        }

        _albumSortingAdapter = new SettingsListAdapter(getContext(), albumSortingValues);
        _albumSortingPicker.setAdapter(_albumSortingAdapter);
        _albumSortingPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.AlbumSorting selectedValue = AppSettings.AlbumSorting.values()[position];
                AppSettings.TrackSorting trackSorting = GeneralStorage.getShared().getTrackSortingValue(getContext());
                _presenter.onAppSortingChange(selectedValue, trackSorting);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayList<String> trackSortingValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.TrackSorting.values().length; e++)
        {
            trackSortingValues.add(AppSettings.TrackSorting.values()[e].name());
        }
        
        _trackSortingAdapter = new SettingsListAdapter(getContext(), trackSortingValues);
        _trackSortingPicker.setAdapter(_trackSortingAdapter);
        _trackSortingPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.TrackSorting selectedValue = AppSettings.TrackSorting.values()[position];
                AppSettings.AlbumSorting albumSorting = GeneralStorage.getShared().getAlbumSortingValue(getContext());
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

        _showVolumeBarAdapter = new SettingsListAdapter(getContext(), showVolumeBarValues);
        _showVolumeBarPicker.setAdapter(_showVolumeBarAdapter);
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
        _keybindHomeAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindHome.setAdapter(_keybindHomeAdapter);
        setKeybindsOnItemSelectedListener(_keybindHome, ApplicationInput.HOME_BUTTON);
        
        _keybindPlayerVUAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerVU.setAdapter(_keybindPlayerVUAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerVU, ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
        
        _keybindPlayerVDAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerVD.setAdapter(_keybindPlayerVDAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerVD, ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
        
        _keybindPlayerNextAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerNext.setAdapter(_keybindPlayerNextAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerNext, ApplicationInput.PLAYER_NEXT_BUTTON);
        
        _keybindPlayerPrevAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerPrev.setAdapter(_keybindPlayerPrevAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerPrev, ApplicationInput.PLAYER_PREVIOUS_BUTTON);

        _keybindPlayerRecallAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindPlayerRecall.setAdapter(_keybindPlayerRecallAdapter);
        setKeybindsOnItemSelectedListener(_keybindPlayerRecall, ApplicationInput.PLAYER_RECALL);
        
        _keybindQPlayerVUAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerVU.setAdapter(_keybindQPlayerVUAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerVU, ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);

        _keybindQPlayerVDAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerVD.setAdapter(_keybindQPlayerVDAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerVD, ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);

        _keybindQPlayerNextAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerNext.setAdapter(_keybindQPlayerNextAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerNext, ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);

        _keybindQPlayerPrevAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindQPlayerPrev.setAdapter(_keybindQPlayerPrevAdapter);
        setKeybindsOnItemSelectedListener(_keybindQPlayerPrev, ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);

        _keybindEarphonesUnplugAdapter = new SettingsKeybindListAdapter(getContext());
        _keybindEarphonesUnplug.setAdapter(_keybindEarphonesUnplugAdapter);
        setKeybindsOnItemSelectedListener(_keybindEarphonesUnplug, ApplicationInput.EARPHONES_UNPLUG);
        
        // Reset
        _resetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        AppSettings.AppTheme value = GeneralStorage.getShared().getAppThemeValue(getContext());
        
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
        AppSettings.AlbumSorting value = GeneralStorage.getShared().getAlbumSortingValue(getContext());

        for (int e = 0; e < AppSettings.AlbumSorting.values().length; e++)
        {
            if (value == AppSettings.AlbumSorting.values()[e])
            {
                _albumSortingPicker.setSelection(e);
            }
        }
    }

    private void selectProperTrackSorting()
    {
        AppSettings.TrackSorting value = GeneralStorage.getShared().getTrackSortingValue(getContext());

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
        AppSettings.ShowVolumeBar value = GeneralStorage.getShared().getShowVolumeBarValue(getContext());

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
        ApplicationAction HOME_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.HOME_BUTTON);
        _keybindHome.setSelection(SettingsKeybindListAdapter.getCountForAction(HOME_BUTTON));
        
        ApplicationAction PLAYER_VOLUME_UP_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
        _keybindPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_UP_BUTTON));
        
        ApplicationAction PLAYER_VOLUME_DOWN_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
        _keybindPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_DOWN_BUTTON));
        
        ApplicationAction PLAYER_NEXT_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.PLAYER_NEXT_BUTTON);
        _keybindPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_NEXT_BUTTON));
        
        ApplicationAction PLAYER_PREVIOUS_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.PLAYER_PREVIOUS_BUTTON);
        _keybindPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_PREVIOUS_BUTTON));

        ApplicationAction PLAYER_RECALL = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.PLAYER_RECALL);
        _keybindPlayerRecall.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_RECALL));
        
        ApplicationAction QUICK_PLAYER_VOLUME_UP_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);
        _keybindQPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_UP_BUTTON));

        ApplicationAction QUICK_PLAYER_VOLUME_DOWN_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);
        _keybindQPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_DOWN_BUTTON));

        ApplicationAction QUICK_PLAYER_NEXT_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);
        _keybindQPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_NEXT_BUTTON));

        ApplicationAction QUICK_PLAYER_PREVIOUS_BUTTON = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);
        _keybindQPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_PREVIOUS_BUTTON));

        ApplicationAction EARPHONES_UNPLUG = GeneralStorage.getShared().getSettingsAction(getContext(), ApplicationInput.EARPHONES_UNPLUG);
        _keybindEarphonesUnplug.setSelection(SettingsKeybindListAdapter.getCountForAction(EARPHONES_UNPLUG));
    }
    
    private void showResetSettingsDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setMessage(R.string.settings_dialog_reset);
        builder1.setCancelable(true);
        
        builder1.setPositiveButton(
                R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _presenter.onAppSettingsReset();
                    }
                });
        
        builder1.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(@NonNull AudioAlbum album) {
        
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
    public void appSettingsReset()
    {
        selectProperValues();
    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        // Check if the fragment already have the correct app theme
        if (_appTheme == GeneralStorage.getShared().getAppThemeValue(getContext()))
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
        _appTheme = GeneralStorage.getShared().getAppThemeValue(getContext());
    }
    
    @Override
    public void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }
    
    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
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