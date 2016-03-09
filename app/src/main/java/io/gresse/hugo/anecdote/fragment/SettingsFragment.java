package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.UpdateAnecdoteFragmentEvent;

/**
 * Anecdote preferences fragment
 *
 * Created by Hugo Gresse on 06/03/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().post(new ChangeTitleEvent(getString(R.string.action_settings), null));
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().post(new UpdateAnecdoteFragmentEvent());
    }
}
