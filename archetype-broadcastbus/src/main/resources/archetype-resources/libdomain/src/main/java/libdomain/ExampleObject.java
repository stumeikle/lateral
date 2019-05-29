package ${package}.libdomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;
import transgenic.lauterbrunnen.lateral.domain.Sequence;
import java.util.List;

public class ExampleObject {

    //Preferrably make these private or package level security to prevent mistaken usage in the server or
    //application
    String name;
    List<String> addresses;
    @RepositoryId
    @Sequence
    int id;

}