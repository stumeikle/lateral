package transgenic.lauterbrunnen.lateral.persist;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by stumeikle on 13/05/16.
 * Purposefully single threaded
 */
public class DbQueue extends Thread {

    private static final DbQueue instance = new DbQueue();
    private LinkedBlockingDeque<EventWrapper>   queue;

    private DbQueue() {
        setDaemon(false);
        setName("DbQueue");

        queue = new LinkedBlockingDeque<>();
        start();
    }

    public static DbQueue getInstance() { return instance; }

    public void add( EventWrapper wrapper ) {
        queue.add(wrapper);
    }

    public void run() {
        while(true) {
            try {
                EventWrapper wrapper = queue.poll(1, TimeUnit.SECONDS);

                if (wrapper!=null) {
                    switch(wrapper.getType()) {
                        case CREATE: wrapper.getPersister().persist(wrapper.getEntity()); break;
                        case UPDATE: wrapper.getPersister().update(wrapper.getEntity()); break;
                        case REMOVE: wrapper.getPersister().remove(wrapper.getEntity()); break;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
