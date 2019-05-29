package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stumeikle on 04/12/16.
 * This handles INCOMING command responses. ie is active at the cache server side. the admin command sender
 */
public class CommandResponseHandler implements MessageHandler<CommandResponse> {

    private Map<UniqueId, CommandResponse> responseMap = new ConcurrentHashMap<>();
    private Map<UniqueId, Runnable> responseAction = new ConcurrentHashMap<>();

    @Override
    public void handle(CommandResponse commandResponse) {
        //incoming response
        responseMap.put(commandResponse.getCommandId(), commandResponse);
        Runnable action = responseAction.get(commandResponse.getCommandId());
        if (action!=null) { action.run(); }
    }

    @Override
    public Object blockUntilResponse(Object messageSent) {

        //wait until we get the result and then clean up and return
        //Heh. not a reactor model
        Object lock = new Object();
        Command command = (Command)messageSent;
        responseAction.put(command.getId(), () -> {synchronized(lock){ lock.notify(); }});

        if (!responseMap.containsKey(command.getId())) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Object retval = responseMap.get(command.getId()).getResult();

        return retval;
    }
}
