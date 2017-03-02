package ovh.snacking.snacking.view.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.CustomExpandableListAdapter;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.ProductGroup;

/**
 * Created by Pc on 11/11/2016.
 */

public class TabFragment1 extends android.support.v4.app.Fragment {

    private Realm realm;
    private long mLastClickTime = 0;
    private CustomExpandableListAdapter mAdapter;
    private ExpandableListView mList;
    private ArrayList<CustomExpandableListAdapter.ExpandableListGroup> mListItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_expandable_list, container, false);
        mList = (ExpandableListView) layout.findViewById(R.id.exp_listview);
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        setListItems();
        mAdapter = new CustomExpandableListAdapter(getContext(), mListItems);
        mList.setAdapter(mAdapter);

        // Expand the header groups by default
        setExpandedGroups();

        mList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                final Product selectedProd = (Product)parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                ((EditingInvoiceFragment) getParentFragment()).onProductSelected(selectedProd);

                /*final EditText et = new EditText(getContext());

                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.dialog_title_product_quantity)
                        .setView(et)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!et.getText().toString().isEmpty()) {
                                    final Integer input = Integer.parseInt(et.getText().toString());
                                    if (input >0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                Integer invoiceId = ((EditingInvoiceFragment) getParentFragment()).getInvoiceId();
                                                Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
                                                Line existingLine = invoice.getLines().where().equalTo("prod.id", selectedProd.getId()).findFirst();
                                                if (null != existingLine) {
                                                    existingLine.addQuantity(input);
                                                } else {
                                                    Line line = realm.createObject(Line.class, nextLineId());
                                                    line.setQty(input);
                                                    line.setProd(selectedProd);

                                                    // Add custom price if exist
                                                    ProductCustomerPriceDolibarr customProdPrice = realm.where(ProductCustomerPriceDolibarr.class).equalTo("fk_soc", invoice.getCustomer().getId()).equalTo("fk_product", selectedProd.getId()).findFirst();
                                                    if (customProdPrice != null) {
                                                        line.setSubprice(customProdPrice.getPrice().intValue());
                                                    } else {
                                                        line.setSubprice(selectedProd.getPrice().intValue());
                                                    }

                                                    // Compute ht, tss and ttc
                                                    line.updatePrices();

                                                    // Add line
                                                    invoice.getLines().add(line);
                                                }
                                                Toast.makeText(getContext(), input + " " + selectedProd.getLabel() + " ajoutés", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        dialog.dismiss();
                                    }
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog ad = builder.create();
                ad.show();*/

                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setListItems() {
        ArrayList<CustomExpandableListAdapter.ExpandableListGroup> listGroups = new ArrayList<>();
        ArrayList<CustomExpandableListAdapter.ExpandableListChild> listChildren;
        ArrayList<Product> listChildrenProd;

        CustomExpandableListAdapter.ExpandableListGroup group;
        RealmResults<ProductGroup> groupProds = realm.where(ProductGroup.class).findAllSorted("position");
        for (ProductGroup gr : groupProds) {
            group = new CustomExpandableListAdapter.ExpandableListGroup();
            group.setName(gr.getName());

            // Add child to group
            /*listChildren = new ArrayList<>();
            CustomExpandableListAdapter.ExpandableListChild child;
            RealmResults<Product> products = realm.where(Product.class).equalTo("group.id", gr.getId()).findAllSorted("ref");
            for (Product prod : products) {
                child = new CustomExpandableListAdapter.ExpandableListChild();
                child.setName(prod.getLabel());
                child.setTag(prod.getRef());
                listChildren.add(child);
            }*/
            listChildrenProd = new ArrayList<>();
            Product child;
            RealmResults<Product> products = realm.where(Product.class).equalTo("group.id", gr.getId()).findAllSorted("ref");
            for (Product prod : products) {
                child = prod;
                listChildrenProd.add(child);
            }
            group.setItems(listChildrenProd);
            listGroups.add(group);
        }
        mListItems = listGroups;
    }

    private void setExpandedGroups() {
            int size = realm.where(ProductGroup.class).findAll().size();
            switch (size) {
                default:
                    mList.expandGroup(0, true);
                    break;
                case 2:
                    mList.expandGroup(0, true);
                    mList.expandGroup(1, true);
                    break;
                case 3:
                    mList.expandGroup(0, true);
                    mList.expandGroup(1, true);
                    mList.expandGroup(2, true);
                    break;
                case 4:
                    mList.expandGroup(0, true);
                    mList.expandGroup(1, true);
                    mList.expandGroup(2, true);
                    mList.expandGroup(3, true);
                    break;
            }
    }

}
