package ovh.snacking.snacking.controller;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;

/**
 * Created by Alex on 21/10/2016.
 */

public class InvoiceAdapter extends RealmBaseAdapter<Invoice> implements ListAdapter {
    private static class ViewHolder {
        TextView customer_name;
        TextView datetime;
        TextView invoice_type;
        TextView invoice_state;
    }

    public InvoiceAdapter(Context context, OrderedRealmCollection<Invoice> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InvoiceAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.selectable_list_invoice, parent, false);
            viewHolder = new InvoiceAdapter.ViewHolder();
            viewHolder.customer_name = (TextView) convertView.findViewById(R.id.customer_name);
            viewHolder.datetime = (TextView) convertView.findViewById(R.id.invoice_datetime);
            viewHolder.invoice_type = (TextView) convertView.findViewById(R.id.invoice_type);
            viewHolder.invoice_state = (TextView) convertView.findViewById(R.id.invoice_state);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Invoice item = adapterData.get(position);

        //Customer ref
        if (null == item.getCustomer()) {
            viewHolder.customer_name.setText(String.valueOf("client pas en base"));
        } else {
            viewHolder.customer_name.setText(String.valueOf(item.getCustomer().getName()));
        }

        //Datetime
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm  dd/MM");
        viewHolder.datetime.setText(simpleDate.format(item.getDate()));

        // Invoice type part
        if (item.getType() == Invoice.FACTURE) {
            viewHolder.invoice_type.setText(R.string.invoice_type_facture);
            viewHolder.invoice_type.setTextColor(Color.BLUE);
        } else {
            viewHolder.invoice_type.setText(R.string.invoice_type_avoir);
            viewHolder.invoice_type.setTextColor(Color.DKGRAY);
        }

        //Invoice state part
        if (Invoice.EN_COURS.equals(item.getState())) {
            viewHolder.invoice_state.setText(R.string.invoice_state_en_cours);
            viewHolder.invoice_state.setTextColor(Color.YELLOW);
        } else if (Invoice.TERMINEE.equals(item.getState())){
            viewHolder.invoice_state.setText(R.string.invoice_state_terminee);
            viewHolder.invoice_state.setTextColor(Color.GREEN);
        }

        return convertView;
    }
}
