package ovh.snacking.snacking.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import ovh.snacking.snacking.model.Customer;

/**
 * Created by Alex on 21/10/2016.
 *
 * Adapter to get a list of the customer's names
 *
 */

public class CustomerAdapter extends RealmBaseAdapter<Customer> implements ListAdapter {
    public CustomerAdapter(OrderedRealmCollection<Customer> realmResults) {
        super(realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomerAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_selectable_list_item, parent, false);
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

    private static class ViewHolder {
        TextView name;
    }
}
