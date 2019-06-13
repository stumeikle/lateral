package transgenic.lauterbrunnen.lateral.persist;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by stumeikle on 13/05/16.
 */
public interface TransactionManager {

    public interface Runnable {
        void run(EntityManager em);
    }

    void runInTransactionalContext(TransactionManager.Runnable runnable);
}
