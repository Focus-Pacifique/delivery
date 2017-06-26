package ovh.snacking.snacking.controller.interfaces;

/**
 * Created by alexis on 18/06/17.
 */

public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
}