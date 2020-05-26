package transgenic.lauterbrunnen.lateral.cache.hazelcast;

import com.hazelcast.map.MapStoreFactory;

/**
 * Created by stumeikle on 03/11/16.
 */
public interface HCMapStoreFactory extends MapStoreFactory<Object, Object> {
    void setWriteThrough(boolean writeThrough);
    void setWriteBehind(boolean writeBehind);
    void setReadThrough(boolean readThrough);
}
