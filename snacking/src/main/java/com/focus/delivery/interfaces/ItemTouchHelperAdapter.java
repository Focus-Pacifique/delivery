package com.focus.delivery.interfaces;

import android.support.v7.widget.RecyclerView;

/**
 * Created by alexis on 18/06/17.
 */

public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
        void onItemCleared(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
}