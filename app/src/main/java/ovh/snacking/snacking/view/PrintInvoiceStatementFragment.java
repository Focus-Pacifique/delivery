package ovh.snacking.snacking.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.CustomerAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.DolibarrInvoice;

/**
 * Created by Alex on 04/02/2017.
 */

public class PrintInvoiceStatementFragment extends Fragment {

    private Realm realm;
    private Toolbar toolbar;
    private Button mButtonPrint, mButtonCustomer, mButtonStartDate, mButtonEndDate, mButtonRefresh;

    private Customer mCustomer;
    private Date mStartDate, mEndDate;

    OnPrintInvoiceStatementListener mListener;

    public interface OnPrintInvoiceStatementListener {
        void onBackButtonPressed();
        void onShowInvoiceStatement(Customer customer, Date startDate, Date endDate);
        void onPrintInvoiceStatement(View view);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPrintInvoiceStatementListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPrintInvoiceStatementListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        realm = RealmSingleton.getInstance(getContext()).getRealm();

        final View layout = inflater.inflate(R.layout.print_invoice_statement, container, false);
        mButtonPrint = (Button) layout.findViewById(R.id.button_print);
        mButtonCustomer = (Button) layout.findViewById(R.id.button_change_customer);
        mButtonStartDate = (Button) layout.findViewById(R.id.button_change_start_date);
        mButtonEndDate = (Button) layout.findViewById(R.id.button_change_end_date);
        mButtonRefresh = (Button) layout.findViewById(R.id.button_refresh_UI);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        mButtonCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChooseCustomer();
            }
        });

        mButtonStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePickerDialog();
            }
        });

        mButtonEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickerDialog();
            }
        });

        mButtonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshTopTextView();
                refreshInvoiceStatementUI();
            }
        });

        mButtonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mCustomer) {
                    Toast.makeText(getContext(), "Client vide", Toast.LENGTH_LONG).show();
                } else if (null == mStartDate) {
                    Toast.makeText(getContext(), "Date de début vide", Toast.LENGTH_LONG).show();
                } else if (null == mEndDate) {
                    Toast.makeText(getContext(), "Date de fin vide", Toast.LENGTH_LONG).show();
                } else {
                    if(null != getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_INVOICE_STATEMENT)) {
                        mListener.onPrintInvoiceStatement(getView().findViewById(R.id.invoice_statement));
                    } else {
                        Toast.makeText(getContext(), "Relevé de facture vide", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        ((FloatingActionButton) getActivity().findViewById(R.id.fab)).hide();

        // Back arrow in the menu
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackButtonPressed();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Back arrow in the menu
        toolbar.setNavigationIcon(null);
        toolbar.setNavigationOnClickListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }


    private void dialogChooseCustomer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.dialog_choose_customer)
                .setAdapter(new CustomerAdapter(getContext(), realm.where(Customer.class).findAll()), null);

        final AlertDialog ad = builder.create();
        ad.getListView().setItemsCanFocus(false);
        ad.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ad.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Manage selected item here
                mCustomer = (Customer) parent.getItemAtPosition(position);
                refreshTopTextView();
                ad.dismiss();
            }
        });

        ad.setCanceledOnTouchOutside(true);
        ad.show();
    }

    private void showStartDatePickerDialog() {
        DialogFragment dialogFrag = new DatePickerStartFragment();
        dialogFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialogStartDate");
    }

    private void showEndDatePickerDialog() {
        DialogFragment dialogFrag = new DatePickerEndFragment();
        dialogFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialogEndDate");
    }

    public void setStartDate(int year, int month, int day) {
        mStartDate = new GregorianCalendar(year, month, day).getTime();
        refreshTopTextView();
    }

    public void setEndDate(int year, int month, int day) {
        mEndDate = new GregorianCalendar(year, month, day).getTime();
        refreshTopTextView();
    }


    private void refreshTopTextView() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
        if (null != mCustomer) {
            ((TextView) getView().findViewById(R.id.textView_change_customer)).setText(String.valueOf(mCustomer.getName()));
        }
        if (null != mStartDate) {
            ((TextView) getView().findViewById(R.id.textView_change_start_date)).setText(simpleDate.format(mStartDate));
        }
        if (null != mEndDate) {
            ((TextView) getView().findViewById(R.id.textView_change_end_date)).setText(simpleDate.format(mEndDate));
        }
    }

    private void refreshInvoiceStatementUI() {
        if (null != mCustomer && null != mStartDate && null != mEndDate) {
            mListener.onShowInvoiceStatement(mCustomer, mStartDate, mEndDate);
        }
    }
}
