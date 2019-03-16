package com.media.notabadplayer.View.Settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements BaseView
{
    private String[] APPEARANCE_VALUES = {"LIGHT", "DARK", "GRAY"};
    
    private BasePresenter _presenter;
    
    private Spinner _appearancePicker;
    private SettingsListAdapter _appearanceAdapter;
    
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
        _appearancePicker = root.findViewById(R.id.themePicker);
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
        
        // Init UI
        initUI();
        
        // Select correct app theme
        selectProperAppTheme();
        
        // Select correct keybinds
        selectProperKeybinds();
        
        return root;
    }
    
    private void initUI()
    {
        _appearanceAdapter = new SettingsListAdapter(getContext(), APPEARANCE_VALUES);
        _appearancePicker.setAdapter(_appearanceAdapter);
        _appearancePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _presenter.onAppThemeChange(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
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
    }
    
    private void setKeybindsOnItemSelectedListener(Spinner spinner, final ApplicationInput input)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ApplicationAction action = ApplicationAction.values()[position];
                _presenter.onKeybindSelected(action, input);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void selectProperAppTheme()
    {
        int appThemeValue = GeneralStorage.getShared().getAppThemeValue(getContext());
        
        if (appThemeValue >= APPEARANCE_VALUES.length)
        {
            appThemeValue = 0;
        }
        
        _appearancePicker.setSelection(appThemeValue);
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
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover) {
        
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
    public void appThemeChanged()
    {

    }
    
    @Override
    public void appSortingChanged()
    {

    }
    
    class SettingsListAdapter extends BaseAdapter
    {
        private String[] _values;
        
        private Context _context;
        
        public SettingsListAdapter(@NonNull Context context, String[] values)
        {
            this._values = values;
            this._context = context;
        }
        
        public int getCount()
        {
            return _values.length;
        }
        
        public Object getItem(int position)
        {
            return _values[position];
        }
        
        public long getItemId(int position)
        {
            return position;
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View listItem = LayoutInflater.from(_context).inflate(R.layout.item_keybind_action, parent, false);

            TextView title = listItem.findViewById(R.id.title);
            title.setText(_values[position]);

            return listItem;
        }
    }
}