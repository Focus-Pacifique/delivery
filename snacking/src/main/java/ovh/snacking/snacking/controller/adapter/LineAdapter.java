package ovh.snacking.snacking.controller.adapter;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Line;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by Alex on 11/11/2016.
 */

public class LineAdapter extends RealmBaseAdapter<Line> implements ListAdapter {

    private Integer invoiceId;
    private Realm realm;

    public LineAdapter(Context context, OrderedRealmCollection<Line> realmResults, Integer invoiceId) {
        super(context, realmResults);
        this.invoiceId = invoiceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LineAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.selectable_list_line_invoice, parent, false);
            viewHolder = new LineAdapter.ViewHolder();
            viewHolder.plus = (Button) convertView.findViewById(R.id.plus_btn);
            viewHolder.minus = (Button) convertView.findViewById(R.id.minus_btn);
            viewHolder.quantity = (TextView) convertView.findViewById(R.id.quantity);
            viewHolder.product_name = (TextView) convertView.findViewById(R.id.product_name);
            viewHolder.product_price = (TextView) convertView.findViewById(R.id.product_price);
            viewHolder.delete = (ImageButton) convertView.findViewById(R.id.delete_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (LineAdapter.ViewHolder) convertView.getTag();
        }

        realm = RealmSingleton.getInstance(context).getRealm();

        final Line item = adapterData.get(position);

        viewHolder.quantity.setText(String.valueOf(item.getQty().toString() + " "));
        viewHolder.product_name.setText(String.valueOf(item.getProd().getRef() + " "));
        viewHolder.product_price.setText(String.valueOf("(" + item.getSubprice() + " HT)"));

        // Handle plus button
        viewHolder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        item.addQuantity(1);
                    }
                });
            }
        });

        // Handle minus button
        viewHolder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (item.getQty() > 1) {
                            item.addQuantity(-1);
                        } else {
                            realm.where(Line.class).equalTo("id", item.getId()).findFirst().deleteFromRealm();
                        }
                    }
                });
            }
        });

        // Handle delete button
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(Line.class).equalTo("id", item.getId()).findFirst().deleteFromRealm();
                    }
                });
            }
        });

        realm.close();

        return convertView;
    }

    private static class ViewHolder {
        TextView quantity;
        TextView product_name;
        TextView product_price;
        Button plus;
        Button minus;
        ImageButton delete;
    }
}
