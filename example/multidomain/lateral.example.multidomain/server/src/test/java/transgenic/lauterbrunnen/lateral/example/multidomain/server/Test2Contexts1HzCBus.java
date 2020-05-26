package transgenic.lauterbrunnen.lateral.example.multidomain.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.logic.ProductAPI;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.logic.ProductManager;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Shipment;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingAPI;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic.ShippingManager;

import java.util.ArrayList;

/**
 * Created by stumeikle on 09/05/20.
 */
public class Test2Contexts1HzCBus {

    private static final Log LOG = LogFactory.getLog(Test2Contexts1HzCBus.class);
    private ProductAPI productAPI;
    private ShippingAPI shippingAPI;

    @Test
    public void Test2Contexts1HzCBus(){

        BasicConfigurator.configure();
        Lateral.INSTANCE.setPropertyFilename("test2contexts1hzcbus-app.properties");
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
            assert(false);
        }

        assert(true);
    }

}
