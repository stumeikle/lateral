package transgenic.lauterbrunnen.lateral.domain.internal;

import transgenic.lauterbrunnen.lateral.domain.OptimisticLocking;
import transgenic.lauterbrunnen.lateral.domain.RepositoryId;

/**
 * Created by stumeikle on 13/01/17.
 */
@OptimisticLocking
public class _Sequence {
    @RepositoryId
    String name;
    long   value;
}
