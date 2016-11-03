package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverdbdumper;

import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.persist.TransactionManager;

/**
 * Created by stumeikle on 21/06/16.
 */
public class ServerDbdumper {

    public ServerDbdumper() {

        Lateral.INSTANCE.initialise();
        TransactionManager.INSTANCE.runInTransactionalContext((em)->{});

    }

    public static void main(String[] args) {
        new ServerDbdumper();
    }
}
