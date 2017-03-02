package ovh.snacking.snacking.controller.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Product;

/**
 * Created by Alex on 11/11/2016.
 */

public class ProductGridAdapter extends RealmBaseAdapter<Product> implements ListAdapter {

    public ProductGridAdapter(Context context, OrderedRealmCollection<Product> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductGridAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.selectable_grid_view_product, parent, false);
            viewHolder = new ProductGridAdapter.ViewHolder();
            viewHolder.ref = (TextView) convertView.findViewById(R.id.text_view_product);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ProductGridAdapter.ViewHolder) convertView.getTag();
        }

        Product item = adapterData.get(position);
        viewHolder.ref.setText(item.getRef());
        return convertView;
    }

    private static class ViewHolder {
        TextView ref;
    }
}
