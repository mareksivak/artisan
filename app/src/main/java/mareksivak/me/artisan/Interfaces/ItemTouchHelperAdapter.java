package mareksivak.me.artisan.Interfaces;

/**
 * Created by mareksivak on 14/10/2017.
 */

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
