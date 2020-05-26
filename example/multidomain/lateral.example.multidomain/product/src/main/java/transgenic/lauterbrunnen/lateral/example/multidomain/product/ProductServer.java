package transgenic.lauterbrunnen.lateral.example.multidomain.product;

import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.ProductContext;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 25/05/20.
 */

public class ProductServer {


    public ProductServer() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

//        Repository repository = inject(Repository.class, ProductContext.class);
//        Factory factory = inject(Factory.class, ProductContext.class);
//
//        Product product = factory.create(Product.class);
//        try {
//            repository.persist(product);
//        } catch (PersistenceException e) {
//            e.printStackTrace();
//        }

    }


    public static void main(String[] args) {
        new ProductServer();
    }
}
