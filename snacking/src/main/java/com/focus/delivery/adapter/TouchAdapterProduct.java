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
import com.focus.delivery.model.Product;

import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by alex on 08/07/17.
 */

public class TouchAdapterProduct extends RealmRecyclerViewAdapter<Product, TouchAdapterProduct.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    private final Context mContext;
    private Realm realm;

    public TouchAdapterProduct(RealmList<Product> products, Fragment fragment, Realm realm) {
        super(products, false);
        this.mContext = fragment.getContext();
        this.mDragStartListener = (OnStartDragListener) fragment;
        this.realm = realm;
    }

    @Override
    public TouchAdapterProduct.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_position, parent, false);
        return new TouchAdapterProduct.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TouchAdapterProduct.ViewHolder viewHolder, int position) {
        final Product product = getData().get(position);
        viewHolder.tvPosition.setText(String.valueOf(position));
        viewHolder.tvName.setText(String.valueOf(product.getLabel()));

        viewHolder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(viewHolder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        if (fromPosition < toPosition) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(getData(), i, i + 1);
                    }
                }
            });
        } else if (fromPosition > toPosition){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(getData(), i, i - 1);
                    }
                }
            });
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                getData().remove(position);
            }
        });

        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, getItemCount());
    }

    @Override
    public void onItemCleared(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int fromPosition = Integer.valueOf(((TouchAdapterProduct.ViewHolder) viewHolder).tvPosition.getText().toString());
        final int toPosition = viewHolder.getAdapterPosition();

        if(fromPosition != toPosition && toPosition != -1)
            notifyItemRangeChanged(Math.min(fromPosition, toPosition), getItemCount());

    }

    static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
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
