package transgenic.lauterbrunnen.lateral.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 02/12/16.
 */
public class Admin {

    private static final Log LOG = LogFactory.getLog(Admin.class);
    private static AdminCommandBus adminCommandBus = null;//TODO set to dummy one

    public Admin() {
    }

    public static void setAdminCommandBus(AdminCommandBus xadminCommandBus) {
        adminCommandBus = xadminCommandBus;
    }

    public static Object sendCommandAndCompleteAction(String commandName, String entityName, Object... params) {
        Command command = new Command();
        command.setId(UniqueId.generate() );
        command.setCommand(commandName);
        command.setParameters(params);
        command.setTopic(entityName);

        LOG.debug("Sending command " + commandName + " for " + entityName + " with params " + params);
        return adminCommandBus.sendMessageGetResponse(command);
    }
}
