package transgenic.lauterbrunnen.lateral.example.multidomain.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.ProductContext;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.logic.ProductAPI;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.logic.ProductManager;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingAPI;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingManager;
//import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
//import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;
//import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.ShippingContext;
//import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingAPI;
//import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingManager;

import java.util.ArrayList;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/*
 * A fantasy server which will bridge both product and shipping domains (or bounded contexts)
 * Not recommended in the real world, but used here to test shared/separate caching and dbs
 *
 *
 * Need a way to run this up with different application.properties
 * ie.
 *
 * hazelcast only. both contexts connected to the same hazelcast
 * hazelcast only. each context on a different multicast bus
 * db backed, both contexts storing to the same db (does it matter the cache configuration?)
 * db backed, both contexts storing to different dbs
 */
public class Server {

    private static final Log LOG = LogFactory.getLog(Server.class);
    private ProductAPI productAPI;
    private ShippingAPI shippingAPI;


    public Server() {

        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        productAPI = new ProductManager();
        shippingAPI = new ShippingManager();

        //create an order
        //add products to the order
        //ship the order, maybe split the shipment

        //lets say orders are persisted elsewhere for whatever reason
        Order order = new Order();
        order.setOrderId(UniqueId.generate());

        try {

            Product coolThing = productAPI.createNewProduct("YBoz", null, null, 99.99, "Something all the kids are raving about");
            order.setProducts(new ArrayList<>());
            order.getProducts().add(coolThing);

            Product boringThing = productAPI.createNewProduct("Dull-lite", null, null, 0.95, "Yawn yawn yaaaaawn");
            order.getProducts().add(boringThing);

            Address address = shippingAPI.createShippingAddress("9","Acacia Avenue", "Bananaville", "UK", "AA1 BML");
            Shipment shipment1 = shippingAPI.createNewShipment(address,order.getOrderId(), coolThing.getId());
            Shipment shipment2 = shippingAPI.createNewShipment(address,order.getOrderId(), boringThing.getId());

            //at this point we expect 2 embedded hazelcast servers both connecting to the same hc bus
            //

        }
        catch(Exception e) {
            System.out.println("Exception "  + e );
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
