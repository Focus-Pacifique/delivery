package ovh.snacking.snacking.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.Realm;
import ovh.snacking.snacking.NumberToWords.FrenchNumberToWords;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.TaxAdapter;
import ovh.snacking.snacking.controller.print.PrintInvoiceAdapter;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 19/11/2016.
 */

public class PrintInvoiceFragment extends Fragment {

    OnPrintListener mListener;
    private Integer invoiceId = 0;
    private Invoice mInvoice;
    private Realm realm;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPrintListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPrintListener");
        }
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = RealmSingleton.getInstance(getContext()).getRealm();
        final View layout = inflater.inflate(R.layout.print_invoice, container, false);

        // Set up the invoice lines adapter
        mInvoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
        ListView listView = (ListView) layout.findViewById(R.id.list_view_invoice_lines);
        PrintInvoiceAdapter lineInvoiceAdapter = new PrintInvoiceAdapter(getContext(), mInvoice.getLines(), mInvoice.getId());
        listView.setAdapter(lineInvoiceAdapter);

        // Customer
        TextView customer = (TextView) layout.findViewById(R.id.customer_name);
        customer.setText(mInvoice.getCustomer().getName());

        // Invoice date and number
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
        TextView date = (TextView) layout.findViewById(R.id.invoice_date);
        date.setText("Date : " + simpleDate.format(mInvoice.getDate()));

        TextView invoice_title = (TextView) layout.findViewById(R.id.invoice_title);
        invoice_title.setText(getTitle(mInvoice));

        TextView invoice_number = (TextView) layout.findViewById(R.id.invoice_number);
        if (Invoice.FINISHED.equals(mInvoice.getState())) {
            invoice_number.setText(mInvoice.getRef());
        } else {
            invoice_number.setText(mInvoice.computeRef(0));
        }

        // Facture source pour les avoirs
        TextView facture_source_number = (TextView) layout.findViewById(R.id.facture_source_number);
        if (Invoice.AVOIR.equals(mInvoice.getType())) {
            Invoice factureSource = realm.where(Invoice.class).equalTo("id", mInvoice.getFk_facture_source()).findFirst();
            if (factureSource != null) {
                facture_source_number.setText("(de la Facture N°" + factureSource.getRef() + ")");
                facture_source_number.setVisibility(View.VISIBLE);
            }
        } else {
            facture_source_number.setVisibility(View.INVISIBLE);
        }

        setTOTALViews(layout);

        // Listeners
        Button btnPrint = (Button) layout.findViewById(R.id.button_print);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFinished = mListener.onFinishInvoice(invoiceId);
                if (isFinished) {
                    updateInvoiceRef();
                    mListener.onPrint(getView().findViewById(R.id.document_content), invoiceId);
                }
            }
        });

        Button btnChangeInvoice = (Button) layout.findViewById(R.id.button_change_last_invoice);
        if (!mInvoice.isPOSTToDolibarr() && mInvoice.getId() == realm.where(Invoice.class).max("id").intValue() && Invoice.FINISHED.equals(mInvoice.getState())) {
            btnChangeInvoice.setVisibility(View.VISIBLE);
            btnChangeInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onChangeLastInvoice(getView().findViewById(R.id.document_content), invoiceId);
                }
            });
        } else {
            btnChangeInvoice.setVisibility(View.INVISIBLE);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((FloatingActionButton) getActivity().findViewById(R.id.fab)).hide();

        if (mInvoice.getCounterPrint() > 0)
            ((MainActivity) getActivity()).setActionBarTitle("Imprimée (" + mInvoice.getCounterPrint() + ") fois");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private String getTitle(Invoice invoice) {
        String ref;
        if (Invoice.FACTURE.equals(invoice.getType())) {
            ref = "FACTURE N°";
        } else if (Invoice.AVOIR.equals(invoice.getType())) {
            ref = "AVOIR N°";
        } else {
            ref = "INVOICE N°" + invoice.getRef();
        }
        return ref;
    }

    private void updateInvoiceRef() {
        TextView invoice_number = (TextView) getView().findViewById(R.id.invoice_number);
        if(null != invoice_number) {
            invoice_number.setText(mInvoice.getRef());
        }
    }

    private void setTOTALViews(View layout) {
        ArrayList<String[]> taxes = populateTaxAdapter(mInvoice);
        TaxAdapter adapter = new TaxAdapter(getContext(), taxes);

        // Set price adapter to recycler view
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerViewTaxes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        // Convert number to words
        String total_in_words;
        if (Invoice.AVOIR.equals(mInvoice.getType())) {
            total_in_words = "- (moins) " + FrenchNumberToWords.convert(-mInvoice.getTotalTTC());
        } else {
            total_in_words = FrenchNumberToWords.convert(mInvoice.getTotalTTC());
        }
        total_in_words += " francs CFP";
        ((TextView) layout.findViewById(R.id.tot_words)).setText(total_in_words);
    }

    private ArrayList<String[]> populateTaxAdapter(Invoice invoice) {
        ArrayList<String[]> taxes = new ArrayList<>();

        // Total HT
        Integer TOT_HT = invoice.getTotalHT();
        if (Invoice.AVOIR.equals(invoice.getType())) {
            TOT_HT = -TOT_HT;
        }
        String[] HT = new String[2];
        HT[0] = getString(R.string.tot_ht);
        HT[1] = String.format("%,d", TOT_HT) + " " + getString(R.string.xpf);
        taxes.add(HT);


        // Multiple tax rates
        NumberFormat nf = new DecimalFormat("##.##");
        HashMap<Double, Integer> totalTaxes = mInvoice.getTotalTaxes();
        Iterator<Map.Entry<Double, Integer>> it = totalTaxes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Double, Integer> item = it.next();
            String[] TAX = new String[2];

            TAX[0] = "TGC (" + nf.format(item.getKey()) + " %)";

            Integer TOT_TAX = item.getValue();
            if (Invoice.AVOIR.equals(invoice.getType())) {
                TOT_TAX = -TOT_TAX;
            }
            TAX[1] = String.format("%,d", TOT_TAX) + " " + getString(R.string.xpf);

            taxes.add(TAX);
        }


        // Total Tax2 = TSS
        Integer TOT_TAX2 = mInvoice.getTotalTax2();
        if (Invoice.AVOIR.equals(mInvoice.getType())) {
            TOT_TAX2 = -TOT_TAX2;
        }
        String[] TAX2 = new String[2];
        TAX2[0] = getString(R.string.tax2Label);
        TAX2[1] = String.format("%,d", TOT_TAX2) + " " + getString(R.string.xpf);
        taxes.add(TAX2);


        // Total TTC
        Integer TOT_TTC = mInvoice.getTotalTTC();
        if (Invoice.AVOIR.equals(mInvoice.getType())) {
            TOT_TTC = -TOT_TTC;
        }
        String[] TTC = new String[2];
        TTC[0] = getString(R.string.tot_ttc);
        TTC[1] = String.format("%,d", TOT_TTC) + " " + getString(R.string.xpf);
        taxes.add(TTC);

        return taxes;
    }

    public interface OnPrintListener {
        //void onBackToManageInvoices();

        boolean onFinishInvoice(Integer invoiceId);

        void onChangeLastInvoice(View view, Integer invoiceId);

        void onPrint(View view, Integer invoiceId);
    }

}