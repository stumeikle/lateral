package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 27/11/16.
 */
public interface AdminCommandQueue {

    void registerInterest( CommandFilter filter, CommandHandler handler);
    boolean replace(UniqueId commandId, Command oldCommand, Command newCommand);
    void removeCommand( UniqueId commandId );
    void create(Command command);
    void callbackWhenDone( UniqueId id, CommandCallback callback );
    void update(UniqueId commandId, Command command);
}
