package transgenic.lauterbrunnen.lateral.example.multidomain.product.logic;

import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.*;

import java.util.ArrayList;
import java.util.List;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 03/05/20.
 */
public class ProductManager implements ProductAPI {

    private Factory factory = inject(Factory.class, ProductContext.class);
    private Repository repository = inject(Repository.class, ProductContext.class);

    @Override
    public Product createNewProduct(String name, Category category, Dimensions dimensions, double price, String description) throws PersistenceException {

        Product product = factory.create(Product.class);

        product.setName(name);
        product.setCategory(category);
        product.setDimensions(dimensions);
        product.setPrice(price);
        product.setDescription(description);

        repository.persist(product);

        return product;
    }

    @Override
    public void addReviewToProduct(UniqueId productId, Review review) throws PersistenceException {

        ProductRepository productRepository = (ProductRepository) repository.getRepositoryForClass(Product.class);
        Product product = productRepository.retrieve(productId);

        List<Review> reviews = product.getReviews();
        if (reviews==null) {
            reviews = new ArrayList<>();
            product.setReviews(reviews);
        }
        reviews.add(review);

        repository.persist(product);

    }
}
