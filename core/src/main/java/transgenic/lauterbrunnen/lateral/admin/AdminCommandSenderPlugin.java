package transgenic.lauterbrunnen.lateral.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGAdminCommandBus;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGIncomingMessageQueue;
import transgenic.lauterbrunnen.lateral.admin.jgroups.JGOutgoingMessageQueue;
import transgenic.lauterbrunnen.lateral.plugin.LateralPlugin;
import transgenic.lauterbrunnen.lateral.plugin.LateralPluginParameters;

import java.util.Properties;

/**
 * Created by stumeikle on 28/05/19.
 */
@LateralPluginParameters(configName = "admin_command_sender", groups = "admin_command_bus" )
public class AdminCommandSenderPlugin implements LateralPlugin  {

    private static final Log LOG = LogFactory.getLog(AdminCommandSenderPlugin.class);

    public void initialise(Properties properties) {
        LOG.info("Initialising admin command sender plugin...");

        JGAdminCommandBus adminCommandBus = new JGAdminCommandBus();
        JGOutgoingMessageQueue<Command> outgoingMessageQueue = new JGOutgoingMessageQueue<>(adminCommandBus);
        JGIncomingMessageQueue<CommandResponse> incomingMessageQueue = new JGIncomingMessageQueue<>(adminCommandBus);
        incomingMessageQueue.setHandler(new CommandResponseHandler());
        Admin.setAdminCommandBus( adminCommandBus ); //this is just to get the 2 queues
    }
}
