package io.gresse.hugo.anecdote.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.ResetAppEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;

/**
 * Anecdote preferences fragment
 * <p/>
 * Created by Hugo Gresse on 06/03/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference myPref = findPreference(getString(R.string.pref_reset_key));
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                EventBus.getDefault().post(new ResetAppEvent());
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChangeTitleEvent(getString(R.string.action_settings)));
        EventTracker.trackFragmentView(this, null, EventTracker.CONTENT_TYPE_APP);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().post(new UpdateAnecdoteFragmentEvent());
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /***************************
     * implements SharedPreferences.OnSharedPreferenceChangeListener
     ***************************/

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value = "";
        if (key.equals(getString(R.string.pref_rowstriping_key))) {
            value = String.valueOf(sharedPreferences.getBoolean(key, false));
        } else if (key.equals(getString(R.string.pref_textsize_key))) {
            value = String.valueOf(sharedPreferences.getString(key, null));
        }

        EventTracker.trackSettingChange(key, value);
    }

}
