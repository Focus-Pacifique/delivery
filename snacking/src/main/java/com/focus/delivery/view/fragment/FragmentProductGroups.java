package com.focus.delivery.view.fragment;

import android.content.Context;
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
import android.widget.EditText;

import io.realm.Realm;

import com.focus.delivery.R;
import com.focus.delivery.adapter.ItemTouchHelperCallback;
import com.focus.delivery.adapter.TouchAdapterGroupProduct;
import com.focus.delivery.interfaces.OnStartDragListener;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.model.ProductGroup;
import com.focus.delivery.view.activity.MainActivity;

/**
 * Created by Alex on 29/01/2017.
 *
 * Fragment to manage group of products
 *
 */

public class FragmentProductGroups extends Fragment implements
        TouchAdapterGroupProduct.TouchAdapterGroupProductListener,
        OnStartDragListener {

    private Realm realm;
    private RecyclerView mRecyclerView;
    private TouchAdapterGroupProduct mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private FragmentProductGroupsListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FragmentProductGroupsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentProductGroupsListener");
        }
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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        setUpRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_fragment_product_group));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new TouchAdapterGroupProduct(realm.where(ProductGroup.class).findAllSorted(ProductGroup.FIELD_POSITION), this, realm);

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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_edit).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                displayDialogAddGroup();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDialogAddGroup() {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_product_group)
                .setView(et)
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String input = et.getText().toString().isEmpty() ? "" : et.getText().toString();
                        addProductGroup(input);
                    }
                });

        final AlertDialog ad = builder.create();
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String input = et.getText().toString().isEmpty() ? "" : et.getText().toString();
                    addProductGroup(input);
                    ad.dismiss();
                    return true;
                }
                return false;
            }
        });

        ad.show();
    }

    private void addProductGroup(final String name) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ProductGroup group = ProductGroup.create(realm);
                group.setName(name);
                mAdapter.notifyItemInserted(group.getPosition());
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onProductGroupSelected(ProductGroup group) {
        mListener.displayProductsOfGroup(group);
    }

    public interface FragmentProductGroupsListener {
        void displayProductsOfGroup(ProductGroup productGroup);
    }
}
