package transgenic.lauterbrunnen.lateral.persist;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by stumeikle on 13/05/16.
 */
public enum TransactionManager {

    INSTANCE;

    public interface Runnable {
        void run(EntityManager em);
    }

    //private static final TransactionManager instance = new TransactionManager();
    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("pu");
//    private final EntityManager em = factory.createEntityManager();

    private TransactionManager() {

    }

    public void runInTransactionalContext(TransactionManager.Runnable runnable) {

        EntityManager em = factory.createEntityManager();
        try {
            em.getTransaction().begin();

            runnable.run(em);

            em.getTransaction().commit();
        }catch(Exception ex) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
