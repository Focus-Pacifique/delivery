package com.focus.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import com.focus.delivery.R;
import com.focus.delivery.model.DolibarrInvoice;

/**
 * Created by Alex on 09/02/2017.
 */

public class AdapterDolibarrInvoiceStatement extends RealmBaseAdapter<DolibarrInvoice> implements ListAdapter {
    private Realm realm;

    public AdapterDolibarrInvoiceStatement(OrderedRealmCollection<DolibarrInvoice> realmResults) {
        super(realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdapterDolibarrInvoiceStatement.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.print_invoice_statement_line, parent, false);
            viewHolder = new AdapterDolibarrInvoiceStatement.ViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.dolibarr_invoice_date);
            viewHolder.ref = (TextView) convertView.findViewById(R.id.dolibarr_invoice_ref);
            viewHolder.ref_client = (TextView) convertView.findViewById(R.id.customer_invoice_ref);
            viewHolder.tot_ttc = (TextView) convertView.findViewById(R.id.dolibarr_invoice_tot_ttc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AdapterDolibarrInvoiceStatement.ViewHolder) convertView.getTag();
        }

        final DolibarrInvoice item = adapterData.get(position);

        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");

        viewHolder.date.setText(String.valueOf(simpleDate.format(item.getDate())));
        viewHolder.ref.setText(String.valueOf(item.getRef()));
        String ref_client = item.getRef_client();
        if (ref_client != null ) {
            viewHolder.ref_client.setText(String.valueOf(ref_client));
        }
        viewHolder.tot_ttc.setText(String.valueOf(item.getTotal_ttc().intValue()));

        return convertView;
    }

    private static class ViewHolder {
        TextView date;
        TextView ref;
        TextView ref_client;
        TextView tot_ttc;
    }
}
