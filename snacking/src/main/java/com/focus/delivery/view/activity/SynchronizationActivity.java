package com.focus.delivery.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import com.focus.delivery.R;
import com.focus.delivery.service.DolibarrBroadcastReceiver;
import com.focus.delivery.service.DolibarrService;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.model.User;
import com.focus.delivery.util.Constants;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.fragment.PreferencesFragment;

public class SynchronizationActivity extends AppCompatActivity {

    private Realm realm;

    private RealmResults<Invoice> invoicesToPost;
    private RealmChangeListener<RealmResults<Invoice>> callbackInvoice = new RealmChangeListener<RealmResults<Invoice>>() {
        @Override
        public void onChange(RealmResults<Invoice> invoices) {
            if (invoices.size() == 0) {
                refreshUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronization);

        realm = RealmSingleton.getInstance(getApplicationContext()).getRealm();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Filter to get the synchronisation status with broadcast notification
        IntentFilter mStatusIntentFilter = new IntentFilter(Constants.BROADCAST_MESSAGE_INTENT);
        DolibarrBroadcastReceiver mSyncReceiver = new DolibarrBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mSyncReceiver, mStatusIntentFilter);

        // Back arrow in the menu
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register the invoice to post callback
        invoicesToPost = realm.where(Invoice.class).equalTo("state", Invoice.FINISHED).equalTo("isPOSTToDolibarr", false).findAll();
        invoicesToPost.addChangeListener(callbackInvoice);

        // Show the current user
        User user = realm.where(User.class).equalTo("isActive", true).findFirst();
        final TextView tab_name = (TextView) findViewById(R.id.text_view_tab_name);
        if (user != null) {
            tab_name.setText(" " + user.getName());
        }

        refreshUI();

        final Button buttonUpdate = (Button) findViewById(R.id.button_update);
        final Button buttonPostInvoices = (Button) findViewById(R.id.button_post_invoice);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonUpdate.setEnabled(false);
                buttonPostInvoices.setEnabled(false);
                launchService(Constants.GET_DATA_FROM_DOLIBARR);
            }
        });


        buttonPostInvoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonUpdate.setEnabled(false);
                buttonPostInvoices.setEnabled(false);
                launchService(Constants.POST_INVOICE_TO_DOLIBARR);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        invoicesToPost.removeAllChangeListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    private void launchService(String action) {
        // Test if the network is reachable and then start the sync service
        if(isNetworkConnected()) {
            Intent intent = new Intent(SynchronizationActivity.this, DolibarrService.class);
            if (Constants.GET_DATA_FROM_DOLIBARR.equals(action)) {
                intent.setAction(action);
            } else if (Constants.POST_INVOICE_TO_DOLIBARR.equals(action)) {
                intent.setAction(action);
            }
            startService(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SynchronizationActivity.this);
            builder.setMessage(R.string.network_error_activity_sync)
                    .setTitle(R.string.dialog_title_activity_sync)
                    .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void updateLastSyncDate() {
        long date = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(PreferencesFragment.PREF_SYNC_LAST_DATE, 0);
        Date dateLastSync = new Date(date);
        TextView textView = (TextView) findViewById(R.id.last_sync_date);
        if(null != dateLastSync) {
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            textView.setText(simpleDate.format(date));
        } else {
            textView.setText("Jamais synchronisé !");
        }
    }

    private void refreshUI() {
        updateLastSyncDate();

        Button buttonUpdate = (Button) findViewById(R.id.button_update);
        Button buttonPostInvoices = (Button) findViewById(R.id.button_post_invoice);
        final TextView invoiceToPost = (TextView) findViewById(R.id.invoice_to_post);

        // Show the invoices status (POST to dolibarr, not POST, etc...)
        RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("state", Invoice.FINISHED).equalTo("isPOSTToDolibarr", false).findAll();

        if (invoices.size() == 0) {
            // TODO changer etat des boutons pour pas qu'on puisse mettre à jour sans avoir poster les factures
            buttonPostInvoices.setEnabled(false);
            buttonUpdate.setEnabled(true);
            invoiceToPost.setText(R.string.invoice_to_post);
        } else {
            // TODO changer etat des boutons pour pas qu'on puisse mettre à jour sans avoir poster les factures
            buttonPostInvoices.setEnabled(true);
            buttonUpdate.setEnabled(false);
            invoiceToPost.setText("(" + invoices.size() + ") Factures/Avoirs terminés non postés dans Dolibarr. Postez les avant de pouvoir faire une MAJ.");
        }

    }
}
