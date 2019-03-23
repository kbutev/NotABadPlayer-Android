package com.media.notabadplayer.Utilities;

import android.app.Activity;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.R;

public class AppThemeSetter
{
    public static void setTheme(Activity activity, AppSettings.AppTheme theme)
    {
        switch (theme)
        {
            case LIGHT:
                activity.setTheme(R.style.Light);
                break;
            case DARK:
                activity.setTheme(R.style.Dark);
                break;
            case MIX:
                activity.setTheme(R.style.Mix);
                break;
        }
    }
}
