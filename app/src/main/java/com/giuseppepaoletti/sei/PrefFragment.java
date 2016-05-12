package com.giuseppepaoletti.sei;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Giuseppe Paoletti on 04/05/2016.
 */


public class PrefFragment extends PreferenceFragment
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
