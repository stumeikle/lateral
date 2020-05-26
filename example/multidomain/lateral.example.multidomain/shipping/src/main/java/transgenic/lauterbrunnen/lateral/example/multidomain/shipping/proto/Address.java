package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.proto;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 21/02/20.
 */
public class Address {

    @RepositoryId
    UniqueId id;
    String houseNameNumber;
    String street;
    String area;
    String city;
    String county;
    String country;
    String postCode;

}
