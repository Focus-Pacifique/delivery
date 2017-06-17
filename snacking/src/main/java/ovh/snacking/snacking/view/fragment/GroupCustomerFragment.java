package ovh.snacking.snacking.view.fragment;

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
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.model.CustomerAndGroupBinding;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 06/02/2017.
 *
 * Fragment to manage group of customers
 *
 */

public class GroupCustomerFragment extends Fragment {
    OnGroupCustomerSelectedListener mListener;
    private Realm realm;
    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnGroupCustomerSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGroupCustomerSelectedListener");
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

        RealmResults<CustomerGroup> customerGroups = realm.where(CustomerGroup.class).findAllSorted("position");
        GroupCustomerAdapter mGroupAdapter = new GroupCustomerAdapter(customerGroups);
        mRecyclerView.setAdapter(mGroupAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // On fab click : new CustomerGroup
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomerGroup();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.nav_group_customer));
        fab.setImageResource(R.drawable.ic_create_white_24dp);
        fab.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void deleteCustomerGroup(final CustomerGroup custGroup) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (realm.where(CustomerGroup.class).findAll().size() > 1) {

                    // Set correct position of group
                    RealmResults<CustomerGroup> groups = realm.where(CustomerGroup.class).greaterThan("position", custGroup.getPosition()).findAll();
                    for (CustomerGroup group : groups) {
                        group.setPosition(group.getPosition() - 1);
                    }

                    // Delete the corresponding lines into the bind table
                    realm.where(CustomerAndGroupBinding.class).equalTo("group.id", custGroup.getId()).findAll().deleteAllFromRealm();

                    //Delete the customer group
                    custGroup.deleteFromRealm();
                } else {
                    Toast.makeText(getContext(), R.string.minimum_group_size, Toast.LENGTH_LONG).show();
                }
            }
        });
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
                                    Integer nextPosition = nextCustomerGroupPosition();
                                    CustomerGroup group = realm.createObject(CustomerGroup.class, nextCustomerGroupId());
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

    private Integer nextCustomerGroupId() {
        return realm.where(CustomerGroup.class).findFirst() != null ? (realm.where(CustomerGroup.class).max("id").intValue() + 1) : 1;
    }

    private Integer nextCustomerGroupPosition() {
        return realm.where(CustomerGroup.class).findAll().size() + 1;
    }

    public interface OnGroupCustomerSelectedListener {
        void onGroupCustomerSelected(CustomerGroup group);
    }

    private class GroupCustomerAdapter extends RealmRecyclerViewAdapter<CustomerGroup, GroupCustomerAdapter.ViewHolder> {

        private GroupCustomerAdapter(OrderedRealmCollection<CustomerGroup> realmResults) {
            super(realmResults, true);
        }

        @Override
        public GroupCustomerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_group, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GroupCustomerAdapter.ViewHolder holder, int position) {
            CustomerGroup group = getData().get(position);
            holder.imgItem.setImageResource(R.drawable.ic_people_black_24dp);
            holder.groupName.setText(String.valueOf(group.getName()));
            holder.groupPosition.setText(String.valueOf(group.getPosition()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
            private final ImageView imgItem;
            private final TextView groupName;
            private final TextView groupPosition;

            public ViewHolder(View view) {
                super(view);
                imgItem = (ImageView) view.findViewById(R.id.group_image);
                groupName = (TextView) view.findViewById(R.id.group_name);
                groupPosition = (TextView) view.findViewById(R.id.group_position);
                view.setOnLongClickListener(this);
                view.setOnClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                deleteCustomerGroup(getItem(getAdapterPosition()));
                return true;
            }

            @Override
            public void onClick(View v) {
                mListener.onGroupCustomerSelected(getItem(getAdapterPosition()));
            }
        }
    }
}
