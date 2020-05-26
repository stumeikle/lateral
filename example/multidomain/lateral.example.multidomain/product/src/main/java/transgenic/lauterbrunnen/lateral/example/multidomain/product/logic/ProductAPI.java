package transgenic.lauterbrunnen.lateral.example.multidomain.product.logic;

import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Category;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Dimensions;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Review;

/**
 * Created by stumeikle on 03/05/20.
 */
public interface ProductAPI {

    Product createNewProduct(String name, Category category, Dimensions dimensions, double price, String description) throws PersistenceException;
    void addReviewToProduct(UniqueId productId, Review review) throws PersistenceException;
}
