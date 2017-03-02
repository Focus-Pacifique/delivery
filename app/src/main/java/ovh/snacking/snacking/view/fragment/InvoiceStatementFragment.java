package ovh.snacking.snacking.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.NumberToWords.FrenchNumberToWords;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.DolibarrInvoiceStatementAdapter;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.DolibarrInvoice;


/**
 * Created by Alex on 09/02/2017.
 */

public class InvoiceStatementFragment extends Fragment {

    private Realm realm;
    private Customer mCustomer;
    private Date mStartDate, mEndDate;

    public void setInformations(Customer customer, Date startDate, Date endDate) {
        mCustomer = customer;
        mStartDate = startDate;
        mEndDate = endDate;
    }

    private Integer computeTotalTTC(RealmResults<DolibarrInvoice> invoices) {
        Integer totalTTC = 0;
        for(DolibarrInvoice invoice : invoices) {
            totalTTC += invoice.getTotal_ttc().intValue();
        }
        return totalTTC;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        realm = RealmSingleton.getInstance(getContext()).getRealm();
        final View layout = inflater.inflate(R.layout.invoice_statement_hot_food, container, false);

        // Set up the invoice lines adapter
        final RealmResults<DolibarrInvoice> invoices = realm.where(DolibarrInvoice.class).equalTo("socid", mCustomer.getId()).greaterThanOrEqualTo("date", mStartDate).lessThanOrEqualTo("date", mEndDate).findAllSorted("date");

        // Customer
        TextView customer = (TextView) layout.findViewById(R.id.customer_name);
        customer.setText(mCustomer.getName());

        // Invoices
        ListView listView = (ListView) layout.findViewById(R.id.list_view_invoice_statement_lines);
        DolibarrInvoiceStatementAdapter invoiceStatementAdapter = new DolibarrInvoiceStatementAdapter(getContext(), invoices);
        listView.setAdapter(invoiceStatementAdapter);

        // Invoice date and number
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
        TextView startDate = (TextView) layout.findViewById(R.id.invoice_statement_start_date);
        startDate.setText("du : " + simpleDate.format(mStartDate));

        TextView endDate = (TextView) layout.findViewById(R.id.invoice_statement_end_date);
        endDate.setText("au : " + simpleDate.format(mEndDate));

        // Total
        final TextView tot_ttc = (TextView) layout.findViewById(R.id.invoice_statement_tot_ttc);
        Integer TOT_TTC = computeTotalTTC(invoices);
        tot_ttc.setText(String.format("%,d", TOT_TTC) + " XPF");

        TextView tot_words = (TextView) layout.findViewById(R.id.tot_words);
        String total_in_words = "";
        if (TOT_TTC < 0) {
            total_in_words += "- (moins) ";
        }
        total_in_words += FrenchNumberToWords.convert(TOT_TTC);
        total_in_words += " francs CFP";
        tot_words.setText(total_in_words);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
