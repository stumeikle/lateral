package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication.persist.hazelcast.generated;

import transgenic.lauterbrunnen.lateral.admin.Admin;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Track;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stumeikle on 28/11/16.
 * Loading things via an admin command
 * ie via dbd or similar
 *
 * THIS IS THE CLIENT SIDE
 * the server side will need to live in dbdumper
 */
public class TrackRetrieverImplRemote implements TrackRetriever {
    @Override
    public Object load(Object o) {

        //this should get it into the cache (blocks)
        Admin.sendCommandAndCompleteAction("loadObject", "Track", o.toString()); //TODO add unique id here

        //not sure what we do here. once its into the cache, hazelcast could retrieve it itself
        //might get stored again CHECK TODO hopefully no cyclical calls
        return Repository.retrieve(Track.class, o);
    }

    @Override
    public Map<Object, Object> loadAll(Collection<Object> collection) {
        Map<Object, Object> retval = new HashMap<>();

        //not really recommended
        for(Object key: collection) {
            Object result = load(key);
            if (result!=null) {
                retval.put(key,result);
            }
        }

        return retval;
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        Admin.sendCommandAndCompleteAction("loadAllKeys", "Track");

        //how to get the result back? ARG

        return null;
    }
}
