package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 27/11/16.
 *
 * Changing 20161128 need to pass the whole command back now so that results can be received
 */
public interface CommandCallback {
    void commandDone(Command command);
}
