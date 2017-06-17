package ovh.snacking.snacking.view.fragment;

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
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.ProductCustomerPriceDolibarr;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

public class EditingInvoiceFragment extends Fragment {

    static final Integer NUM_TABS = 2;
    OnEditInvoiceListener mListener;
    private Realm realm;
    private Integer invoiceId = 0;
    private Invoice mInvoice;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FloatingActionButton fab;

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnEditInvoiceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnInvoiceSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        realm = RealmSingleton.getInstance(getContext()).getRealm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tabs_invoice, container, false);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) layout.findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the tabs with viewPager
        TabLayout tabLayout = (TabLayout) layout.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Set up the fab
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onShowInvoice(invoiceId);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mInvoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        // Back arrow in the menu
        /*toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackToManageInvoices();
            }
        });*/

        // fab to print the print_invoice_fragment
        fab.setImageResource(R.drawable.ic_print_white_24dp);
        fab.show();

        ((MainActivity) getActivity()).setActionBarTitle(mInvoice.getCustomer().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        // Back arrow in the menu
        //toolbar.setNavigationIcon(null);
        //toolbar.setNavigationOnClickListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void onProductSelected(final Product prod) {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_product_quantity)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addInvoiceLine(parseEditTextInput(et), prod);
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog ad = builder.create();

        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addInvoiceLine(parseEditTextInput(et), prod);
                    ad.dismiss();
                    return true;
                }
                return false;
            }
        });

        ad.setView(et);
        ad.setCanceledOnTouchOutside(true);
        ad.show();
    }

    private Integer parseEditTextInput(EditText et) {
        if (!et.getText().toString().isEmpty()) {
            final Integer input = Integer.parseInt(et.getText().toString());
            if (input > 0) {
                return input;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void addInvoiceLine(final Integer qty, final Product prod) {
        if (qty != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    final Line existingLine = mInvoice.getLines().where().equalTo("prod.id", prod.getId()).findFirst();
                    if (null != existingLine) {
                        existingLine.addQuantity(qty);
                    } else {
                        Line line = realm.createObject(Line.class, nextLineId());
                        line.setQty(qty);
                        line.setProd(prod);

                        // Add custom price if exist
                        ProductCustomerPriceDolibarr customProdPrice = realm.where(ProductCustomerPriceDolibarr.class).equalTo("fk_soc", mInvoice.getCustomer().getId()).equalTo("fk_product", prod.getId()).findFirst();
                        if (customProdPrice != null && customProdPrice.getPrice_HT() != null) {
                            line.setSubprice(customProdPrice.getPrice_HT());
                        } else {
                            line.setSubprice(prod.getPrice());
                        }

                        // Compute ht, tss and ttc
                        line.updatePrices();

                        // Add line
                        mInvoice.getLines().add(line);
                    }
                }
            });
            Toast.makeText(getContext(), qty + " " + prod.getLabel() + " ajoutés", Toast.LENGTH_SHORT).show();
        }
    }

    private Integer nextLineId() {
        if(null != realm.where(Line.class).findFirst()) {
            return realm.where(Line.class).max("id").intValue() + 1;
        } else {
            return 1;
        }
    }

    public interface OnEditInvoiceListener {
        //void onBackToManageInvoices();

        void onShowInvoice(Integer invoiceId);
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
                    return new TabFragmentProduct();
                case 1:
                    return new TabFragmentEditInvoice();
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
