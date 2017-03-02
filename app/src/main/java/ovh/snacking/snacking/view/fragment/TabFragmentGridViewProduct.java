package ovh.snacking.snacking.view.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.ProductGridAdapter;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.Product;

/**
 * Created by Pc on 14/11/2016.
 */

public class TabFragmentGridViewProduct extends Fragment {

    RealmResults<Product> products;
    private Realm realm;
    private long mLastClickTime = 0;
    private GridView mGridView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
        products = realm.where(Product.class).findAllSorted("ref");
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_grid_view_product, container, false);
        mGridView = (GridView) layout.findViewById(R.id.grid_view_products);
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        ProductGridAdapter productGridAdapter = new ProductGridAdapter(getContext(), products);
        mGridView.setAdapter(productGridAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                ((EditingInvoiceFragment) getParentFragment()).onProductSelected((Product)parent.getItemAtPosition(position));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}
