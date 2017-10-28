package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication.persist.hazelcast.generated;

import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.TrackImpl;
import transgenic.lauterbrunnen.lateral.example.microservice.serverapplication.entity.generated.TrackEntity;
import transgenic.lauterbrunnen.lateral.example.microservice.serverapplication.entity.generated.TrackEntityTransformer;
import transgenic.lauterbrunnen.lateral.persist.TransactionManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stumeikle on 28/11/16.
 */
public class TrackRetrieverImplDirect implements TrackRetriever {

    private ThreadLocal<Object> trackEntity = new ThreadLocal<>();

    @Override
    public Object load(Object key) {
        //convert repository key to db key if needed
        Object      dbKey=key;
        trackEntity.set(null);
        TransactionManager.INSTANCE.runInTransactionalContext(em -> {
            trackEntity.set( em.find( TrackEntity.class, dbKey ));
        });

        //convert back to impl
        if (trackEntity.get()!=null) {
            TrackImpl trackImpl = new TrackImpl();
            TrackEntityTransformer.transform(trackImpl,(TrackEntity)trackEntity.get());

            return trackImpl;
        }
        return null;    }

	//TODO yes, could be much optimised
    @Override
    public Map<Object, Object> loadAll(Collection<Object> collection) {
        Map<Object, Object> retval = new HashMap<>();

        for(Object key: collection) {
            Object entity = load(key);
            if (entity!=null) {
                retval.put(key,entity);
            }
        }

        return retval;
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        //use sql to pull out all the keys
		//need to add
		//@NamedQuery(name="TrackEntity.findAllIds", query="SELECT track.id FROM TrackEntity track")
		//To entities
        TransactionManager.INSTANCE.runInTransactionalContext((em)->{
            Query query = em.createNamedQuery("TrackEntity.findAllIds");
            trackEntity.set(query.getResultList());
        });
        return (Iterable<Object>)trackEntity.get();
    }
}

