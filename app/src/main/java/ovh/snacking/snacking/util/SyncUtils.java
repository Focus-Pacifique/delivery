package ovh.snacking.snacking.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

import ovh.snacking.snacking.controller.Constants;
import ovh.snacking.snacking.service.DolibarrService;

/**
 * Created by alex on 25/02/17.
 */

public class SyncUtils {

    public static void addPeriodicSync(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to start at approximately 2:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);

        Intent intent = new Intent(context, DolibarrService.class);
        intent.setAction(Constants.SYNC_DATA_WITH_DOLIBARR);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public static void removePeriodicSync(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DolibarrService.class);
        intent.setAction(Constants.SYNC_DATA_WITH_DOLIBARR);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.cancel(alarmIntent);
    }
}
