package com.focus.delivery.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import io.realm.Realm;
import com.focus.delivery.R;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.model.Line;
import com.focus.delivery.model.Product;
import com.focus.delivery.model.ProductCustomerPriceDolibarr;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.activity.MainActivity;

public class TabFragmentEditInvoice extends Fragment
    implements TabProduct.TabProductListener {

    static final Integer NUM_TABS = 2;
    private TabFragmentEditInvoiceListener mListener;
    private Realm realm;
    private Invoice mInvoice;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FloatingActionButton fab;

    public interface TabFragmentEditInvoiceListener {
        void onShowInvoice(Integer invoiceId);
    }

    public static TabFragmentEditInvoice newInstance(int idInvoice) {
        TabFragmentEditInvoice frag = new TabFragmentEditInvoice();

        Bundle bundle = new Bundle();
        bundle.putInt(Invoice.FIELD_ID, idInvoice);
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (TabFragmentEditInvoiceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TabFragmentEditInvoiceListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tabs_invoice, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) layout.findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the tabs with viewPager
        TabLayout tabLayout = (TabLayout) layout.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Set up the fab
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        mInvoice = realm.where(Invoice.class).equalTo(Invoice.FIELD_ID, getArguments().getInt(Invoice.FIELD_ID)).findFirst();

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onShowInvoice(mInvoice.getId());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // fab to print the print_invoice_fragment
        fab.setImageResource(R.drawable.ic_print_white_24dp);
        fab.show();

        //Title
        ((MainActivity) getActivity()).setActionBarTitle(mInvoice.getCustomer().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        fab.hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    @Override
    public void addProductToInvoice(final Product product, final int qty) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Line existingLine = mInvoice.getLines().where().equalTo("prod.id", product.getId()).findFirst();
                if (null != existingLine) {
                    existingLine.addQuantity(qty);
                } else {
                    Line line = realm.createObject(Line.class, nextLineId());
                    line.setQty(qty);
                    line.setProd(product);

                    // Add custom price if exist
                    ProductCustomerPriceDolibarr customProdPrice = realm.where(ProductCustomerPriceDolibarr.class).equalTo("fk_soc", mInvoice.getCustomer().getId()).equalTo("fk_product", product.getId()).findFirst();
                    if (customProdPrice != null && customProdPrice.getPrice_HT() != null) {
                        line.setSubprice(customProdPrice.getPrice_HT());
                    } else {
                        line.setSubprice(product.getPrice());
                    }

                    // Compute ht, tss and ttc
                    line.updatePrices();

                    // Add line
                    mInvoice.getLines().add(line);
                }
            }
        });

        Toast.makeText(getContext(), qty + " " + product.getLabel() + " ajoutés", Toast.LENGTH_SHORT).show();
    }

    private Integer nextLineId() {
        if(null != realm.where(Line.class).findFirst()) {
            return realm.where(Line.class).max("id").intValue() + 1;
        } else {
            return 1;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    TabProduct tabProduct = TabProduct.newInstance();
                    tabProduct.setListener(TabFragmentEditInvoice.this);
                    return tabProduct;
                case 1:
                    return TabEditInvoice.newInstance(getArguments().getInt(Invoice.FIELD_ID));
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "PRODUITS";
                case 1:
                    return "FACTURE";
            }
            return null;
        }
    }
}
