package ovh.snacking.snacking.view;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.CustomerAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Customer;


public class SettingsActivity extends AppCompatActivity {

    public static final String TAG_PRINT_INVOICE_STATEMENT = "ovh.snacking.snacking.view.PrintInvoiceStatement";
    public static final String TAG_SETTINGS_FRAGMENT = "ovh.snacking.snacking.view.SettingsFragment";

    private FragmentManager fm;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        fm = getSupportFragmentManager();
        realm = RealmSingleton.getInstance(getApplicationContext()).getRealm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        launchFragment(TAG_SETTINGS_FRAGMENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**********************************/
    /**  Methods to manage fragments **/
    /**********************************/

    private void launchFragment(String tag) {
        Fragment frag = getFragment(tag);
        if (!frag.isAdded()) {
            fm.beginTransaction()
                    .replace(android.R.id.content, frag, frag.getTag())
                    //.addToBackStack(null) pas de mémoire
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    private Fragment getFragment(String tag) {
        Fragment frag = fm.findFragmentByTag(tag);
        if (frag != null) {
            return frag;
        } else {
            switch (tag) {
                case TAG_SETTINGS_FRAGMENT:
                    frag = new SettingsFragment();
                    break;
                case TAG_PRINT_INVOICE_STATEMENT:
                    frag = new PrintInvoiceStatementFragment();
                    break;
                default:
                    //Log.e("SWITCH MainActivity", "No case");
            }
            return frag;
        }
    }

    private void cleanFragmentManager() {
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

}
