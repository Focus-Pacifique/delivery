package ovh.snacking.snacking.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.CustomerGroupAdapter;
import ovh.snacking.snacking.controller.adapter.ItemTouchHelperCallback;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 06/02/2017.
 *
 * Fragment to manage group of customers
 *
 */

public class CustomerGroupsFragment extends Fragment implements
        CustomerGroupAdapter.CustomerGroupAdapterListener {

    private CustomerGroupFragmentListener mListener;
    private Realm realm;
    private RecyclerView mRecyclerView;
    private CustomerGroupAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CustomerGroupFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CustomerGroupFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_fragment_group));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        realm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new CustomerGroupAdapter(realm.where(CustomerGroup.class).findAllSorted(CustomerGroup.FIELD_POSITION), this, realm);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration( new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void addCustomerGroup() {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_customer_group)
                .setView(et)
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String input = et.getText().toString();
                        if (!input.isEmpty()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    CustomerGroup group = CustomerGroup.create(realm);
                                    group.setName(input);
                                    mAdapter.notifyItemInserted(group.getPosition());
                                }
                            });
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
        ad.show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onCustomerGroupSelected(CustomerGroup group) {
        mListener.displayCustomersOfGroup(group);
    }

    public interface CustomerGroupFragmentListener {
        void displayCustomersOfGroup(CustomerGroup group);
    }
}
