package ovh.snacking.snacking.controller.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.DolibarrInvoice;

/**
 * Created by Alex on 09/02/2017.
 */

public class DolibarrInvoiceStatementAdapter extends RealmBaseAdapter<DolibarrInvoice> implements ListAdapter {
    private Realm realm;

    public DolibarrInvoiceStatementAdapter(Context context, OrderedRealmCollection<DolibarrInvoice> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DolibarrInvoiceStatementAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.print_invoice_statement_line, parent, false);
            viewHolder = new DolibarrInvoiceStatementAdapter.ViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.dolibarr_invoice_date);
            viewHolder.ref = (TextView) convertView.findViewById(R.id.dolibarr_invoice_ref);
            viewHolder.ref_client = (TextView) convertView.findViewById(R.id.customer_invoice_ref);
            viewHolder.tot_ttc = (TextView) convertView.findViewById(R.id.dolibarr_invoice_tot_ttc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DolibarrInvoiceStatementAdapter.ViewHolder) convertView.getTag();
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
