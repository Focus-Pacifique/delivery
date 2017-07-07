package com.focus.delivery.interfaces;

/**
 * Created by alexis on 19/06/17.
 */

public interface ItemTouchHelperViewHolder {
    /**
     * Called when the first registers an
     * item as being moved or swiped.
     * Implementations should update the item view to indicate
     * it's active state.
     */
    void onItemSelected();


    /**
     * Called when the has completed the
     * move or swipe, and the active item state should be cleared.
     */
    void onItemClear();
}
