package transgenic.lauterbrunnen.lateral.exampledomain;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

import java.util.List;

/**
 * Created by stumeikle on 28/05/16.
 */
public class House {

    @RepositoryId
    private Integer id;
    private List<Teacher> teacherList;
    private HeadOfHouse headOfHouse;
}
