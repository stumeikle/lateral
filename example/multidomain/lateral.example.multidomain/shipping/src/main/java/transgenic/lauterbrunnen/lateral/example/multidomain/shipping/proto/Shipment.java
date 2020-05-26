package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.proto;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.List;

/**
 * Created by stumeikle on 21/02/20.
 */
public class Shipment {

    @RepositoryId
    UniqueId    id;
    Address destination;
    UniqueId orderId;
    List<UniqueId> productsBeingShipped;
    ShippingContainer   box;

    DeliveryFirm    deliveryFirm;
    String          trackingNumber;
    long            expectedDeliveryDateUTC;
    long            creationDateUTC;

}
