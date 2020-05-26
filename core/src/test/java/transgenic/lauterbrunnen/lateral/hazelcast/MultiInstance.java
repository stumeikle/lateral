package transgenic.lauterbrunnen.lateral.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import transgenic.lauterbrunnen.lateral.util.Just;

/**
 * Created by stumeikle on 26/08/19.
 */
public class MultiInstance {

    public MultiInstance() {

        Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

        Config cfg2 = new Config();
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg2);


        Just.doIt(()->Thread.sleep(30000));

    }

    public static void main(String[] args) {
        new MultiInstance();
    }
}
