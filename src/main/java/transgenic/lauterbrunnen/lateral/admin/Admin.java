package transgenic.lauterbrunnen.lateral.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import transgenic.lauterbrunnen.lateral.admin.hazelcast.CommandReceiver;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.concurrent.ConcurrentHashMap;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 20/11/16.
 *
 */
public class Admin {

    private static final Log LOG = LogFactory.getLog(Admin.class);
    private static final AdminCommandQueue adminCommandQueue = inject(AdminCommandQueue.class);

    public static void registerInterest( CommandFilter filter, CommandHandler handler)  {
        adminCommandQueue.registerInterest(filter, handler);
    }

    public static boolean claimOwnershipOf( Command command ){
        LOG.debug("Entering claimOwnership. Command status=" + command.getStatus());
        if ("claimed".equals(command.getStatus())) return false;
        try {
            Command proposedValue = (Command) command.clone();
            proposedValue.setOwner(getUniqueName());
            proposedValue.setStatus("claimed");
            proposedValue.setLockVersion( UniqueId.generate());

            LOG.debug("About to do replace");
            if (!adminCommandQueue.replace( command.getCommandId(), command, proposedValue )) {
                LOG.debug("Replace returns false");
                return false;
            }
            LOG.debug("Replace ok");
            return true;
        } catch (CloneNotSupportedException e) {
            return false;
        }
    }

    public static String getUniqueName() {
        return "bob";
    }

    public static void sendCommand(String commandString, Object... params) {
        Command command = new Command();

        command.setCommand(commandString);
        command.setParameter(params);
        command.setCommandId(UniqueId.generate());
        command.setTimeCreated(System.currentTimeMillis());

        adminCommandQueue.create(command);
    }

    public static Command sendCommandAndCompleteAction( String commandString, Object... params) {
        Command command = new Command();

        command.setCommand(commandString);
        command.setParameter(params);
        command.setCommandId(UniqueId.generate());
        command.setTimeCreated(System.currentTimeMillis());

        LOG.debug("Sending admin commmand " + commandString + " , " +
                (params.length>0?params[0]:"") + " with id " + command.getCommandId());

        //register for the callback
        adminCommandQueue.callbackWhenDone(command.getCommandId(), Admin::commandDone);

        //create the command
        adminCommandQueue.create(command);

        //wait on the callback
        int count=0;
        while(!commandsDone.containsKey(command.getCommandId())) {
            try {
                Thread.sleep(500);
            }catch (Exception e) {

            }
            count++;
            if (count%10==0)
                LOG.info("Still waiting on admin command " + command.getCommand() + " ...");
        }
        LOG.debug("Admin command complete");
        return commandsDone.remove(command.getCommandId());
    }

    private static final ConcurrentHashMap<UniqueId, Command> commandsDone = new ConcurrentHashMap<>();
    private static void commandDone(Command command) {
        commandsDone.put(command.getCommandId(),command);
    }

    //used by the command handler to indicate work is complete
    //cuases it to be removed from the map which signals to the remote client that the work is done
    public static void completeCommand( Command command ) {
        LOG.debug("Command id " + command.getCommandId() + " is complete");

        //replace the current command and then remove it. the update saves the result
        adminCommandQueue.update(command.getCommandId(), command);
        LOG.debug("Command updated");
        adminCommandQueue.removeCommand(command.getCommandId());
        LOG.debug("Command removed");
    }
}
