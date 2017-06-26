package ovh.snacking.snacking.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.CustomerAdapter;
import ovh.snacking.snacking.controller.adapter.CustomerTouchHelperAdapter;
import ovh.snacking.snacking.controller.adapter.ItemTouchHelperCallback;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.CustomerAndGroupBinding;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 07/02/2017.
 *
 * Fragment to mangage customers into a group
 *
 */

public class CustomerGroupDetailsFragment extends Fragment implements CustomerTouchHelperAdapter.OnStartDragListener {

    public static String ARG_GROUP_POSITION = "position";

    private Realm realm;
    private RecyclerView mRecyclerView;
    private CustomerTouchHelperAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private FloatingActionButton fab;
    private CustomerGroup mGroup;

    public static CustomerGroupDetailsFragment newInstance(int position) {
        CustomerGroupDetailsFragment frag = new CustomerGroupDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_GROUP_POSITION, position);
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mGroup = realm.where(CustomerGroup.class).equalTo(CustomerGroup.FIELD_POSITION, getArguments().getInt(ARG_GROUP_POSITION)).findFirst();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpRecyclerView();


        //RealmResults<CustomerAndGroupBinding> customers = realm.where(CustomerAndGroupBinding.class).equalTo("group.id", mGroup.getId()).findAllSorted("position");
        //CustomerRecyclerViewAdapter mCustomerAdapter = new CustomerRecyclerViewAdapter(customers);
        //recyclerView.setAdapter(mCustomerAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddCustomerToGroup();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //Title
        ((MainActivity) getActivity()).setActionBarTitle(String.valueOf(mGroup.getName()));

        fab.setImageResource(R.drawable.ic_create_white_24dp);
        fab.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        realm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new CustomerTouchHelperAdapter(mGroup.getCustomers(), this, realm);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration( new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void dialogAddCustomerToGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setAdapter(new CustomerAdapter(realm.where(Customer.class).findAll()), null);

        AlertDialog ad = builder.create();
        ad.getListView().setItemsCanFocus(false);
        ad.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ad.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Manage selected items here
                final Customer customer = (Customer) parent.getItemAtPosition(position);
                mAdapter.i
                /*realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        CustomerAndGroupBinding bind = realm.createObject(CustomerAndGroupBinding.class, nextCustomerAndGroupBindingId());
                        bind.setCustomer(customer);
                        bind.setGroup(mGroup);
                        //bind.setPosition(nextCustomerAndGroupBindingPosition(mGroup));
                    }
                });*/
            }
        });

        ad.show();
    }

    private boolean removeCustomerFromGroup(final CustomerAndGroupBinding bind) {
        /*realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Set correct position of customer
                RealmResults<CustomerAndGroupBinding> lines = realm.where(CustomerAndGroupBinding.class)
                        .equalTo("group.id", bind.getGroup().getId())
                        .greaterThan("position", bind.getPosition())
                        .findAll();
                for (CustomerAndGroupBinding line : lines) {
                    line.setPosition(line.getPosition() - 1);
                }

                //Delete the line
                bind.deleteFromRealm();
            }
        });*/
        return true;
    }

    private Integer nextCustomerAndGroupBindingId() {
        return realm.where(CustomerAndGroupBinding.class).findFirst() != null ? (realm.where(CustomerAndGroupBinding.class).max("id").intValue() + 1) : 1;
    }

    /*private Integer nextCustomerAndGroupBindingPosition(CustomerGroup group) {
        //return realm.where(CustomerAndGroupBinding.class).equalTo("group.id", group.getId()).findAll().size();
    }*/

    /*public interface OnCustomerOfGroupListener {
        void onBackPressed();
    }*/

    public class CustomerRecyclerViewAdapter extends RealmRecyclerViewAdapter<CustomerAndGroupBinding, CustomerRecyclerViewAdapter.ViewHolder> {
        public CustomerRecyclerViewAdapter(OrderedRealmCollection<CustomerAndGroupBinding> realmResults) {
            super(realmResults, true);
        }

        @Override
        public CustomerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
            return new CustomerRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomerRecyclerViewAdapter.ViewHolder holder, int position) {
            CustomerAndGroupBinding bind = getData().get(position);
            holder.imgItem.setImageResource(R.drawable.ic_person_black_24dp);
            holder.customerName.setText(String.valueOf(bind.getCustomer().getName()));
            holder.customerPosition.setText(String.valueOf(bind.getPosition()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
            private final ImageView imgItem;
            private final TextView customerName;
            private final TextView customerPosition;

            public ViewHolder(View view) {
                super(view);
                imgItem = (ImageView) view.findViewById(R.id.customer_image);
                customerName = (TextView) view.findViewById(R.id.customer_name);
                customerPosition = (TextView) view.findViewById(R.id.customer_position);
                view.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                removeCustomerFromGroup(getItem(getAdapterPosition()));
                return true;
            }

        }
    }
}
