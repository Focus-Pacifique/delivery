package ovh.snacking.snacking.controller.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.interfaces.ItemTouchHelperViewHolder;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.view.fragment.CustomerGroupsFragment;

/**
 * Created by alexis on 18/06/17.
 */

public class CustomerGroupAdapter extends SelectableAdapter<CustomerGroup, CustomerGroupAdapter.ViewHolder> {

    private final CustomerGroupAdapterListener mListener;
    private final Context mContext;
    private Realm realm;

    public interface CustomerGroupAdapterListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
        void onCustomerGroupSelected(CustomerGroup group);
    }

    public CustomerGroupAdapter(OrderedRealmCollection<CustomerGroup> realmResults, Fragment fragment, Realm realm) {
        super(realmResults);
        this.mContext = fragment.getContext();
        this.mListener = (CustomerGroupsFragment) fragment;
        this.realm = realm;
    }

    @Override
    public CustomerGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_position, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CustomerGroupAdapter.ViewHolder holder, int position) {
        final CustomerGroup group = getItems().get(position);
        holder.tvPosition.setText(String.valueOf(group.getPosition()));
        holder.tvName.setText(String.valueOf(group.getName()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCustomerGroupSelected(group);
            }
        });

        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                changeGroupName(group);
                return true;
            }
        });*/

        /*holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mListener.onStartDrag(holder);
                }
                return false;
            }
        });*/
    }

    private void changeGroupName(final CustomerGroup group) {
        final EditText et = new EditText(mContext);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(group.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.dialog_change_group_name)
                .setView(et)
                .setPositiveButton("Changer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String input = et.getText().toString();
                        if (!input.isEmpty()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    group.setName(input);
                                    notifyItemChanged(group.getPosition());
                                }
                            });
                        }
                    }
                });

        AlertDialog ad = builder.create();
        ad.show();
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CustomerGroup group = realm.where(CustomerGroup.class).equalTo(CustomerGroup.FIELD_POSITION, fromPosition).findFirst();
                if (fromPosition < toPosition) {
                    RealmResults<CustomerGroup> results = realm.where(CustomerGroup.class)
                            .greaterThan(CustomerGroup.FIELD_POSITION, fromPosition)
                            .lessThanOrEqualTo(CustomerGroup.FIELD_POSITION, toPosition)
                            .findAll();
                    for (int i = 0; i < results.size(); i++) {
                        results.get(i).setPosition(results.get(i).getPosition() - 1);
                    }
                } else {
                    RealmResults<CustomerGroup> results = realm.where(CustomerGroup.class)
                            .greaterThanOrEqualTo(CustomerGroup.FIELD_POSITION, toPosition)
                            .lessThan(CustomerGroup.FIELD_POSITION, fromPosition)
                            .findAll();
                    for (int i = 0; i < results.size(); i++) {
                        results.get(i).setPosition(results.get(i).getPosition() + 1);
                    }
                }
                group.setPosition(toPosition);
            }
        });
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
    }

    @Override
    public void onItemDismiss(final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CustomerGroup.delete(realm, getItems().get(position).getPosition());
            }
        });
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private final TextView tvPosition;
        private final TextView tvName;
        private final ImageView handleView;

        public ViewHolder(View view) {
            super(view);
            tvPosition = (TextView) view.findViewById(R.id.tvPosition);
            tvName = (TextView) view.findViewById(R.id.tvName);
            handleView = (ImageView) view.findViewById(R.id.handle);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }
    }
}
