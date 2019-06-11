package com.media.notabadplayer.View.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.View.BaseView;

public class SettingsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    private BaseView _rootView;
    
    private View _layout;
    
    private Spinner _themePicker;
    private Spinner _trackSortingPicker;
    private Spinner _showVolumeBarPicker;
    private Spinner _openPlayerOnPlayPicker;
    
    private Spinner _keybindPlayerVU;
    private Spinner _keybindPlayerVD;
    private Spinner _keybindPlayerNext;
    private Spinner _keybindPlayerPrev;
    private Spinner _keybindPlayerRecall;
    private Spinner _keybindPlayerSwipeLeft;
    private Spinner _keybindPlayerSwipeRight;
    private Spinner _keybindPlayerVolume;
    private Spinner _keybindQPlayerVU;
    private Spinner _keybindQPlayerVD;
    private Spinner _keybindQPlayerNext;
    private Spinner _keybindQPlayerPrev;
    private Spinner _keybindEarphonesUnplug;
    private Spinner _keybindExternalPlay;
    
    private Button _resetSettingsButton;

    private ProgressBar _progressIndicator;
    
    private AppSettings.AppTheme _currentAppTheme;
    
    public SettingsFragment()
    {

    }

    public static @NonNull SettingsFragment newInstance(@NonNull BasePresenter presenter, @NonNull BaseView rootView)
    {
        SettingsFragment fragment = new SettingsFragment();
        fragment._presenter = presenter;
        fragment._rootView = rootView;
        return fragment;
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
        _layout = root.findViewById(R.id.settingsLayout);
        
        _themePicker = root.findViewById(R.id.themePicker);
        _trackSortingPicker = root.findViewById(R.id.trackSortingPicker);
        _showVolumeBarPicker = root.findViewById(R.id.showVolumeBarPicker);
        _openPlayerOnPlayPicker = root.findViewById(R.id.openPlayerOnPlayPicker);
        
        _keybindPlayerVU = root.findViewById(R.id.keybindPlayerVU);
        _keybindPlayerVD = root.findViewById(R.id.keybindPlayerVD);
        _keybindPlayerNext = root.findViewById(R.id.keybindPlayerNext);
        _keybindPlayerPrev = root.findViewById(R.id.keybindPlayerPrev);
        _keybindPlayerRecall = root.findViewById(R.id.keybindPlayerPrevPlaylist);
        _keybindPlayerSwipeLeft = root.findViewById(R.id.keybindPlayerSwipeLeft);
        _keybindPlayerSwipeRight = root.findViewById(R.id.keybindPlayerSwipeRight);
        _keybindPlayerVolume = root.findViewById(R.id.keybindPlayerVolume);
        _keybindQPlayerVU = root.findViewById(R.id.keybindQPlayerVU);
        _keybindQPlayerVD = root.findViewById(R.id.keybindQPlayerVD);
        _keybindQPlayerNext = root.findViewById(R.id.keybindQPlayerNext);
        _keybindQPlayerPrev = root.findViewById(R.id.keybindQPlayerPrev);
        _keybindEarphonesUnplug = root.findViewById(R.id.keybindEarphonesUnplug);
        _keybindExternalPlay = root.findViewById(R.id.keybindExternalPlay);
        _resetSettingsButton = root.findViewById(R.id.resetButton);
        
        _progressIndicator = root.findViewById(R.id.progressIndicator);
        
        // UI is initialized by onAppSettingsLoad() which is called by the presenter
        
        // App theme retrieve and store
        _currentAppTheme = GeneralStorage.getShared().getAppThemeValue();
        
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        _presenter.start();
        
        showProgressIndicator();
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

        // Appearance pickers - setup adapters
        ArrayList<String> themeValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.AppTheme.values().length; e++)
        {
            themeValues.add(AppSettings.AppTheme.values()[e].name());
        }

        SettingsListAdapter themeAdapter = new SettingsListAdapter(context, themeValues);
        _themePicker.setAdapter(themeAdapter);

        ArrayList<String> trackSortingValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.TrackSorting.values().length; e++)
        {
            trackSortingValues.add(AppSettings.TrackSorting.values()[e].name());
        }

        SettingsListAdapter trackSortingAdapter = new SettingsListAdapter(context, trackSortingValues);
        _trackSortingPicker.setAdapter(trackSortingAdapter);

        ArrayList<String> showVolumeBarValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.ShowVolumeBar.values().length; e++)
        {
            showVolumeBarValues.add(AppSettings.ShowVolumeBar.values()[e].name());
        }

        SettingsListAdapter showVolumeBarAdapter = new SettingsListAdapter(context, showVolumeBarValues);
        _showVolumeBarPicker.setAdapter(showVolumeBarAdapter);

        ArrayList<String> openPlayerOnPlayValues = new ArrayList<>();
        for (int e = 0; e < AppSettings.OpenPlayerOnPlay.values().length; e++)
        {
            openPlayerOnPlayValues.add(AppSettings.OpenPlayerOnPlay.values()[e].name());
        }

        SettingsListAdapter openPlayOnPlayAdapter = new SettingsListAdapter(context, openPlayerOnPlayValues);
        _openPlayerOnPlayPicker.setAdapter(openPlayOnPlayAdapter);
        
        // Reset settings button - setup callback
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

        // Keybinds pickers - setup adapters
        SettingsKeybindListAdapter keybindPlayerVUAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerVU.setAdapter(keybindPlayerVUAdapter);

        SettingsKeybindListAdapter keybindPlayerVDAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerVD.setAdapter(keybindPlayerVDAdapter);

        SettingsKeybindListAdapter keybindPlayerNextAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerNext.setAdapter(keybindPlayerNextAdapter);
        
        SettingsKeybindListAdapter keybindPlayerPrevAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerPrev.setAdapter(keybindPlayerPrevAdapter);

        SettingsKeybindListAdapter keybindPlayerRecallAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerRecall.setAdapter(keybindPlayerRecallAdapter);

        SettingsKeybindListAdapter keybindPlayerSwipeLeftAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerSwipeLeft.setAdapter(keybindPlayerSwipeLeftAdapter);

        SettingsKeybindListAdapter keybindPlayerSwipeRightAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerSwipeRight.setAdapter(keybindPlayerSwipeRightAdapter);

        SettingsKeybindListAdapter keybindPlayerVolumeAdapter = new SettingsKeybindListAdapter(context);
        _keybindPlayerVolume.setAdapter(keybindPlayerVolumeAdapter);

        SettingsKeybindListAdapter keybindQPlayerVUAdapter = new SettingsKeybindListAdapter(context);
        _keybindQPlayerVU.setAdapter(keybindQPlayerVUAdapter);

        SettingsKeybindListAdapter keybindQPlayerVDAdapter = new SettingsKeybindListAdapter(context);
        _keybindQPlayerVD.setAdapter(keybindQPlayerVDAdapter);

        SettingsKeybindListAdapter keybindQPlayerNextAdapter = new SettingsKeybindListAdapter(context);
        _keybindQPlayerNext.setAdapter(keybindQPlayerNextAdapter);

        SettingsKeybindListAdapter keybindQPlayerPrevAdapter = new SettingsKeybindListAdapter(context);
        _keybindQPlayerPrev.setAdapter(keybindQPlayerPrevAdapter);

        SettingsKeybindListAdapter keybindEarphonesUnplugAdapter = new SettingsKeybindListAdapter(context);
        _keybindEarphonesUnplug.setAdapter(keybindEarphonesUnplugAdapter);

        SettingsKeybindListAdapter keybindEarphonesExternalPlay = new SettingsKeybindListAdapter(context);
        _keybindExternalPlay.setAdapter(keybindEarphonesExternalPlay);
    }
    
    private void setupPickersCallbacks()
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }
        
        // Appearance
        _themePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.AppTheme selectedValue = AppSettings.AppTheme.values()[position];
                _presenter.onAppThemeChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        _trackSortingPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.TrackSorting selectedValue = AppSettings.TrackSorting.values()[position];
                AppSettings.AlbumSorting albumSorting = GeneralStorage.getShared().getAlbumSortingValue();
                _presenter.onAppTrackSortingChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        _showVolumeBarPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.ShowVolumeBar selectedValue = AppSettings.ShowVolumeBar.values()[position];
                _presenter.onShowVolumeBarSettingChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        _openPlayerOnPlayPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.OpenPlayerOnPlay selectedValue = AppSettings.OpenPlayerOnPlay.values()[position];
                _presenter.onOpenPlayerOnPlaySettingChange(selectedValue);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Keybinds
        setKeybindsOnItemSelectedListener(_keybindPlayerVU, ApplicationInput.PLAYER_VOLUME_UP_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindPlayerVD, ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindPlayerNext, ApplicationInput.PLAYER_NEXT_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindPlayerPrev, ApplicationInput.PLAYER_PREVIOUS_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindPlayerRecall, ApplicationInput.PLAYER_RECALL);

        setKeybindsOnItemSelectedListener(_keybindPlayerSwipeLeft, ApplicationInput.PLAYER_SWIPE_LEFT);

        setKeybindsOnItemSelectedListener(_keybindPlayerSwipeRight, ApplicationInput.PLAYER_SWIPE_RIGHT);

        setKeybindsOnItemSelectedListener(_keybindPlayerVolume, ApplicationInput.PLAYER_VOLUME);
        
        setKeybindsOnItemSelectedListener(_keybindQPlayerVU, ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindQPlayerVD, ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindQPlayerNext, ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindQPlayerPrev, ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);

        setKeybindsOnItemSelectedListener(_keybindEarphonesUnplug, ApplicationInput.EARPHONES_UNPLUG);

        setKeybindsOnItemSelectedListener(_keybindExternalPlay, ApplicationInput.EXTERNAL_PLAY);
    }
    
    private void setKeybindsOnItemSelectedListener(final Spinner spinner, final ApplicationInput input)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinner.isClickable())
                {
                    return;
                }
                
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
        selectProperOpenPlayerOnPlayValue();
        selectProperKeybinds();
    }
    
    private void selectProperAppTheme()
    {
        AppSettings.AppTheme value = GeneralStorage.getShared().getAppThemeValue();
        
        for (int e = 0; e < AppSettings.AppTheme.values().length; e++)
        {
            if (value == AppSettings.AppTheme.values()[e])
            {
                _themePicker.setSelection(e, false);
                break;
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
                _trackSortingPicker.setSelection(e, false);
                break;
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
                _showVolumeBarPicker.setSelection(e, false);
                break;
            }
        }
    }

    private void selectProperOpenPlayerOnPlayValue()
    {
        AppSettings.OpenPlayerOnPlay value = GeneralStorage.getShared().getOpenPlayerOnPlayValue();

        for (int e = 0; e < AppSettings.OpenPlayerOnPlay.values().length; e++)
        {
            if (value == AppSettings.OpenPlayerOnPlay.values()[e])
            {
                _openPlayerOnPlayPicker.setSelection(e, false);
                break;
            }
        }
    }
    
    private void selectProperKeybinds()
    {
        GeneralStorage storage = GeneralStorage.getShared();
        Map<ApplicationInput, ApplicationAction> keybinds = storage.retrieveAllSettingsActionValues();
        
        ApplicationAction PLAYER_VOLUME_UP_BUTTON = keybinds.get(ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
        _keybindPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_UP_BUTTON), false);
        
        ApplicationAction PLAYER_VOLUME_DOWN_BUTTON = keybinds.get(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
        _keybindPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME_DOWN_BUTTON), false);
        
        ApplicationAction PLAYER_NEXT_BUTTON = keybinds.get(ApplicationInput.PLAYER_NEXT_BUTTON);
        _keybindPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_NEXT_BUTTON), false);
        
        ApplicationAction PLAYER_PREVIOUS_BUTTON = keybinds.get(ApplicationInput.PLAYER_PREVIOUS_BUTTON);
        _keybindPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_PREVIOUS_BUTTON), false);

        ApplicationAction PLAYER_RECALL = keybinds.get(ApplicationInput.PLAYER_RECALL);
        _keybindPlayerRecall.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_RECALL), false);

        ApplicationAction PLAYER_SWIPE_LEFT = keybinds.get(ApplicationInput.PLAYER_SWIPE_LEFT);
        _keybindPlayerSwipeLeft.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_SWIPE_LEFT), false);

        ApplicationAction PLAYER_SWIPE_RIGHT = keybinds.get(ApplicationInput.PLAYER_SWIPE_RIGHT);
        _keybindPlayerSwipeRight.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_SWIPE_RIGHT), false);
        
        ApplicationAction PLAYER_VOLUME = keybinds.get(ApplicationInput.PLAYER_VOLUME);
        _keybindPlayerVolume.setSelection(SettingsKeybindListAdapter.getCountForAction(PLAYER_VOLUME), false);
        
        ApplicationAction QUICK_PLAYER_VOLUME_UP_BUTTON = keybinds.get(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);
        _keybindQPlayerVU.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_UP_BUTTON), false);

        ApplicationAction QUICK_PLAYER_VOLUME_DOWN_BUTTON = keybinds.get(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);
        _keybindQPlayerVD.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_VOLUME_DOWN_BUTTON), false);

        ApplicationAction QUICK_PLAYER_NEXT_BUTTON = keybinds.get(ApplicationInput.QUICK_PLAYER_NEXT_BUTTON);
        _keybindQPlayerNext.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_NEXT_BUTTON), false);

        ApplicationAction QUICK_PLAYER_PREVIOUS_BUTTON = keybinds.get(ApplicationInput.QUICK_PLAYER_PREVIOUS_BUTTON);
        _keybindQPlayerPrev.setSelection(SettingsKeybindListAdapter.getCountForAction(QUICK_PLAYER_PREVIOUS_BUTTON), false);

        ApplicationAction EARPHONES_UNPLUG = keybinds.get(ApplicationInput.EARPHONES_UNPLUG);
        _keybindEarphonesUnplug.setSelection(SettingsKeybindListAdapter.getCountForAction(EARPHONES_UNPLUG), false);

        ApplicationAction EXTERNAL_PLAY = keybinds.get(ApplicationInput.EXTERNAL_PLAY);
        _keybindExternalPlay.setSelection(SettingsKeybindListAdapter.getCountForAction(EXTERNAL_PLAY), false);
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
    
    public void enableInteraction()
    {
        _themePicker.setClickable(true);
        _trackSortingPicker.setClickable(true);
        _showVolumeBarPicker.setClickable(true);
        _openPlayerOnPlayPicker.setClickable(true);

        _keybindPlayerVU.setClickable(true);
        _keybindPlayerVD.setClickable(true);
        _keybindPlayerNext.setClickable(true);
        _keybindPlayerPrev.setClickable(true);
        _keybindPlayerRecall.setClickable(true);
        _keybindPlayerSwipeLeft.setClickable(true);
        _keybindPlayerSwipeRight.setClickable(true);
        _keybindPlayerVolume.setClickable(true);
        _keybindQPlayerVU.setClickable(true);
        _keybindQPlayerVD.setClickable(true);
        _keybindQPlayerNext.setClickable(true);
        _keybindQPlayerPrev.setClickable(true);
        _keybindEarphonesUnplug.setClickable(true);
        _keybindExternalPlay.setClickable(true);
        
        _resetSettingsButton.setClickable(true);
    }

    public void disableInteraction()
    {
        _themePicker.setClickable(false);
        _trackSortingPicker.setClickable(false);
        _showVolumeBarPicker.setClickable(false);
        _openPlayerOnPlayPicker.setClickable(false);

        _keybindPlayerVU.setClickable(false);
        _keybindPlayerVD.setClickable(false);
        _keybindPlayerNext.setClickable(false);
        _keybindPlayerPrev.setClickable(false);
        _keybindPlayerRecall.setClickable(false);
        _keybindPlayerSwipeLeft.setClickable(false);
        _keybindPlayerSwipeRight.setClickable(false);
        _keybindPlayerVolume.setClickable(false);
        _keybindQPlayerVU.setClickable(false);
        _keybindQPlayerVD.setClickable(false);
        _keybindQPlayerNext.setClickable(false);
        _keybindQPlayerPrev.setClickable(false);
        _keybindEarphonesUnplug.setClickable(false);
        _keybindExternalPlay.setClickable(false);
        
        _resetSettingsButton.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist)
    {
        
    }
    
    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<AudioPlaylist> playlists)
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
    public void searchQueryResults(@NonNull String searchQuery, @NonNull List<AudioTrack> songs, @Nullable String searchTip)
    {
        
    }

    @Override
    public void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage)
    {
        // Init UI
        initUI();

        // Select correct values
        selectProperValues();

        // Setup user interaction for the picker views
        // Do this after selectProperValues(), to prevent the callbacks from being fired
        setupPickersCallbacks();

        // Hide progress indicator
        hideProgressIndicator();
    }

    @Override
    public void appSettingsReset()
    {
        // Notify root view
        if (_rootView != null)
        {
            _rootView.appSettingsReset();
        }
        
        // Update self
        selectProperValues();
    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        // Notify root view
        if (_rootView != null)
        {
            _rootView.appThemeChanged(appTheme);
        }
        
        // Update self
        FragmentActivity a = getActivity();

        if (a == null)
        {
            return;
        }

        // Check if the fragment already have the correct app theme
        if (_currentAppTheme == GeneralStorage.getShared().getAppThemeValue())
        {
            return;
        }

        // Retrieve and store app theme again
        _currentAppTheme = GeneralStorage.getShared().getAppThemeValue();
        
        // Reload
        Fragment fragment = this;
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(fragment);
        ft.attach(fragment);
        ft.commit();
    }
    
    @Override
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {
        // Notify root view
        if (_rootView != null)
        {
            _rootView.appTrackSortingChanged(trackSorting);
        }
    }
    
    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onFetchDataErrorEncountered(@NonNull Exception error)
    {
        if (getView() == null)
        {
            return;
        }

        // Retry until we succeed
        _presenter.fetchData();
    }
    
    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        };

        AlertWindows.showAlert(getContext(), R.string.error_invalid_file, R.string.error_invalid_file_play, R.string.ok, action);
    }

    private void showProgressIndicator()
    {
        _layout.setVisibility(View.INVISIBLE);
        
        _progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator()
    {
        _layout.setVisibility(View.VISIBLE);
        
        _progressIndicator.setVisibility(View.GONE);
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