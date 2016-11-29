package transgenic.lauterbrunnen.lateral.example.microservice.libdomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

import java.net.URL;

/**
 * Created by stumeikle on 17/11/16.
 */
public class Track {

    String name;
    Album album;
    Artist artist;
    double lengthInMins;
    URL media;
    @RepositoryId
    int id;

}
