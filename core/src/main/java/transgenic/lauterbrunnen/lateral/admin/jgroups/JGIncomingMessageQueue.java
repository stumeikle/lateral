package transgenic.lauterbrunnen.lateral.admin.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import transgenic.lauterbrunnen.lateral.admin.AdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.IncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.MessageHandler;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by stumeikle on 04/12/16.
 */
public class JGIncomingMessageQueue<T> extends Thread implements IncomingMessageQueue<T>{

    private JChannel channel;
    private LinkedBlockingQueue<T> queue;
    private MessageHandler<T> handler;

    public JGIncomingMessageQueue(JGAdminCommandBus adminCommandBus) {
        channel = adminCommandBus.getChannel();
        queue = new LinkedBlockingQueue<>();
        channel.setReceiver(new ReceiverAdapter() {
            public void receive(Message msg) {
                //only queue messages from others
                if (!(msg.getSrc().equals(channel.getAddress())))
                    queue.add((T)msg.getObject());
                //System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
            }
        });
        this.setName("IncomingMessageQ");
        this.setDaemon(true);
        start();
        adminCommandBus.setIncomingMessageQueue(this);
    }

    public void run() {
        while(true) {
            try {
                T   t = queue.take();

                if (handler!=null) handler.handle(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setHandler(MessageHandler<T> handler) {
        this.handler = handler;
    }

    public MessageHandler<T> getHandler() {
        return handler;
    }
}
