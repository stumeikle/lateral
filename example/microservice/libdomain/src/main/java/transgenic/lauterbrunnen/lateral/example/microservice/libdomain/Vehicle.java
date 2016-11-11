package transgenic.lauterbrunnen.lateral.example.microservice.libdomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

/**
 * Created by stumeikle on 03/11/16.
 */
public class Vehicle {

    @RepositoryId
    String registration;
    int    mileage;
    String make;
    String model;
    int    numDoors;
    Garage seller;
    boolean isFast;
}
