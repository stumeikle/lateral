package transgenic.lauterbrunnen.lateral.persist;

import com.impetus.client.cassandra.common.CassandraConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stumeikle on 11/06/19.
 *
 * TODO consider .. i don't want lateral to depend on every package under the sun if these are not actually
 * used in the deployment
 */
public class CassandraKunderaTransactionManager implements TransactionManager {

    private static final Log LOG = LogFactory.getLog(CassandraKunderaTransactionManager.class);

    private final EntityManagerFactory factory;
    private final EntityManager em;

    public CassandraKunderaTransactionManager() {
        Map<String, String> props = new HashMap<>();
        props.put(CassandraConstants.CQL_VERSION, CassandraConstants.CQL_VERSION_3_0);

        factory = Persistence.createEntityManagerFactory("cassandra_pu", props);
        em = factory.createEntityManager();
    }

    @Override
    public void runInTransactionalContext(Runnable runnable) {
        try {
            if (!em.getTransaction().isActive())
                em.getTransaction().begin();

            runnable.run(em);

            em.getTransaction().commit();
        }catch(Exception ex) {
            LOG.error(ex);
            em.getTransaction().rollback();
        }
    }

    protected void finalize() {
        em.close();
    }
}
