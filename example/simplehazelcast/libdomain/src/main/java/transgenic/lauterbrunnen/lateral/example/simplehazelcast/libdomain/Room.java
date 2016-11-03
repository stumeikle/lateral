package transgenic.lauterbrunnen.lateral.example.simplehazelcast.libdomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

/**
 * Created by stumeikle on 28/05/16.
 */
public class Room {

    @RepositoryId
    private String fireCode;
    private String name;
    private String location;
}
