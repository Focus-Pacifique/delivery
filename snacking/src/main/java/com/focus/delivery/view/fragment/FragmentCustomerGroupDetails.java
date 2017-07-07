package com.focus.delivery.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.focus.delivery.R;
import com.focus.delivery.adapter.AdapterCustomerList;
import com.focus.delivery.adapter.TouchAdapterCustomer;
import com.focus.delivery.adapter.ItemTouchHelperCallback;
import com.focus.delivery.interfaces.OnStartDragListener;
import com.focus.delivery.model.Customer;
import com.focus.delivery.model.CustomerGroup;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.activity.MainActivity;

import io.realm.Realm;

/**
 * Created by Alex on 07/02/2017.
 *
 * Fragment to mangage customers into a group
 *
 */

public class FragmentCustomerGroupDetails extends Fragment implements OnStartDragListener {

    public static String ARG_GROUP_POSITION = "position";

    private Realm realm;
    private RecyclerView mRecyclerView;
    private TouchAdapterCustomer mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private CustomerGroup mGroup;

    public static FragmentCustomerGroupDetails newInstance(int position) {
        FragmentCustomerGroupDetails frag = new FragmentCustomerGroupDetails();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_GROUP_POSITION, position);
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        mGroup = realm.where(CustomerGroup.class).equalTo(CustomerGroup.FIELD_POSITION, getArguments().getInt(ARG_GROUP_POSITION)).findFirst();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView() {
        mAdapter = new TouchAdapterCustomer(mGroup.getCustomers(), this, realm);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration( new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                displayDialogAddCustomer();
                return true;
            case R.id.action_edit:
                displayDialogChangeName();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Title
        ((MainActivity) getActivity()).setActionBarTitle(String.valueOf(mGroup.getName()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void displayDialogAddCustomer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setAdapter(new AdapterCustomerList(realm.where(Customer.class).findAll()), null);

        final AlertDialog ad = builder.create();
        ad.getListView().setItemsCanFocus(false);
        ad.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        ad.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addCustomer((Customer) parent.getItemAtPosition(position));
            }
        });

        ad.show();
    }

    private void addCustomer(final Customer customer) {
        // Add customer to group
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mGroup.getCustomers().add(customer);
            }
        });

        // Notify item inserted
        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
    }

    private void displayDialogChangeName() {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(mGroup.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_change_group_name)
                .setView(et)
                .setPositiveButton("Changer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String input = et.getText().toString().isEmpty() ? "" : et.getText().toString();
                        editGroupName(mGroup, input);
                    }
                });

        final AlertDialog ad = builder.create();
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String input = et.getText().toString().isEmpty() ? "" : et.getText().toString();
                    editGroupName(mGroup, input);
                    ad.dismiss();
                    return true;
                }
                return false;
            }
        });

        ad.show();
    }

    private void editGroupName(final CustomerGroup group, final String newName) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                group.setName(newName);
            }
        });

        // Update title
        ((MainActivity) getActivity()).setActionBarTitle(newName);
    }
}