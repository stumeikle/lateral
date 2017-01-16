package transgenic.lauterbrunnen.lateral.example.microservice.libdomain;

import transgenic.lauterbrunnen.lateral.domain.OptimisticLocking;
import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.Sequence;

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
    @Sequence
    int id;

}
