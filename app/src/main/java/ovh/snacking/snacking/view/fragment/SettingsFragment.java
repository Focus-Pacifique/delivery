package ovh.snacking.snacking.view.fragment;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.Constants;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Value;
import ovh.snacking.snacking.controller.service.DolibarrBroadcastReceiver;
import ovh.snacking.snacking.util.SyncUtils;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 04/02/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String PREF_KEY_AUTO_SYNC = "pref_key_auto_sync";
    public static final String PREF_KEY_MANUAL_SYNC = "pref_key_manual_sync";

    private Realm realm;
    private Value mValue;
    private RealmChangeListener<Value> callback = new RealmChangeListener<Value>() {
        @Override
        public void onChange(Value value) {
            mValue = value;
            setLastSyncDate(mValue.getLastSync());
        }
    };

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref);

        Preference syncPref = findPreference(PREF_KEY_AUTO_SYNC);
        syncPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    SyncUtils.schedulePeriodicSync(getContext());
                } else {
                    SyncUtils.removePeriodicSync(getContext());
                }
                return true;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
        mValue = realm.where(Value.class).findFirst();

        //Filter to get the synchronisation status with broadcast notification
        IntentFilter mStatusIntentFilter = new IntentFilter(Constants.BROADCAST_MESSAGE_INTENT);
        DolibarrBroadcastReceiver mSyncReceiver = new DolibarrBroadcastReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSyncReceiver, mStatusIntentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_fragment_settings));
        mValue.addChangeListener(callback);
        setLastSyncDate(mValue.getLastSync());
    }

    @Override
    public void onStop() {
        super.onStop();
        mValue.removeChangeListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setLastSyncDate(Date date) {
        Preference pref = findPreference(PREF_KEY_MANUAL_SYNC);
        if (date != null) {
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            pref.setSummary(getString(R.string.pref_summary_manual_sync) + " " + simpleDate.format(date));
        } else {
            pref.setSummary(getString(R.string.pref_summary_manual_sync) + " : jamais");
        }
    }
}
