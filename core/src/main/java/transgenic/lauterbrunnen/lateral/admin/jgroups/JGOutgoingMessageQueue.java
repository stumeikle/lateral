package transgenic.lauterbrunnen.lateral.admin.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import transgenic.lauterbrunnen.lateral.admin.AdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.OutgoingMessageQueue;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by stumeikle on 04/12/16.
 */
public class JGOutgoingMessageQueue<T> extends Thread implements OutgoingMessageQueue<T> {

    private JChannel channel;
    private LinkedBlockingQueue<T> queue;

    public JGOutgoingMessageQueue(JGAdminCommandBus adminCommandBus) {
        channel = adminCommandBus.getChannel();
        adminCommandBus.setOutgoingMessageQueue(this);
        queue = new LinkedBlockingQueue<>();
        this.setName("OutgoingMessageQ");
        this.setDaemon(true);
        start();
    }

    @Override
    public void send(T message) {
        queue.add(message);
    }

    public void run() {
        while(true) {
            try {
                T   t = queue.take();
                channel.send( new Message(null, t));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
