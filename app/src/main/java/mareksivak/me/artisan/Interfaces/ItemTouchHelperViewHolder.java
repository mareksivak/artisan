package mareksivak.me.artisan.Interfaces;

/**
 * Created by mareksivak on 14/10/2017.
 */

public interface ItemTouchHelperViewHolder {

     // Implementations should update the item view to indicate it's active state.
    void onItemSelected();


    // Called when completed the move or swipe, and the active item * state should be cleared.
    void onItemClear();
}
