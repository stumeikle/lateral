package transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverdbdumper;

import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.admin.Admin;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.example.simplehazelcast.serverdbdumper.persist.hazelcast.generated.TeacherRetrieverImplDirect;
import transgenic.lauterbrunnen.lateral.persist.TransactionManager;

/**
 * Created by stumeikle on 21/06/16.
 */
public class ServerDbdumper {

    public ServerDbdumper() {

        Lateral.INSTANCE.initialise();

        //manually catch the admin commands. this can go into the listener class
        //or it could be its own plugin
        new EntityAdminEndpointManager();

        TransactionManager.INSTANCE.runInTransactionalContext((em)->{});

    }

    public static void main(String[] args) {
        new ServerDbdumper();
    }
}
