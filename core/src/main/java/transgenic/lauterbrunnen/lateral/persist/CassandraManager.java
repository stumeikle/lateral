package transgenic.lauterbrunnen.lateral.persist;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * Created by stumeikle on 29/06/20.
 */
public interface CassandraManager {

    interface SessionExecute {
        ResultSet sessionExecute(Session session);
    }

    ResultSet runInSession(SessionExecute sessionExecute);
    void save(Object object);
    void delete(Object key, Class objectClass);
    <T> T load(Object key, Class<T> objectClass);
}
