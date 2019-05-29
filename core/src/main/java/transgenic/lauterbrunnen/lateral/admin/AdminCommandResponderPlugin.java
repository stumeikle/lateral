package transgenic.lauterbrunnen.lateral.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGAdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGIncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGOutgoingMessageQueue;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 28/05/19.
 */
@LateralPluginParameters(configName = "admin_command_responder", groups = "admin_command_bus" )
public class AdminCommandResponderPlugin implements LateralPlugin {

    private static final Log LOG = LogFactory.getLog(AdminCommandResponderPlugin.class);

    public void initialise(Properties properties) {
        LOG.info("Initialising admin command responder plugin...");

        JGAdminCommandBus adminCommandBus = new JGAdminCommandBus();
        JGOutgoingMessageQueue<CommandResponse> outgoingMessageQueue = new JGOutgoingMessageQueue<>(adminCommandBus);
        JGIncomingMessageQueue<Command> incomingMessageQueue = new JGIncomingMessageQueue<>(adminCommandBus);
        CommandHandler handler = new CommandHandler(outgoingMessageQueue);// yes, outgoing is for the responses
        incomingMessageQueue.setHandler(handler);
        Admin.setAdminCommandBus( adminCommandBus );

        //we also need to install all the commands
        AdminEndpointManager    adminEndpointManager = inject(AdminEndpointManager.class);
        adminEndpointManager.initialise(handler);
    }

}
