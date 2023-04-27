package org.marshsoft.ussdautopushy.ui;

import android.os.Bundle;
import android.text.InputType;
import androidx.preference.PreferenceFragmentCompat;

import org.marshsoft.ussdautopushy.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        androidx.preference.EditTextPreference editUssdTextDelayPreference = getPreferenceManager().findPreference("UssdDelay");
        androidx.preference.EditTextPreference editSmsTextDelayPreference = getPreferenceManager().findPreference("UssdDelay");
        androidx.preference.EditTextPreference editTextTokenPreference = getPreferenceManager().findPreference("token");
        assert editUssdTextDelayPreference != null;
        assert editSmsTextDelayPreference != null;
        assert editTextTokenPreference != null;
        editTextTokenPreference.setOnBindEditTextListener(editText -> editText.setEnabled(false));
        editUssdTextDelayPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
        editSmsTextDelayPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));

    }
}