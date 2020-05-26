package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic;

import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.DeliveryFirm;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.ShippingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 21/02/20.
 */
public class ShippingManager implements ShippingAPI {

    /*
        Address destination;
    UniqueId orderId;
    List<UniqueId> productsBeingShipped;
    ShippingContainer   box;

    DeliveryFirm    deliveryFirm;
    String          trackingNumber;
    long            expectedDeliveryDateUTC;
    long            creationDateUTC;
     */


    //TODO -- will this work, probably not in-abstracio but will need a repo mocked for testing
    private Factory factory = inject(Factory.class, ShippingContext.class);
    private Repository repository = inject(Repository.class, ShippingContext.class);
    private DeliveryFirm someFirmOrOther;

    public ShippingManager() {

        //retrieve the standard delivery firm from the database
        //or, if there's none there create a default value.
        //this could be configuration driven of course
        Collection<DeliveryFirm> matches = repository.search( DeliveryFirm.class, "name='Bogus Deliveries'");
        if (matches.size()==0) {
            //initialise our standard delivery firm
            someFirmOrOther = factory.create(DeliveryFirm.class);
            someFirmOrOther.setName("Bogus Deliveries");

            Address address = factory.create(Address.class);
            address.setHouseNameNumber("29");
            address.setStreet("Acacia Road");
            address.setCity("Birmingham");
            someFirmOrOther.setContactAddress( address );
        } else {
            someFirmOrOther = matches.iterator().next();
        }
    }

    public Shipment createNewShipment(Address destination, UniqueId orderId, UniqueId productId) throws PersistenceException {

        Shipment    shipping = factory.create(Shipment.class);

        //validate the destination is a supported country
        shipping.setDestination(destination);

        List<UniqueId> productsBeingShipped = new ArrayList<>();
        productsBeingShipped.add( productId );
        shipping.setProductsBeingShipped(productsBeingShipped);
        shipping.setDeliveryFirm(someFirmOrOther);

        //call bogus deliveries web service to get a tracking number
        shipping.setTrackingNumber(""+ Math.random());
        shipping.setCreationDateUTC(System.currentTimeMillis()/1000);
        shipping.setExpectedDeliveryDateUTC(System.currentTimeMillis()/1000 + //bogus specific
                3*24*3600 );
        shipping.setOrderId(orderId);

        repository.persist(shipping);

        return shipping;
    }

    @Override
    public Shipment retrieveShipment(UniqueId id) {
        return repository.retrieve(Shipment.class, id);
    }

    @Override
    public void modifyShipment() {
        //knock yourself out
    }

    @Override
    public void addProductToShipment(Shipment shipment, UniqueId productId) throws PersistenceException {
        shipment.getProductsBeingShipped().add(productId);
        repository.update(shipment);

    }

    @Override
    public Collection<Shipment> findShipmentsForOrderId(UniqueId orderId) {
        return repository.search(Shipment.class, "orderId=" + orderId);
    }

    @Override
    public Collection<Shipment> findShipmentsForAddress(Address address) {
        //bit unclear how to do this
        //Search for all matching addresses
        //and then search for all shipments with said address
        return null;
    }

    @Override
    public Collection<Shipment> findShipmentForTrackingNumber(String trackingNumber) {
        return repository.search(Shipment.class, "trackingNumber='" + trackingNumber + "'");
    }

    public Address createShippingAddress(String houseName, String street, String city, String country, String postcode) throws PersistenceException{
        Address address = factory.create(Address.class);

        address.setHouseNameNumber(houseName);
        address.setStreet(street);
        address.setCity(city);
        address.setCountry(country);
        address.setPostCode(postcode);

        repository.persist(address);

        return address;
    }
}
