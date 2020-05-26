package transgenic.lauterbrunnen.lateral.example.multidomain.shipping;

import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;

/**
 * Created by stumeikle on 25/05/20.
 */
public class ShippingServer {


    public ShippingServer() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

    }


    public static void main(String[] args) {
        new ShippingServer();
    }
}
