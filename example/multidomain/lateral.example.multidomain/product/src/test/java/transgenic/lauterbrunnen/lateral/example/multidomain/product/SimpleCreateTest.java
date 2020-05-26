package transgenic.lauterbrunnen.lateral.example.multidomain.product;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.domain.PersistenceException;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Category;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Dimensions;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Product;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.generated.Review;
import transgenic.lauterbrunnen.lateral.example.multidomain.product.logic.ProductManager;

import java.util.Collection;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;

/**
 * Created by stumeikle on 03/05/20.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleCreateTest {

    private static Factory factory;
    private static Repository repository;

    @BeforeClass
    public static void beforeClass() {
        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();
        factory = inject(Factory.class);
        repository = inject(Repository.class);
    }

    @Test
    public void createTest() {

        try {

            ProductManager productManager = new ProductManager();
            Product product;

            //Create supplemental objects
            Category category = factory.create(Category.class);
            category.setCategoryCode("Fruit");
            Dimensions dimensions = factory.create(Dimensions.class);
            dimensions.setHeight(2.3);
            dimensions.setWidth(1.23);
            dimensions.setDepth(10.56);

            product = productManager.createNewProduct("ElFantastico", category, dimensions, 29.95, "Something incredible");
        }
        catch(PersistenceException persistenceException) {
            System.out.println("Trouble");
            assert(true);
        }
    }

    @Test
    public void retrieveTest() {

        //should be run directly after create test
        Collection<Product> products = repository.search(Product.class,"name='ElFantastico'");

        assert(products.size()==1);


        try {
            //Now create a review
            Review review = factory.create(Review.class);
            review.setAuthor("Joe Bloggs");
            review.setNumberOfStars(3);
            review.setTitle("Mediocre rubbish");
            review.setDescription("Barely usable, definately does not live up to the brand name! Smells nice though");
            Product product = products.iterator().next();

            ProductManager productManager = new ProductManager();
            productManager.addReviewToProduct(product.getId(), review);

        } catch(PersistenceException pe) {
            assert(false);
        }

        //If you want to see the review on the product structure the addReviewToProduct method should either return
        //the new product or it should be retrieved again from the repository
    }
}
