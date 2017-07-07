package com.focus.delivery.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import com.focus.delivery.R;
import com.focus.delivery.adapter.AdapterInvoiceLine;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.util.RealmSingleton;

/**
 * Created by Alex on 14/11/2016.
 */

public class TabFragmentEditInvoice extends Fragment {

    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Integer invoiceId = ((EditingInvoiceFragment) getParentFragment()).getInvoiceId();
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        View layout = inflater.inflate(R.layout.fragment_edit_invoice, container, false);

        TextView text_view_invoice = (TextView) layout.findViewById(R.id.text_view_invoice);
        if (Invoice.AVOIR == invoice.getType()) {
            text_view_invoice.setText(R.string.invoice_type_avoir);
            text_view_invoice.setTextColor(Color.DKGRAY);
        } else {
            text_view_invoice.setText(R.string.invoice_type_facture);
            text_view_invoice.setTextColor(Color.BLUE);
        }

        // Set invoice line adapter to recycler view
        AdapterInvoiceLine adapter = new AdapterInvoiceLine(getContext(), invoice.getLines(), invoice.getType(), AdapterInvoiceLine.VIEW_EDIT);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerViewInvoiceLines);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
