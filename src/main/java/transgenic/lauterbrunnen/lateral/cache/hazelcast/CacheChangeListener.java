package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.domain.EntityImpl;
import transgenic.lauterbrunnen.lateral.persist.DbQueue;
import transgenic.lauterbrunnen.lateral.persist.EventType;
import transgenic.lauterbrunnen.lateral.persist.EventWrapper;
import transgenic.lauterbrunnen.lateral.persist.Persister;


/**
 * Created by stumeikle on 13/05/16.
 * can this stuff not be generated?
 */
public class CacheChangeListener<I extends EntityImpl,T> implements EntryAddedListener<I,T>,EntryUpdatedListener<I,T>, EntryRemovedListener<I,T>
{
    private static final Log LOG = LogFactory.getLog(CacheChangeListener.class);
    private Persister persister;

    public CacheChangeListener( Persister persister ) {
        this.persister = persister;
    }

    @Override
    public void entryAdded(EntryEvent<I,T> event) {
        //skip any entries which are being loaded from the store in the first place
        EntityImpl impl = (EntityImpl)event.getValue();
        if (impl.loadedFromStore()) return;

        LOG.info("Observed entity type " + event.getValue().getClass().getSimpleName() + ", " + event.getKey() + " added");

        EventWrapper wrapper = new EventWrapper();
        wrapper.setType( EventType.CREATE );
        wrapper.setEntity(event.getValue());
        wrapper.setPersister(persister);
        DbQueue.getInstance().add(wrapper);
    }

    @Override
    public void entryUpdated(EntryEvent<I,T> event) {
        T entity= event.getValue();

        LOG.info("Observed entity type " + event.getValue().getClass().getSimpleName() + ", " + event.getKey() + " updated. updateid=" + ((EntityImpl)entity).getUpdateId());

        EventWrapper    wrapper = new EventWrapper();
        wrapper.setType( EventType.UPDATE );
        wrapper.setEntity(event.getValue());
        wrapper.setPersister(persister);
        DbQueue.getInstance().add(wrapper);
    }

    @Override
    public void entryRemoved(EntryEvent<I,T> event) {
        LOG.info("Observed entity " + event.getKey() + " removed");

        EventWrapper    wrapper = new EventWrapper();
        wrapper.setType( EventType.REMOVE );
        wrapper.setEntity(event.getValue());
        wrapper.setPersister(persister);
        DbQueue.getInstance().add(wrapper);

        //NOTE
        //in the case of a remove we will create a pseudo update id
        //for the audit trail. this will allow us to have UniqueID + UpdateId as
        //unique identifier for the update queue, so the active and passive can
        //agree on which parts of the queue have been persisted. IE truncate queue
        //to this value. creates anyway have update id 0

        //pseudo update id could be entity update id + 1 or MAX_value
    }
}

