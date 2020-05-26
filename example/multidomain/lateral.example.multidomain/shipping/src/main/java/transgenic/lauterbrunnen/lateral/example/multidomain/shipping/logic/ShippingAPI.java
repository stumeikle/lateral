package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic;

import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;

import java.util.Collection;

/**
 * Created by stumeikle on 24/02/20.
 *
 * How we expect this domain model to be interacted with?
 *
 * I think changes, new objects should always be persisted.
 * If we need to control the rate at which the changes are written to the db then
 * we can use configuration for that. Changes can always be written to the cache.
 */
public interface ShippingAPI {

    //This is the key
    //What do we want our business logic to be able to do?
    //bit strange this next is really createNewShipmentForSingleProduct
    Shipment createNewShipment(Address destination, UniqueId orderId, UniqueId productId) throws PersistenceException;

    Shipment retrieveShipment(UniqueId id);
    void modifyShipment();
    void addProductToShipment(Shipment shipment, UniqueId productId) throws PersistenceException ;
    Collection<Shipment> findShipmentsForOrderId(UniqueId orderId);
    Collection<Shipment> findShipmentsForAddress(Address address);
    Collection<Shipment> findShipmentForTrackingNumber(String trackingNumber);
    Address createShippingAddress(String houseName, String street, String city, String country, String postcode)throws PersistenceException;

}
