package ovh.snacking.snacking.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.model.Product;

/**
 * Created by Alex on 11/11/2016.
 */

public class ProductTwoLinesListAdapter extends RealmBaseAdapter<Product> implements ListAdapter {

    public ProductTwoLinesListAdapter(OrderedRealmCollection<Product> realmResults) {
        super(realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductTwoLinesListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new ProductTwoLinesListAdapter.ViewHolder();
            viewHolder.ref = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.label = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ProductTwoLinesListAdapter.ViewHolder) convertView.getTag();
        }

        Product item = adapterData.get(position);
        viewHolder.ref.setText(item.getRef());
        viewHolder.label.setText(item.getLabel());
        return convertView;
    }

    private static class ViewHolder {
        TextView ref;
        TextView label;
    }
}
