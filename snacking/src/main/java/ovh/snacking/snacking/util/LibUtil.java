package ovh.snacking.snacking.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Calendar;

import ovh.snacking.snacking.controller.service.DolibarrService;

/**
 * Created by alex on 25/02/17.
 */

public class LibUtil {

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

    public static CharSequence highlight(String search, String originalText, int color) {
        // ignore case and accents
        // the same thing should have been done for the search text
        String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
        search = search.toLowerCase();

        int start = normalizedText.indexOf(search);
        if (start < 0) {
            // not found, nothing to to
            return originalText;
        } else {
            // highlight each appearance in the original text
            // while searching in normalized text
            SpannableStringBuilder highlighted = new SpannableStringBuilder(originalText);
            while (start >= 0) {
                int spanStart = Math.min(start, originalText.length());
                int spanEnd = Math.min(start + search.length(), originalText.length());

                highlighted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                highlighted.setSpan(new ForegroundColorSpan(color), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = normalizedText.indexOf(search, spanEnd);
            }

            return highlighted;
        }
    }

    public static String formatDouble(double value) {
        NumberFormat nf = new DecimalFormat("#,###.##");
        return nf.format(value);
    }

    public static double parseEditTextInput(EditText et) {
        if (!et.getText().toString().isEmpty()) {
            final Double input = Double.parseDouble(et.getText().toString());
            if (input > 0) {
                return input;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
