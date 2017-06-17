package ovh.snacking.snacking.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import com.takisoft.fix.support.v7.preference.SwitchPreferenceCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.InvoiceController;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.User;
import ovh.snacking.snacking.util.LibUtil;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 04/02/2017.
 * Preference fragment
 */

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Application
    public static final String PREF_APPLICATION_USER            = "pref_application_user";
    public static final String PREF_APPLICATION_PRODUCT_PER_ROW = "pref_application_product_per_row";

    // Invoice reference
    public static final String PREF_INVOICE_PREFIX_AVOIR        = "pref_invoice_prefix_avoir";
    public static final String PREF_INVOICE_PREFIX_FACTURE      = "pref_invoice_prefix_facture";
    public static final String PREF_INVOICE_USERNAME            = "pref_invoice_username";
    public static final String PREF_INVOICE_NEXT_AVOIR          = "pref_invoice_next_avoir";
    public static final String PREF_INVOICE_NEXT_FACTURE        = "pref_invoice_next_facture";

    // Data & sync
    public static final String PREF_SYNC_AUTO                   = "pref_sync_auto";
    public static final String PREF_SYNC_LAST_DATE              = "pref_sync_last_date";

    private Realm realm;

    @Override
    public void onCreatePreferencesFix(Bundle bundle, String s) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = RealmSingleton.getInstance(getActivity()).getRealm();

        User user = realm.where(User.class).equalTo("isActive", true).findFirst();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(PREF_APPLICATION_USER, user.getName())
                .apply();

        // Set the username for the first time
        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PREF_INVOICE_USERNAME, "").isEmpty())
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putString(PREF_INVOICE_USERNAME, user.getName())
                    .apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_fragment_settings));
        ((FloatingActionButton) getActivity().findViewById(R.id.fab)).hide();

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        Map<String, ?> preferencesMap = sharedPreferences.getAll();
        // iterate through the preference entries and update their summary
        for (Map.Entry<String, ?> preferenceEntry : preferencesMap.entrySet()) {
            updateSummary(sharedPreferences, preferenceEntry.getKey());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(sharedPreferences, key);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            // Application
            case PREF_APPLICATION_USER:
                findPreference(PREF_APPLICATION_USER).setSummary(sharedPreferences.getString(PREF_APPLICATION_USER, ""));
                break;
            case PREF_APPLICATION_PRODUCT_PER_ROW:
                findPreference(PREF_APPLICATION_PRODUCT_PER_ROW).setSummary(sharedPreferences.getString(PREF_APPLICATION_PRODUCT_PER_ROW, ""));
                break;

            // Invoice
            case PREF_INVOICE_PREFIX_AVOIR:
                findPreference(PREF_INVOICE_PREFIX_AVOIR).setSummary(sharedPreferences.getString(PREF_INVOICE_PREFIX_AVOIR, ""));
                break;
            case PREF_INVOICE_PREFIX_FACTURE:
                findPreference(PREF_INVOICE_PREFIX_FACTURE).setSummary(sharedPreferences.getString(PREF_INVOICE_PREFIX_FACTURE, ""));
                break;
            case PREF_INVOICE_USERNAME:
                findPreference(PREF_INVOICE_USERNAME).setSummary(sharedPreferences.getString(PREF_INVOICE_USERNAME, ""));
                findPreference(PREF_INVOICE_NEXT_AVOIR).setSummary(InvoiceController.computeInvoiceNextReference(getActivity(), Invoice.AVOIR));
                findPreference(PREF_INVOICE_NEXT_FACTURE).setSummary(InvoiceController.computeInvoiceNextReference(getActivity(), Invoice.FACTURE));
                break;
            case PREF_INVOICE_NEXT_AVOIR:
                findPreference(PREF_INVOICE_NEXT_AVOIR).setSummary(InvoiceController.computeInvoiceNextReference(getActivity(), Invoice.AVOIR));
                break;
            case PREF_INVOICE_NEXT_FACTURE:
                findPreference(PREF_INVOICE_NEXT_FACTURE).setSummary(InvoiceController.computeInvoiceNextReference(getActivity(), Invoice.FACTURE));
                break;

            // Data & sync
            case PREF_SYNC_AUTO:
                boolean autoSync = sharedPreferences.getBoolean(PREF_SYNC_AUTO, true);
                ((SwitchPreferenceCompat) findPreference(PREF_SYNC_AUTO)).setChecked(autoSync);
                if(autoSync)
                    LibUtil.schedulePeriodicSync(getActivity());
                else
                    LibUtil.removePeriodicSync(getActivity());
                break;
            case PREF_SYNC_LAST_DATE:
                long manualSync = sharedPreferences.getLong(PREF_SYNC_LAST_DATE, 0);
                if(manualSync != 0)
                    findPreference(PREF_SYNC_LAST_DATE).setSummary(getString(R.string.pref_sync_last_date_summary) + " " +
                            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(manualSync)));
                break;
        }
    }
}
