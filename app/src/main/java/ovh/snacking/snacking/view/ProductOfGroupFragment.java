package ovh.snacking.snacking.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import ovh.snacking.snacking.controller.ProductTwoLinesListAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.ProductAndGroupBinding;
import ovh.snacking.snacking.model.ProductGroup;

/**
 * Created by Alex on 07/02/2017.
 */

public class ProductOfGroupFragment extends Fragment {

    private Realm realm;
    private FloatingActionButton fab;
    private ProductGroup mGroup;

    public void setProductGroup(ProductGroup mGroup) {
        this.mGroup = mGroup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL));

        RealmResults<ProductAndGroupBinding> products = realm.where(ProductAndGroupBinding.class).equalTo("group.id", mGroup.getId()).findAllSorted("position");
        ProductRecyclerViewAdapter mCustomerAdapter = new ProductRecyclerViewAdapter(getContext(), products);
        recyclerView.setAdapter(mCustomerAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddProductToGroup();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_product_of_group);
        fab.setImageResource(R.drawable.ic_new);
        fab.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void dialogAddProductToGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.dialog_choose_products)
                .setAdapter(new ProductTwoLinesListAdapter(getContext(), realm.where(Product.class).findAll()), null);

        AlertDialog ad = builder.create();
        ad.getListView().setItemsCanFocus(false);
        ad.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ad.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Manage selected items here
                final Product product = (Product) parent.getItemAtPosition(position);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ProductAndGroupBinding bind = realm.createObject(ProductAndGroupBinding.class, nextProductAndGroupBindingId());
                        bind.setProduct(product);
                        bind.setGroup(mGroup);
                        bind.setPosition(nextProductAndGroupBindingPosition(mGroup));
                    }
                });
            }
        });

        ad.show();
    }

    private boolean removeProductFromGroup(final ProductAndGroupBinding bind) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Set correct position of customer
                RealmResults<ProductAndGroupBinding> lines = realm.where(ProductAndGroupBinding.class)
                        .equalTo("group.id", bind.getGroup().getId())
                        .greaterThan("position", bind.getPosition())
                        .findAll();
                for (ProductAndGroupBinding line : lines) {
                    line.setPosition(line.getPosition() - 1);
                }

                //Delete the line
                bind.deleteFromRealm();
            }
        });
        return true;
    }

    private Integer nextProductAndGroupBindingId() {
        if(null != realm.where(ProductAndGroupBinding.class).findFirst()) {
            Integer nextId = realm.where(ProductAndGroupBinding.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }

    private Integer nextProductAndGroupBindingPosition(ProductGroup group) {
        return realm.where(ProductAndGroupBinding.class).equalTo("group.id", group.getId()).findAll().size();
    }

    public class ProductRecyclerViewAdapter extends RealmRecyclerViewAdapter<ProductAndGroupBinding, ProductRecyclerViewAdapter.ViewHolder> {
        public ProductRecyclerViewAdapter(Context context, OrderedRealmCollection<ProductAndGroupBinding> realmResults) {
            super(context, realmResults, true);
        }

        @Override
        public ProductRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_customer_item, parent, false);
            return new ProductRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ProductRecyclerViewAdapter.ViewHolder holder, int position) {
            ProductAndGroupBinding bind = getData().get(position);
            holder.imgItem.setImageResource(R.drawable.ic_person_black_24dp);
            holder.productName.setText(String.valueOf(bind.getProduct().getRef()));
            holder.productPosition.setText(String.valueOf(bind.getPosition()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
            private final ImageView imgItem;
            private final TextView productName;
            private final TextView productPosition;

            public ViewHolder(View view) {
                super(view);
                imgItem = (ImageView) view.findViewById(R.id.recycler_view_customer_item_image);
                productName = (TextView) view.findViewById(R.id.recycler_view_customer_item_name);
                productPosition = (TextView) view.findViewById(R.id.recycler_view_customer_item_position);
                view.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                removeProductFromGroup(getItem(getAdapterPosition()));
                return true;
            }

        }
    }
}
