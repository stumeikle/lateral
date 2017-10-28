package transgenic.lauterbrunnen.lateral.admin;


import transgenic.lauterbrunnen.lateral.domain.CRUDRepository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 17/06/16.
 *  * EXPLORATORY, NOT IN USE
 */
public interface AdminRepository extends CRUDRepository<AdminPojo, UniqueId> {

    Boolean retrieveCacheReady();
}
