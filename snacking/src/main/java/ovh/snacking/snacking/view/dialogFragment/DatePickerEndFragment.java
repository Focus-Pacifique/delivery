package ovh.snacking.snacking.view.dialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by ACER on 08/02/2017.
 */

public class DatePickerEndFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    OnDatePickerEndFragment mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnDatePickerEndFragment) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDatePickerStartFragment");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        mListener.onEndDateSet(year, month, day);
        this.dismiss();
        //getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, date);
    }

    public interface OnDatePickerEndFragment {
        void onEndDateSet(int year, int month, int day);
    }
}
