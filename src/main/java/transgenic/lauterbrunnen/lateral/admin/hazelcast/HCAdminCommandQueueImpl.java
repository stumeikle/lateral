package transgenic.lauterbrunnen.lateral.admin.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.sun.org.apache.xpath.internal.SourceTree;
import transgenic.lauterbrunnen.lateral.admin.*;
import transgenic.lauterbrunnen.lateral.cache.hazelcast.HCRepositoryManager;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 27/11/16.
 */
public class HCAdminCommandQueueImpl implements AdminCommandQueue, EntryRemovedListener<UniqueId, Command> {

    private HCRepositoryManager manager = inject(HCRepositoryManager.class);
    private TaskQueue executorService = new TaskQueue();
    private IMap map;
    private ConcurrentHashMap<UniqueId, CommandCallback> callbackMap = new ConcurrentHashMap<>();

    public HCAdminCommandQueueImpl() {
        map = manager.getImapNameMap().get("AdminCommandQueue");
        map.addEntryListener(this, true); //true here as we want the result back
    }

    public void addCommandReceiver(CommandReceiver commandReceiver) {
        map.addEntryListener(commandReceiver, true);

    }

    public void registerInterest(CommandFilter filter, CommandHandler handler)  {

        CommandReceiver     cr = new CommandReceiver(filter,handler,executorService);
        addCommandReceiver(cr);
    }

    public boolean replace(UniqueId commandId, Command oldCommand, Command newCommand) {
        return map.replace(commandId, oldCommand, newCommand);
    }

    public void update(UniqueId commandId, Command command) {
        map.put(commandId, command);
    }

    public void removeCommand( UniqueId commandId ) {
        map.remove(commandId);
    }

    public void create(Command command) {
        System.out.println("About to add command " + command.getCommandId());
        map.put(command.getCommandId(), command);
        System.out.println("Added command " + command.getCommandId());
    }

    @Override
    public void callbackWhenDone(UniqueId id, CommandCallback callback) {
        callbackMap.put(id, callback);
    }

    @Override
    public void entryRemoved(EntryEvent<UniqueId, Command> entryEvent) {

        //capture cache change and pass it on
        //bit too faffy
        CommandCallback callback = callbackMap.get(entryEvent.getKey());
        if (callback!=null) {
            callbackMap.remove(entryEvent.getKey());
            callback.commandDone(entryEvent.getOldValue());
        }
    }
}
