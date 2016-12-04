package transgenic.lauterbrunnen.lateral.admin;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.util.Just;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stumeikle on 02/12/16.
 */
public class TopicHandler implements MessageListener<CommandResponse> {

    private static final Log LOG = LogFactory.getLog(TopicHandler.class);
    private String entityName;
    private OutgoingMessageQueue<Command> outgoingMessageQueue;
    private IncomingMessageQueue<CommandResponse> incomingMessageQueue;
    private Map<UniqueId, Runnable> listeners = new ConcurrentHashMap<>();
    private Map<UniqueId, CommandResponse> responses = new ConcurrentHashMap<>();

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public TopicHandler(String entityName, OutgoingMessageQueue<Command> outgoingMessageQueue, IncomingMessageQueue<CommandResponse> incomingMessageQueue) {
        this.entityName = entityName;
        this.incomingMessageQueue = incomingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
    }

    public Object sendCommandAndCompleteAction(Command command) {

        //call us back when its done
        listeners.put(command.getId(), ()->  {}
        );

        //listen for the result
        LOG.debug("Topic publish");
        outgoingMessageQueue.send(command);
        LOG.debug("Topic published");

        //this could have already completed before we get here

        while (responses.get(command.getId())==null) { //this protects against case where we have
            //received the response already before we wait
            Just.doIt(()->Thread.sleep(100));
        }

        LOG.debug("About to process response quack");

        if (responses.containsKey(command.getId())) {
            CommandResponse response = responses.remove(command.getId());
            return response.getResult();
        }
        return null;
    }

    public void onMessage(Message<CommandResponse> message) {
        LOG.debug("onMessage");
        //store it for later retrieval
        CommandResponse response = message.getMessageObject();
        responses.put(response.getCommandId(), response);

        Runnable r = listeners.get(response.getCommandId());
        if (r!=null) {
            r.run();
        }
    }
}
