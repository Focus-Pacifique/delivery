package com.focus.delivery.view.fragment;

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
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import com.focus.delivery.NumberToWords.FrenchNumberToWords;
import com.focus.delivery.R;
import com.focus.delivery.controller.InvoiceController;
import com.focus.delivery.adapter.AdapterInvoiceLine;
import com.focus.delivery.adapter.AdapterTax;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.activity.MainActivity;

/**
 * Created by Alex on 19/11/2016.
 */

public class FragmentPrintInvoice extends Fragment {

    FragmentPrintInvoiceListener mListener;
    private Realm realm;
    private Invoice mInvoice;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FragmentPrintInvoiceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentPrintInvoiceListener");
        }
    }

    public static FragmentPrintInvoice newInstance(int invoiceId) {
        FragmentPrintInvoice frag = new FragmentPrintInvoice();

        Bundle bundle = new Bundle();
        bundle.putInt(Invoice.FIELD_ID, invoiceId);
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = RealmSingleton.getInstance(getContext()).getRealm();
        final View layout = inflater.inflate(R.layout.print_invoice_fragment, container, false);

        // Set up the mInvoice lines adapter
        mInvoice = realm.where(Invoice.class).equalTo(Invoice.FIELD_ID, getArguments().getInt(Invoice.FIELD_ID)).findFirst();
        if(mInvoice != null) {

            // Set mInvoice lines adapter to recycler view
            AdapterInvoiceLine adapterInvoiceLine = new AdapterInvoiceLine(getContext(), mInvoice.getLines(), mInvoice.getType(), AdapterInvoiceLine.VIEW_PRINT);
            RecyclerView recyclerViewInvoiceLines = (RecyclerView) layout.findViewById(R.id.recyclerViewInvoiceLines);
            recyclerViewInvoiceLines.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewInvoiceLines.setAdapter(adapterInvoiceLine);

            // Customer
            TextView customer = (TextView) layout.findViewById(R.id.customer_name);
            customer.setText(mInvoice.getCustomer().getName());

            // Invoice date
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            ((TextView) layout.findViewById(R.id.invoice_date)).setText(String.valueOf("Date : " + simpleDate.format(mInvoice.getDate())));

            // Invoice number
            ((TextView) layout.findViewById(R.id.invoice_title)).setText(getTitle());
            ((TextView) layout.findViewById(R.id.invoice_number)).setText(Invoice.FINISHED == mInvoice.getState() ? mInvoice.getRef() : InvoiceController.computeInvoiceNextReference(getContext(), mInvoice.getType()));

            // Facture source pour les avoirs
            switch (mInvoice.getType()) {
                case Invoice.AVOIR :
                    layout.findViewById(R.id.facture_source_number).setVisibility(View.VISIBLE);
                    Invoice factureSource = realm.where(Invoice.class).equalTo("id", mInvoice.getFk_facture_source()).findFirst();
                    if (factureSource != null)
                        ((TextView) layout.findViewById(R.id.facture_source_number)).setText("(de la Facture N°" + factureSource.getRef() + ")");
                    else
                        ((TextView) layout.findViewById(R.id.facture_source_number)).setText("(facture non trouvée)");
                    break;
                default:
                    layout.findViewById(R.id.facture_source_number).setVisibility(View.GONE);
                    break;
            }

            setTOTALViews(layout, mInvoice);

            // Listeners
            Button btnPrint = (Button) layout.findViewById(R.id.button_print);
            btnPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (InvoiceController.finishInvoice(getContext(), mInvoice)) {
                        TextView invoice_number = (TextView) getView().findViewById(R.id.invoice_number);
                        if(null != invoice_number) {
                            invoice_number.setText(mInvoice.getRef());
                        }
                        mListener.onPrint(getView().findViewById(R.id.document_content), mInvoice.getId());
                    }
                }
            });

            Button btnChangeInvoice = (Button) layout.findViewById(R.id.button_change_last_invoice);
            if (!mInvoice.isPOSTToDolibarr() && mInvoice.getId() == realm.where(Invoice.class).max("id").intValue() && Invoice.FINISHED == mInvoice.getState()) {
                btnChangeInvoice.setVisibility(View.VISIBLE);
                btnChangeInvoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onChangeLastInvoice(getView().findViewById(R.id.document_content), mInvoice.getId());
                    }
                });
            } else {
                btnChangeInvoice.setVisibility(View.GONE);
            }
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

    private String getTitle() {
        String ref = "";
        if (Invoice.FACTURE == mInvoice.getType()) {
            ref = "FACTURE N°";
        } else if (Invoice.AVOIR == mInvoice.getType()) {
            ref = "AVOIR N°";
        }
        return ref;
    }

    private void setTOTALViews(View layout, Invoice invoice) {
        ArrayList<String[]> taxes = populateTaxAdapter(invoice);
        AdapterTax adapter = new AdapterTax(taxes);

        // Set price adapter to recycler view
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerViewTaxes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        // Convert number to words
        String total_in_words;
        if (Invoice.AVOIR == mInvoice.getType()) {
            total_in_words = "- (moins) " + FrenchNumberToWords.convert(-InvoiceController.getTotalTTC(invoice));
        } else {
            total_in_words = FrenchNumberToWords.convert(InvoiceController.getTotalTTC(invoice));
        }
        total_in_words += " francs CFP";
        ((TextView) layout.findViewById(R.id.tot_words)).setText(total_in_words);
    }

    private ArrayList<String[]> populateTaxAdapter(Invoice invoice) {
        ArrayList<String[]> taxes = new ArrayList<>();

        // Total HT
        Integer TOT_HT = InvoiceController.getTotalHT(invoice);
        if (Invoice.AVOIR == invoice.getType()) {
            TOT_HT = -TOT_HT;
        }
        String[] HT = new String[2];
        HT[0] = getString(R.string.tot_ht);
        HT[1] = String.format("%,d", TOT_HT) + " " + getString(R.string.xpf);
        taxes.add(HT);


        // Multiple tax rates
        NumberFormat nf = new DecimalFormat("##.##");
        HashMap<Double, Integer> totalTaxes = InvoiceController.getTotalTaxes(invoice);
        Iterator<Map.Entry<Double, Integer>> it = totalTaxes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Double, Integer> item = it.next();
            String[] TAX = new String[2];

            TAX[0] = "TGC (" + nf.format(item.getKey()) + " %)";

            Integer TOT_TAX = item.getValue();
            if (Invoice.AVOIR == invoice.getType()) {
                TOT_TAX = -TOT_TAX;
            }
            TAX[1] = String.format("%,d", TOT_TAX) + " " + getString(R.string.xpf);

            taxes.add(TAX);
        }


        // Total Tax2 = TSS
        Integer TOT_TAX2 = InvoiceController.getTotalTax2(invoice);
        if (Invoice.AVOIR == mInvoice.getType()) {
            TOT_TAX2 = -TOT_TAX2;
        }
        String[] TAX2 = new String[2];
        TAX2[0] = getString(R.string.tax2Label);
        TAX2[1] = String.format("%,d", TOT_TAX2) + " " + getString(R.string.xpf);
        taxes.add(TAX2);


        // Total TTC
        Integer TOT_TTC = InvoiceController.getTotalTTC(invoice);
        if (Invoice.AVOIR == mInvoice.getType()) {
            TOT_TTC = -TOT_TTC;
        }
        String[] TTC = new String[2];
        TTC[0] = getString(R.string.tot_ttc);
        TTC[1] = String.format("%,d", TOT_TTC) + " " + getString(R.string.xpf);
        taxes.add(TTC);

        return taxes;
    }

    public interface FragmentPrintInvoiceListener {
        void onChangeLastInvoice(View view, Integer invoiceId);
        void onPrint(View view, Integer invoiceId);
    }
}