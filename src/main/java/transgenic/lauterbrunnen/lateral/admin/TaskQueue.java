package transgenic.lauterbrunnen.lateral.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by stumeikle on 02/12/16.
 * executor service appears to block. implementing it ourselves
 */
public class TaskQueue extends Thread {

    private static final Log LOG = LogFactory.getLog(TaskQueue.class);

    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    public TaskQueue() {
        setName("taskQueue");
        setDaemon(true);
        start();
    }

    public void run() {
        while(true) {
            try {
                LOG.debug("Waiting on tasks. queue size = " + tasks.size());
                Runnable runnable = tasks.take();
                runnable.run();
            } catch (InterruptedException e) {
                try{Thread.sleep(100);}catch(Exception te){}
            }
        }
    }

    public void execute(Runnable r) {
        tasks.add(r);
    }
}
