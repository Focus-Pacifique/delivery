package ovh.snacking.snacking.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.CustomerAndGroupBinding;
import ovh.snacking.snacking.model.CustomerGroup;

/**
 * Created by Alex on 17/11/2016.
 */

public class CustomerSectionFragment extends DialogFragment {

    OnCustomerDialogListener mListener;
    private Realm realm;
    private RecyclerView mRecyclerView;
    private SectionedRecyclerViewAdapter mSectionAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnCustomerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCustomerDialogListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        // Create an instance of SectionedRecyclerViewAdapter
        mSectionAdapter = new SectionedRecyclerViewAdapter();

        // Add your Sections
        populateAdapter();

        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mSectionAdapter);
        return view;
    }

    private void populateAdapter() {

        RealmResults<CustomerGroup> customerGroups = realm.where(CustomerGroup.class).findAllSorted("position");

        //Manage empty customers in groups
        if (realm.where(CustomerAndGroupBinding.class).findFirst() == null) {
            CustomerGroup defaultGroup = customerGroups.first();
            mSectionAdapter.addSection(new CustomerSectionFragment.CustomerSection(String.valueOf("Tous les clients"), getAllCustomer()));
        } else {
            for (CustomerGroup group : customerGroups) {
                List<Customer> customers = getCustomerInGroup(group);
                if (customers.size() > 0) {
                    mSectionAdapter.addSection(new CustomerSectionFragment.CustomerSection(String.valueOf(group.getName()), customers));
                }
            }
        }
    }

    private List<Customer> getCustomerInGroup(CustomerGroup group) {
        List<Customer> customers = new ArrayList<>();

        RealmResults<CustomerAndGroupBinding> customerAndGroupBindings = realm.where(CustomerAndGroupBinding.class).equalTo("group.id", group.getId()).findAllSorted("position");
        for (CustomerAndGroupBinding customerAndGroupBinding : customerAndGroupBindings) {
            customers.add(customerAndGroupBinding.getCustomer());
        }

        return customers;
    }

    private List<Customer> getAllCustomer() {
        List<Customer> customers = new ArrayList<>();
        RealmResults<Customer> allCustomer = realm.where(Customer.class).findAllSorted("name");
        for (Customer customer : allCustomer) {
            customers.add(customer);
        }
        return customers;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                mListener.onCustomerSelected(((Customer) parent.getItemAtPosition(position)).getId());
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setTitle(getString(R.string.title_customer_select));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public interface OnCustomerDialogListener {
        void onCustomerSelected(Integer customerId);
    }

    private class CustomerSection extends StatelessSection {

        String mTitle;
        List<Customer> mList;

        public CustomerSection(String title, List<Customer> list) {
            // call constructor with layout resources for this Section header and items
            super(R.layout.section_header, R.layout.section_item_customer);
            this.mTitle = title;
            this.mList = list;
        }

        @Override
        public int getContentItemsTotal() {
            return mList.size(); // number of items of this section
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            // return a custom instance of ViewHolder for the items of this section
            return new CustomerSection.ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

            final CustomerSection.ItemViewHolder itemHolder = (CustomerSection.ItemViewHolder) holder;

            // bind your view here
            final Customer selectedCustomer = mList.get(position);
            itemHolder.tvItem.setText(String.valueOf(selectedCustomer.getName()));
            itemHolder.imgItem.setImageResource(R.drawable.ic_person_black_24dp);

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCustomerSelected(selectedCustomer.getId());
                    dismiss();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new CustomerSection.HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            CustomerSection.HeaderViewHolder headerHolder = (CustomerSection.HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(mTitle);
        }



        private class HeaderViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTitle;

            public HeaderViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.section_header_title);
            }
        }

        private class ItemViewHolder extends RecyclerView.ViewHolder {

            private final View rootView;
            private final ImageView imgItem;
            private final TextView tvItem;

            private ItemViewHolder(View view) {
                super(view);
                rootView = view;
                imgItem = (ImageView) view.findViewById(R.id.section_item_customer_img);
                tvItem = (TextView) view.findViewById(R.id.section_item_customer_name);
            }
        }
    }
}
