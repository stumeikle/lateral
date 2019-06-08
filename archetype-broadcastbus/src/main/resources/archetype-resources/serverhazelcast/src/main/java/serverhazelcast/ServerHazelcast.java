package ${package}.serverhazelcast;

import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.admin.Admin;
import transgenic.lauterbrunnen.lateral.admin.Command;
import transgenic.lauterbrunnen.lateral.persist.TransactionManager;

/**
 * Created by stumeikle on 21/06/16.
 */
public class ServerHazelcast {

    public ServerHazelcast() {

        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        //manually catch the admin commands. this can go into the listener class
        //or it could be its own plugin
        //new EntityAdminEndpointManager();

        TransactionManager.INSTANCE.runInTransactionalContext((em)->{});

    }

    public static void main(String[] args) {
        new ServerHazelcast();
    }
}