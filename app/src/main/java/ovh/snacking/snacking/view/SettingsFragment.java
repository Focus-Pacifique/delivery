package ovh.snacking.snacking.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import ovh.snacking.snacking.R;

/**
 * Created by Alex on 04/02/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    /*OnPrefFragmentListener mListener;

    public interface OnPrefFragmentListener {
        void openInvoiceStatementFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPrefFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPrefFragmentListener");
        }
    }*/

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        super.onDisplayPreferenceDialog(preference);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref);
    }
}
