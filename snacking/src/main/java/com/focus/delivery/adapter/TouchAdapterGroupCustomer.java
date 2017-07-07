package com.focus.delivery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.focus.delivery.R;
import com.focus.delivery.interfaces.ItemTouchHelperAdapter;
import com.focus.delivery.interfaces.ItemTouchHelperViewHolder;
import com.focus.delivery.interfaces.OnStartDragListener;
import com.focus.delivery.model.CustomerGroup;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by alexis on 18/06/17.
 */

public class TouchAdapterGroupCustomer extends RealmRecyclerViewAdapter<CustomerGroup, TouchAdapterGroupCustomer.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    private final CustomerGroupAdapterListener mListener;
    private final Context mContext;
    private Realm realm;

    public interface CustomerGroupAdapterListener {
        void onCustomerGroupSelected(CustomerGroup group);
    }

    public TouchAdapterGroupCustomer(OrderedRealmCollection<CustomerGroup> realmResults, Fragment fragment, Realm realm) {
        super(realmResults, false);
        this.mContext = fragment.getContext();
        this.mDragStartListener = (OnStartDragListener) fragment;
        this.mListener = (CustomerGroupAdapterListener) fragment;
        this.realm = realm;
    }

    @Override
    public TouchAdapterGroupCustomer.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_position, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(getData() != null) {
            final CustomerGroup group = getData().get(position);

            holder.tvPosition.setText(String.valueOf(group.getPosition()));
            holder.tvName.setText(String.valueOf(group.getName()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCustomerGroupSelected(group);
                }
            });
        }

        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
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
    }

    @Override
    public void onItemDismiss(final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CustomerGroup.delete(realm, getData().get(position).getPosition());
            }
        });
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, getItemCount());
    }

    @Override
    public void onItemCleared(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int fromPosition = Integer.valueOf(((ViewHolder) viewHolder).tvPosition.getText().toString());
        final int toPosition = viewHolder.getAdapterPosition();

        if(fromPosition != toPosition && toPosition != -1)
            notifyItemRangeChanged(Math.min(fromPosition, toPosition), getItemCount());
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
