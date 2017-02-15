package ovh.snacking.snacking.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.CustomerAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Customer;

/**
 * Created by ACER on 07/02/2017.
 */

public class CustomerListFragment extends Fragment {

    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        RealmResults<Customer> customers = realm.where(Customer.class).findAllSorted("name");
        CustomerListFragment.CustomerRecyclerViewAdapter mCustomerAdapter = new CustomerListFragment.CustomerRecyclerViewAdapter(getContext(), customers);
        recyclerView.setAdapter(mCustomerAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Manage the visibility of the fab
        ((FloatingActionButton) getActivity().findViewById(R.id.fab)).hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }


    public class CustomerRecyclerViewAdapter extends RealmRecyclerViewAdapter<Customer, CustomerListFragment.CustomerRecyclerViewAdapter.ViewHolder> {
        public CustomerRecyclerViewAdapter(Context context, OrderedRealmCollection<Customer> realmResults) {
            super(context, realmResults, true);
        }

        @Override
        public CustomerListFragment.CustomerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_customer_item, parent, false);
            return new CustomerListFragment.CustomerRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomerListFragment.CustomerRecyclerViewAdapter.ViewHolder holder, int position) {
            Customer customer = getData().get(position);
            holder.imgItem.setImageResource(R.drawable.ic_person_black_24dp);
            holder.customerName.setText(String.valueOf(customer.getName()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final ImageView imgItem;
            private final TextView customerName;

            public ViewHolder(View view) {
                super(view);
                imgItem = (ImageView) view.findViewById(R.id.recycler_view_customer_item_image);
                customerName = (TextView) view.findViewById(R.id.recycler_view_customer_item_name);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                //TODO pouvoir changer la position dans le groupe
            }
        }
    }
}
