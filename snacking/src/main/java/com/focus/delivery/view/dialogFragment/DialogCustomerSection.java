package com.focus.delivery.view.dialogFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import com.focus.delivery.R;
import com.focus.delivery.adapter.SectionExpandableCustomer;
import com.focus.delivery.interfaces.FilterableList;
import com.focus.delivery.model.Customer;
import com.focus.delivery.model.CustomerGroup;
import com.focus.delivery.util.RealmSingleton;

/**
 * Created by Alex on 17/11/2016.
 */

public class DialogCustomerSection extends AppCompatDialogFragment
        implements SectionExpandableCustomer.SectionExpandableCustomerListener,
        SearchView.OnQueryTextListener {

    private DialogCustomerSectionListener mListener;
    private Realm realm;
    private SectionedRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;  // SearchView
    private String mQuery = "";                     // Search query


    public interface DialogCustomerSectionListener {
        void onCustomerSelected(Customer customer);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogCustomerSectionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogCustomerSectionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_customer_section, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpRecyclerView();

        // Search view
        mSearchView = (SearchView) view.findViewById(R.id.searchView);
        // Configure the search info and add any event listeners
        mSearchView.setQueryHint(getString(R.string.hint_search_product));
        mSearchView.setOnQueryTextListener(this);

        // Set the old search query
        mSearchView.setQuery(mQuery, false);

        return view;
    }

    private void setUpRecyclerView() {
        // Create an instance of SectionedRecyclerViewAdapter
        mAdapter = new SectionedRecyclerViewAdapter();

        // Populate sections
        RealmResults<CustomerGroup> customerGroups = realm.where(CustomerGroup.class).findAllSorted(CustomerGroup.FIELD_POSITION);

        //Manage empty customers in groups
        if (realm.where(CustomerGroup.class).findFirst() == null) {
            mAdapter.addSection(new SectionExpandableCustomer(mAdapter, "Tous les clients", realm.where(Customer.class).findAllSorted(Customer.FIELD_NAME), this, true));
        } else {
            for (CustomerGroup group : customerGroups) {
                if (group.getCustomers().size() > 0) {
                    mAdapter.addSection(new SectionExpandableCustomer(mAdapter, group.getName(), group.getCustomers(), this, true));
                }
            }
        }

        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // To hide the soft keyboard
        mSearchView.clearFocus();
        mRecyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public void onCustomerSelected(Customer customer) {
        mListener.onCustomerSelected(customer);
        dismiss();
    }

    /**
     * Listen for events on SearchView item. Called in 'onCreateOptionsMenu()'.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        mQuery = query;
        for (Section section : mAdapter.getSectionsMap().values()) {
            if (section instanceof FilterableList) {
                ((FilterableList) section).filter(query);
            }
        }
        mAdapter.notifyDataSetChanged();
        return true;
    }
}