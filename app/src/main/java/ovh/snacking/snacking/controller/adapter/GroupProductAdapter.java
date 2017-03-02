package ovh.snacking.snacking.controller.adapter;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.model.ProductGroup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by Alex on 29/01/2017.
 */

public class GroupProductAdapter extends RealmBaseAdapter<ProductGroup> implements ListAdapter {

    public GroupProductAdapter(Context context, OrderedRealmCollection<ProductGroup> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GroupProductAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new GroupProductAdapter.ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.position = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupProductAdapter.ViewHolder) convertView.getTag();
        }

        final ProductGroup item = adapterData.get(position);

        viewHolder.name.setText(String.valueOf(item.getName()));
        viewHolder.position.setText(String.valueOf(item.getPosition()));

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView position;
    }
}
