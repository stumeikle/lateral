package transgenic.lauterbrunnen.lateral.admin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stumeikle on 02/12/16.
 * CHanging to class from interface generic behaviour now
 */
public interface AdminCommandBus {
    Object sendMessageGetResponse(Object message);
}


