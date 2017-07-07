package com.focus.delivery.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import com.focus.delivery.R;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.model.ProductAndGroupBinding;
import com.focus.delivery.model.ProductGroup;
import com.focus.delivery.view.activity.MainActivity;

/**
 * Created by Alex on 29/01/2017.
 *
 * Fragment to manage group of products
 *
 */

public class FragmentProductGroup extends Fragment {
    ProductGroupFragmentListener mListener;
    private Realm realm;
    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ProductGroupFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ProductGroupFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(glm);

        RealmResults<ProductGroup> productGroups = realm.where(ProductGroup.class).findAllSorted("position");
        GroupProductAdapter mGroupAdapter = new GroupProductAdapter(productGroups);
        mRecyclerView.setAdapter(mGroupAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // On fab click : new ProductGroup
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProductGroup();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.nav_group_product));
        fab.setImageResource(R.drawable.ic_create_white_24dp);
        fab.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void deleteProductGroup(final ProductGroup group) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (realm.where(ProductGroup.class).findAll().size() > 1) {

                    // Set correct position of group
                    RealmResults<ProductGroup> groups = realm.where(ProductGroup.class).greaterThan("position", group.getPosition()).findAll();
                    for (ProductGroup group : groups) {
                        group.setPosition(group.getPosition() - 1);
                    }

                    // Delete the corresponding lines into the bind table
                    realm.where(ProductAndGroupBinding.class).equalTo("group.id", group.getId()).findAll().deleteAllFromRealm();

                    //Delete the product group
                    group.deleteFromRealm();
                } else {
                    Toast.makeText(getContext(), R.string.minimum_group_size, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addProductGroup() {
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_group_product)
                .setView(et)
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String input = et.getText().toString();
                        if (!input.isEmpty()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Integer nextPosition = nextProductGroupPosition();
                                    ProductGroup group = realm.createObject(ProductGroup.class, nextProductGroupId());
                                    group.setName(input);
                                    group.setPosition(nextPosition);
                                    Toast.makeText(getContext(), "Groupe " + input + " ajouté", Toast.LENGTH_SHORT).show();
                                }
                            });
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
        ad.show();
    }

    private Integer nextProductGroupId() {
        return realm.where(ProductGroup.class).findFirst() != null ? (realm.where(ProductGroup.class).max("id").intValue() + 1) : 1;
    }

    private Integer nextProductGroupPosition() {
        return realm.where(ProductGroup.class).findAll().size() + 1;
    }

    public interface ProductGroupFragmentListener {
        void onGroupProductSelected(ProductGroup productGroup);
    }

    private class GroupProductAdapter extends RealmRecyclerViewAdapter<ProductGroup, GroupProductAdapter.ViewHolder> {

        private GroupProductAdapter(OrderedRealmCollection<ProductGroup> realmResults) {
            super(realmResults, true);
        }

        @Override
        public GroupProductAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_position, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GroupProductAdapter.ViewHolder holder, int position) {
            ProductGroup group = getData().get(position);
            holder.imgItem.setImageResource(R.drawable.ic_label_black_24dp);
            holder.groupName.setText(String.valueOf(group.getName()));
            holder.groupPosition.setText(String.valueOf(group.getPosition()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
            private final ImageView imgItem;
            private final TextView groupName;
            private final TextView groupPosition;

            public ViewHolder(View view) {
                super(view);
                imgItem = (ImageView) view.findViewById(R.id.image);
                groupName = (TextView) view.findViewById(R.id.tvPosition);
                groupPosition = (TextView) view.findViewById(R.id.tvName);
                view.setOnLongClickListener(this);
                view.setOnClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                deleteProductGroup(getItem(getAdapterPosition()));
                return true;
            }

            @Override
            public void onClick(View v) {
                mListener.onGroupProductSelected(getItem(getAdapterPosition()));
            }
        }
    }
}
