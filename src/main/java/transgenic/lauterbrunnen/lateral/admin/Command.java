package transgenic.lauterbrunnen.lateral.admin;

/**
 * Created by stumeikle on 02/12/16.
 */

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.io.Serializable;


/**
 * Created by stumeikle on 02/12/16.
 *
 * all change all change. hacked off with hazelcast for blocking on replaces and so on
 * anyway
 *
 * this time -- broadcast approach
 * client broadcasts on a topic,
 * server responds
 * no command ownership. up to the users to configure things correctly
 *
 */
public class Command implements Serializable {
    private UniqueId id;
    private String command;
    private String topic;
    private Object[] parameters;

    public UniqueId getId() {
        return id;
    }

    public void setId(UniqueId id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
