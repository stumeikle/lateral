package transgenic.lauterbrunnen.lateral.exampledomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

import java.util.List;
import java.util.Map;

/**
 * Created by stumeikle on 28/05/16.
 */
public class School {

    @RepositoryId
    private String       name;
    private Address      address;
    private List<Member> members;
    private List<Room>   rooms;
    private List<SchoolClass> classes;
    private Map<String, Teacher> someMap;

}
