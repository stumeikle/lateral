package transgenic.lauterbrunnen.lateral.admin.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.admin.CommandFilter;
import transgenic.lauterbrunnen.lateral.admin.CommandHandler;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.concurrent.ExecutorService;

/**
 * Created by stumeikle on 27/11/16.
 */
public class CommandReceiver implements EntryAddedListener<UniqueId, Command> {

    private CommandFilter filter;
    private CommandHandler handler;
    private ExecutorService executionService;

    public CommandReceiver(CommandFilter filter, CommandHandler handler, ExecutorService executionService) {
        this.filter = filter;
        this.handler = handler;
        this.executionService = executionService;
    }

    @Override
    public void entryAdded(EntryEvent<UniqueId, Command> entryEvent) {
        Command command = entryEvent.getValue();
        if (filter.matches(command.getCommand(), command.getParameter())) {
            //call other, or enqueue other onto an execution queue
            //handler.handleCommand(command);
            executionService.execute(()->handler.handleCommand(command));
        }
    }
}
