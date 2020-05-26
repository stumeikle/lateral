package transgenic.lauterbrunnen.lateral.example.multidomain.product.proto;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.List;

/**
 * Created by stumeikle on 21/02/20.
 */
public class Product {

    @RepositoryId
    UniqueId id;

    String name;
    Category category;
    Dimensions dimensions;
    ColourOptions colourOptions;
    double price;
    String description;
    List<Review> reviews;

}
