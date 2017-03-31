package ovh.snacking.snacking.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import ovh.snacking.snacking.controller.service.DolibarrService;

/**
 * Created by alex on 25/02/17.
 */

public class SyncUtils {

    public static void schedulePeriodicSync(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to start at approximately 11:00 p.m / 23h00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);

        Intent intent = new Intent(context, DolibarrService.class);
        intent.setAction(Constants.SYNC_DATA_WITH_DOLIBARR);

        if (PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE) == null) {
            PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public static void removePeriodicSync(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DolibarrService.class);
        intent.setAction(Constants.SYNC_DATA_WITH_DOLIBARR);

        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (alarmIntent != null) {
            alarmMgr.cancel(alarmIntent);
            alarmIntent.cancel();
        }
    }
}
