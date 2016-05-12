package com.giuseppepaoletti.sei;

import com.giuseppepaoletti.sei.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Created by Giuseppe Paoletti on 04/05/2016.
 */

public class Preferences extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefFragment())
                .commit();
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return PrefFragment.class.getName().equals(fragmentName);
    }
}
