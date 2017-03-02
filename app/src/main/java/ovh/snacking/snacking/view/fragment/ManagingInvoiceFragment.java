package ovh.snacking.snacking.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.InvoiceAdapter;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 17/11/2016.
 */

public class ManagingInvoiceFragment extends ListFragment {

    OnInvoiceListener mListener;
    private Realm realm;
    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnInvoiceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnInvoiceSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
        // Show only the invoices greater than yesterday
        RealmResults<Invoice> invoices = oldInvoices(3);
        setListAdapter(new InvoiceAdapter(getContext(), invoices));
    }

    @Override
    public void onStart() {
        super.onStart();

        // On invoice click : go to edit the invoice
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer invoiceId = ((Invoice) parent.getItemAtPosition(position)).getId();
                mListener.onInvoiceSelected(invoiceId);
            }
        });

        // On long click : remove the print_invoice
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Integer invoiceId = ((Invoice) parent.getItemAtPosition(position)).getId();
                mListener.onInvoiceLongClick(invoiceId);
                return true;
            }
        });

        // On fab click : new Invoice
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNewInvoice();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_managing_invoices));
        fab.setImageResource(R.drawable.ic_new);
        fab.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private RealmResults<Invoice> oldInvoices(Integer days) {
        RealmResults<Invoice> invoices;
        try {
            // Number of days to filter
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String strDate = dateFormat.format(new Date(System.currentTimeMillis() - days * (1000 * 60 * 60 * 24)));
            Date date = dateFormat.parse(strDate);

            // Order by fields
            //String []fieldNames={"type","date"};
            //Sort sort[]={Sort.ASCENDING,Sort.DESCENDING};

            invoices = realm.where(Invoice.class).greaterThan("date", date).findAllSorted("date", Sort.DESCENDING);
        } catch (ParseException e) {
            //e.printStackTrace();
            invoices = realm.where(Invoice.class).findAllSorted("date", Sort.DESCENDING);
        }
        return invoices;
    }

    public interface OnInvoiceListener {
        void onInvoiceSelected(Integer invoiceId);

        void onInvoiceLongClick(Integer invoiceId);

        void onNewInvoice();
    }
}
