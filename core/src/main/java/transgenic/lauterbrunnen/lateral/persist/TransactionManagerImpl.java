package transgenic.lauterbrunnen.lateral.persist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by stumeikle on 11/06/19.
 */
@DefaultImpl
public class TransactionManagerImpl implements TransactionManager {

    private static final Log LOG = LogFactory.getLog(TransactionManagerImpl.class);

    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("pu");
    private final EntityManager em = factory.createEntityManager();

    public void runInTransactionalContext(TransactionManager.Runnable runnable) {

//        EntityManager em = factory.createEntityManager();
        try {
            if (!em.getTransaction().isActive())
                em.getTransaction().begin();

            runnable.run(em);

            em.getTransaction().commit();
        }catch(Exception ex) {
            LOG.error(ex);
            em.getTransaction().rollback();
        }
//        finally {
//            em.close();
//        }
    }

    protected void finalize() {
        em.close();
    }
}
