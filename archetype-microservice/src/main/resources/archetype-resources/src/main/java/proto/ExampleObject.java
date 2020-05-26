package ${package}.proto;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.List;

/**
 * Created by stumeikle on 26/05/20.
 */
public class ExampleObject {

    @RepositoryId
    UniqueId id;

    String name;
    double price;
    String description;
    List<ExampleReview> reviews;
}
