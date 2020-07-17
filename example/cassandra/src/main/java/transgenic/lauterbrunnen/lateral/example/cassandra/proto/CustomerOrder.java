package transgenic.lauterbrunnen.lateral.example.cassandra.proto;

import java.util.List;

/**
 * Created by stumeikle on 16/07/20.
 *
 * Can't call it just 'order' due to cassandra ;(
 */
public class CustomerOrder {

    RestaurantTable restaurantTable;
    long    creationTime;
    List<MenuSelection> selectedItems;
    double totalPrice;
    Payment payment;

}
