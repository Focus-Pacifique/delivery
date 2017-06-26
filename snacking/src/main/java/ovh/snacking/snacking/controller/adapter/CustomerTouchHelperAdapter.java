package ovh.snacking.snacking.controller.adapter;

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

import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmList;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.interfaces.ItemTouchHelperViewHolder;
import ovh.snacking.snacking.model.Customer;

/**
 * Created by alexis on 19/06/17.
 */

public class CustomerTouchHelperAdapter extends SelectableAdapter<Customer, CustomerTouchHelperAdapter.ViewHolder> {
    private final OnStartDragListener mDragStartListener;
    private final Context mContext;
    private Realm realm;

    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public CustomerTouchHelperAdapter(RealmList<Customer> customers, Fragment fragment, Realm realm) {
        super(customers);
        this.mContext = fragment.getContext();
        this.mDragStartListener = (OnStartDragListener) fragment;
        this.realm = realm;
    }

    @Override
    public CustomerTouchHelperAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_position, parent, false);
        return new CustomerTouchHelperAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CustomerTouchHelperAdapter.ViewHolder holder, int position) {
        final Customer customer = getItems().get(position);
        holder.tvPosition.setText(String.valueOf(position));
        holder.tvName.setText(String.valueOf(customer.getName()));

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
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(getItems(), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(getItems(), i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(final int position) {
        getItems().remove(position);
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
