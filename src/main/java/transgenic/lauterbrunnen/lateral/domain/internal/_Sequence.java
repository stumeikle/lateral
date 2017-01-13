package transgenic.lauterbrunnen.lateral.domain.internal;

import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

/**
 * Created by stumeikle on 13/01/17.
 */
//optimistic locking
public class _Sequence {

    @RepositoryId
    String name;
    int    value;
}
