package ovh.snacking.snacking.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.model.Customer;

/**
 * Created by Alex on 21/10/2016.
 */

public class CustomerAdapter extends RealmBaseAdapter<Customer> implements ListAdapter {
    private static class ViewHolder {
        TextView name;
    }

    public CustomerAdapter(Context context, OrderedRealmCollection<Customer> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomerAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_selectable_list_item, parent, false);
            viewHolder = new CustomerAdapter.ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomerAdapter.ViewHolder) convertView.getTag();
        }

        Customer item = adapterData.get(position);
        viewHolder.name.setText(String.valueOf(item.getName()));
        return convertView;
    }
}
