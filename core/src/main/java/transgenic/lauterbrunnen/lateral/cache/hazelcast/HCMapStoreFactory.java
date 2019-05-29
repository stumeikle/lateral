package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.core.MapStoreFactory;

/**
 * Created by stumeikle on 03/11/16.
 */
public interface HCMapStoreFactory extends MapStoreFactory<Object, Object> {
    void setWriteThrough(boolean writeThrough);
    void setReadThrough(boolean readThrough);
}