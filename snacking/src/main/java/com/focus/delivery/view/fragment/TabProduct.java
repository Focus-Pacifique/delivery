package com.focus.delivery.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.focus.delivery.R;
import com.focus.delivery.adapter.SectionExpandableProduct;
import com.focus.delivery.interfaces.FilterableList;
import com.focus.delivery.model.Product;
import com.focus.delivery.model.ProductGroup;
import com.focus.delivery.util.LibUtil;
import com.focus.delivery.util.RealmSingleton;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Alex on 07/02/2017.
 */

public class TabProduct extends Fragment
        implements SectionExpandableProduct.SectionExpandableProductListener,
        SearchView.OnQueryTextListener {

    private Realm realm;
    private SectionedRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;                 // SearchView
    private String mQuery = "";                     // Search query
    private TabProductListener mListener;

    public interface TabProductListener {
        void addProductToInvoice(Product product, int qty);
    }

    public static TabProduct newInstance() {
        TabProduct frag = new TabProduct();

        /*Bundle bundle = new Bundle();
        bundle.putInt(Invoice.FIELD_ID, idInvoice);
        frag.setArguments(bundle);*/

        return frag;
    }

    public void setListener(Fragment fragment) {
        try {
            mListener = (TabProductListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement TabProductListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        // Show menu
        setHasOptionsMenu(true);

        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView() {
        // Create an instance of SectionedRecyclerViewAdapter
        mAdapter = new SectionedRecyclerViewAdapter();

        // Populate sections
        RealmResults<ProductGroup> productGroups = realm.where(ProductGroup.class).findAllSorted(ProductGroup.FIELD_POSITION);
        //Manage empty customers in groups
        if (realm.where(ProductGroup.class).findFirst() == null) {
            mAdapter.addSection(new SectionExpandableProduct(mAdapter, "Tous les produits", realm.where(Product.class).findAllSorted(Product.FIELD_REF), this, true));
        } else {
            for (ProductGroup group : productGroups) {
                if (group.getProducts().size() > 0) {
                    mAdapter.addSection(new SectionExpandableProduct(mAdapter, group.getName(), group.getProducts(), this, true));
                }
            }
        }

        // Thumbs per row view
        final int thumbPerRow = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PreferencesFragment.PREF_APPLICATION_PRODUCT_PER_ROW, ""));
        GridLayoutManager glm = new GridLayoutManager(getContext(), thumbPerRow);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(mAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return thumbPerRow;
                    default:
                        return 1;
                }
            }
        });

        mRecyclerView.setLayoutManager(glm);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public void onProductSelected(Product product) {
        displayDialogProductQuantity(product);
    }

    public void displayDialogProductQuantity(final Product prod) {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_product_quantity)
                .setView(et)
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(LibUtil.parseEditTextInput(et) != 0)
                            mListener.addProductToInvoice(prod, LibUtil.parseEditTextInput(et));
                    }
                });

        final AlertDialog ad = builder.create();
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(LibUtil.parseEditTextInput(et) != 0)
                        mListener.addProductToInvoice(prod, LibUtil.parseEditTextInput(et));
                    ad.dismiss();
                    return true;
                }
                return false;
            }
        });

        ad.show();
    }

    /**
     *  Menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        getActivity().getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Configure the search info and add any event listeners
        mSearchView.setQueryHint(getString(R.string.hint_search_product));
        mSearchView.setOnQueryTextListener(this);

        // Set the old search query
        mSearchView.setQuery(mQuery, false);

        // Display the search icon or the query if it is not empty
        if (mQuery.isEmpty()) {
            mSearchView.setIconified(true);
        } else
            mSearchView.setIconified(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        // To hide the soft keyboard
        mSearchView.clearFocus();
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
