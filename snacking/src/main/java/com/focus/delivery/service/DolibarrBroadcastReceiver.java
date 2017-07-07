package com.focus.delivery.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.focus.delivery.R;
import com.focus.delivery.util.Constants;


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
