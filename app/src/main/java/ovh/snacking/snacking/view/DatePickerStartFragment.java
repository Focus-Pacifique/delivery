package ovh.snacking.snacking.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alex on 08/02/2017.
 */

public class DatePickerStartFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    OnDatePickerStartFragment mListener;
    public interface OnDatePickerStartFragment {
        void onStartDateSet(int year, int month, int day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnDatePickerStartFragment) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDatePickerStartFragment");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = 1;

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        mListener.onStartDateSet(year, month, day);
        this.dismiss();
        //getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, date);
    }

}
