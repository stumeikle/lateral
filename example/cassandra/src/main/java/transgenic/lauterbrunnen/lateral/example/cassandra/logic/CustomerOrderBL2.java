package transgenic.lauterbrunnen.lateral.example.cassandra.logic;

import transgenic.lauterbrunnen.lateral.example.cassandra.generated.*;

import java.util.List;

/**
 * Created by stumeikle on 08/09/20.
 */
public class CustomerOrderBL2 implements CustomerOrder, GetWrappedImpl {

    private CustomerOrderImpl proxee;

    public CustomerOrderBL2(CustomerOrderImpl proxee) {
        this.proxee= proxee;
    }


    //Put validations and business logic in here and refer back to the impl

    @Override
    public RestaurantTable getRestaurantTable() {
        return null;
    }

    @Override
    public void setRestaurantTable(RestaurantTable restaurantTable) {

    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public void setCreationTime(long creationTime) {

    }

    @Override
    public List<MenuSelection> getSelectedItems() {
        return null;
    }

    @Override
    public void setSelectedItems(List<MenuSelection> selectedItems) {

    }

    @Override
    public double getTotalPrice() {
        return 0;
    }

    @Override
    public void setTotalPrice(double totalPrice) {

    }

    @Override
    public Payment getPayment() {
        return null;
    }

    @Override
    public void setPayment(Payment payment) {

    }
}
