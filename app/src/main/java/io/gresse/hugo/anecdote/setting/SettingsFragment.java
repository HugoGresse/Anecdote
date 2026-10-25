package io.gresse.hugo.anecdote.setting;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.ResetAppEvent;

/**
 * Anecdote preferences fragment
 * <p/>
 * Created by Hugo Gresse on 06/03/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference resetPreference = findPreference(getString(R.string.pref_reset_key));
        if (resetPreference != null) {
            resetPreference.setOnPreferenceClickListener(preference -> {
                EventBus.getDefault().post(new ResetAppEvent());
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChangeTitleEvent(getString(R.string.action_settings)));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().post(new UpdateAnecdoteFragmentEvent());
    }
}
