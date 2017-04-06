package ovh.snacking.snacking.controller.print;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.util.RealmSingleton;

/**
 * Created by Pc on 21/11/2016.
 */

public class PrintInvoiceAdapter extends RealmBaseAdapter<Line> implements ListAdapter {

    private Integer invoiceId;
    private Realm realm;

    public PrintInvoiceAdapter(Context context, OrderedRealmCollection<Line> realmResults, Integer invoiceId) {
        super(context, realmResults);
        this.invoiceId = invoiceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PrintInvoiceAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.print_invoice_line, parent, false);
            viewHolder = new PrintInvoiceAdapter.ViewHolder();
            viewHolder.product = (TextView) convertView.findViewById(R.id.print_product);
            viewHolder.price_ht = (TextView) convertView.findViewById(R.id.print_price_ht);
            viewHolder.qty = (TextView) convertView.findViewById(R.id.print_qty);
            viewHolder.tot_ht = (TextView) convertView.findViewById(R.id.print_tot_ht);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PrintInvoiceAdapter.ViewHolder) convertView.getTag();
        }

        realm = RealmSingleton.getInstance(context).getRealm();

        final Line item = adapterData.get(position);

        Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        Integer prodPriceHT = item.getSubprice();
        Integer qty = item.getQty();
        if (Invoice.AVOIR.equals(invoice.getType())) {
            prodPriceHT = -prodPriceHT;
        }
        Integer tot_ht = qty*prodPriceHT;

        viewHolder.product.setText(String.valueOf(item.getProd().getLabel()));
        viewHolder.price_ht.setText(String.format("%,d", prodPriceHT));
        viewHolder.qty.setText(String.format("%,d", item.getQty()));
        viewHolder.tot_ht.setText(String.format("%,d", tot_ht));

        realm.close();

        return convertView;
    }

    private static class ViewHolder {
        TextView product;
        TextView price_ht;
        TextView qty;
        TextView tot_ht;
    }
}
