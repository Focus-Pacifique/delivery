package com.focus.delivery.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
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
import com.focus.delivery.adapter.AdapterProductList;
import com.focus.delivery.adapter.ItemTouchHelperCallback;
import com.focus.delivery.adapter.TouchAdapterProduct;
import com.focus.delivery.interfaces.OnStartDragListener;
import com.focus.delivery.model.Product;
import com.focus.delivery.model.ProductGroup;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.activity.MainActivity;

import io.realm.Realm;

/**
 * Created by Alex on 07/02/2017.
 *
 * Fragment to mangage products into a group
 *
 */

public class FragmentProductGroupDetails extends Fragment implements OnStartDragListener {

    private Realm realm;
    private RecyclerView mRecyclerView;
    private TouchAdapterProduct mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ProductGroup mGroup;

    public static FragmentProductGroupDetails newInstance(int position) {
        FragmentProductGroupDetails frag = new FragmentProductGroupDetails();

        Bundle bundle = new Bundle();
        bundle.putInt(ProductGroup.FIELD_POSITION, position);
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        setHasOptionsMenu(true);

        mGroup = realm.where(ProductGroup.class).equalTo(ProductGroup.FIELD_POSITION, getArguments().getInt(ProductGroup.FIELD_POSITION)).findFirst();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView() {
        mAdapter = new TouchAdapterProduct(mGroup.getProducts(), this, realm);

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
                displayDialogAddProduct();
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

    private void displayDialogAddProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setAdapter(new AdapterProductList(realm.where(Product.class).findAll()), null);

        final AlertDialog ad = builder.create();
        ad.getListView().setItemsCanFocus(false);
        ad.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        ad.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addProduct((Product) parent.getItemAtPosition(position));
            }
        });

        ad.show();
    }

    private void addProduct(final Product product) {
        // Add customer to group
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mGroup.getProducts().add(product);
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

        final android.support.v7.app.AlertDialog ad = builder.create();
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

    private void editGroupName(final ProductGroup group, final String newName) {
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