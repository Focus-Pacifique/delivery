package ovh.snacking.snacking.controller.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.Constants;


/**
 * Created by Alex on 24/10/2016.
 */

public class DolibarrBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int message = intent.getIntExtra(Constants.BROADCAST_MESSAGE_SEND, 0);
        if (message != 0) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.service_dolibarr_error, Toast.LENGTH_SHORT).show();
        }
    }
}
