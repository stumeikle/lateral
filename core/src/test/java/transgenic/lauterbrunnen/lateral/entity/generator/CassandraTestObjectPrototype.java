package transgenic.lauterbrunnen.lateral.entity.generator;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by stumeikle on 01/07/20.
 */
public class CassandraTestObjectPrototype {

    @RepositoryId
    UniqueId id;
    int number;
    double fraction;
    float percentage;
    BigDecimal decimal;
    String description;
    List<String> reviews;
    List<Integer> stars;
    Map<UniqueId, String> lookup;
    Set<Double> proportions;
    Map<UniqueId, List<String>> contacts;

}
