package com.media.notabadplayer.View.Main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

// Exclussively used by other programs when trying to open an audio file with Not A Bad Player.
// Props to the google engineers for being completely clowns and not providing an option to do
// something like this in the manifest.
public class ViewMainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent trigger = getIntent();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        
        if (trigger != null)
        {
            intent.setAction(trigger.getAction());
            intent.setData(trigger.getData());
        }
        
        startActivity(intent);
        
        overridePendingTransition(0, 0);
        
        finish();
    }
}
