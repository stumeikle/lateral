package transgenic.lauterbrunnen.lateral.admin.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import transgenic.lauterbrunnen.lateral.admin.AdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.admin.IncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.OutgoingMessageQueue;

/**
 * Created by stumeikle on 04/12/16.
 */
public class JGAdminCommandBus implements AdminCommandBus {

    private JChannel jChannel;
    private IncomingMessageQueue incomingMessageQueue;
    private OutgoingMessageQueue outgoingMessageQueue;

    public JGAdminCommandBus() {
        System.setProperty("java.net.preferIPv4Stack", "true");

        //connect to the cluster
        try {
            jChannel = new JChannel("udp.xml");
            jChannel.connect("MyCluster");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JChannel getChannel() {
        return jChannel;
    }

    public IncomingMessageQueue getIncomingMessageQueue() {
        return incomingMessageQueue;
    }

    public void setIncomingMessageQueue(IncomingMessageQueue incomingMessageQueue) {
        this.incomingMessageQueue = incomingMessageQueue;
    }

    public OutgoingMessageQueue getOutgoingMessageQueue() {
        return outgoingMessageQueue;
    }

    public void setOutgoingMessageQueue(OutgoingMessageQueue outgoingMessageQueue) {
        this.outgoingMessageQueue = outgoingMessageQueue;
    }

    public Object sendMessageGetResponse(Object message) {

        outgoingMessageQueue.send(message);

        Object result = incomingMessageQueue.getHandler().blockUntilResponse(message);
        //return the result
        return result;
    }

}
