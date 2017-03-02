package ovh.snacking.snacking.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import io.realm.Realm;
import ovh.snacking.snacking.NumberToWords.FrenchNumberToWords;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.PrintInvoiceAdapter;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 19/11/2016.
 */

public class PrintInvoiceFragment extends Fragment {

    OnPrintListener mListener;
    private Integer invoiceId = 0;
    private Invoice mInvoice;
    private Toolbar toolbar;
    private Realm realm;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPrintListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnInvoiceSelectedListener");
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
        if (Invoice.TERMINEE.equals(mInvoice.getState())) {
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

        // Total
        final TextView tot_ht = (TextView) layout.findViewById(R.id.tot_ht);
        final TextView tss = (TextView) layout.findViewById(R.id.tss);
        final TextView tot_ttc = (TextView) layout.findViewById(R.id.tot_ttc);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                Integer TOT_HT = mInvoice.getTotalHT();
                Integer TSS = mInvoice.getTotalTSS();
                Integer TOT_TTC = mInvoice.getTotalTTC();
                if (Invoice.AVOIR.equals(mInvoice.getType())) {
                    TOT_HT = -TOT_HT;
                    TSS = -TSS;
                    TOT_TTC = -TOT_TTC;
                }
                tot_ht.setText(String.format("%,d", TOT_HT) + " XPF");
                tss.setText(String.format("%,d", TSS) + " XPF");
                tot_ttc.setText(String.format("%,d", TOT_TTC) + " XPF");

                // Convert number to words
                TextView tot_words = (TextView) layout.findViewById(R.id.tot_words);
                String total_in_words;
                if (Invoice.AVOIR.equals(mInvoice.getType())) {
                    total_in_words = "- (moins) " + FrenchNumberToWords.convert(-TOT_TTC);
                } else {
                    total_in_words = FrenchNumberToWords.convert(TOT_TTC);
                }
                total_in_words += " francs CFP";
                tot_words.setText(total_in_words);
            }
        });


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
        if (!mInvoice.isPOSTToDolibarr() && mInvoice.getId() == realm.where(Invoice.class).max("id").intValue() && Invoice.TERMINEE.equals(mInvoice.getState())) {
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
        // Back arrow in the menu
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPrintBack();
            }
        });

        ((FloatingActionButton) getActivity().findViewById(R.id.fab)).hide();

        if (mInvoice.getCounterPrint() > 0) {
            ((MainActivity) getActivity()).setActionBarTitle("Imprimée (" + mInvoice.getCounterPrint() + ") fois");
        } else {
            if (Invoice.FACTURE.equals(mInvoice.getType())) {
                ((MainActivity) getActivity()).setActionBarTitle("Facture à imprimer");
            } else if (Invoice.AVOIR.equals(mInvoice.getType())) {
                ((MainActivity) getActivity()).setActionBarTitle("Avoir à imprimer");
            } else {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_print));
            }
        }
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

    public interface OnPrintListener {
        void onPrintBack();

        boolean onFinishInvoice(Integer invoiceId);

        void onChangeLastInvoice(View view, Integer invoiceId);

        void onPrint(View view, Integer invoiceId);
    }

}