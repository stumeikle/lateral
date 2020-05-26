package transgenic.lauterbrunnen.lateral.example.multidomain.server;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;

import java.util.List;

/**
 * Created by stumeikle on 06/05/20.
 */
public class Order {

    private UniqueId orderId;
    private List<Product> products;

    public UniqueId getOrderId() {
        return orderId;
    }

    public void setOrderId(UniqueId orderId) {
        this.orderId = orderId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
