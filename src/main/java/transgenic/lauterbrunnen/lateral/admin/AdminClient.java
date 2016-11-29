package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.util.Just;

import java.net.InetAddress;
import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 17/06/16.
 *
 * EXPLORATORY, NOT IN USE
 *
 * Servers, services created need to connect to the admin bus
 * to send pings and receive commands
 *
 * This probably needs to be a plugin to ensure the startup order TODO
 * else the inject will fail as will occur before the plugin framework is initialised
 */
public class AdminClient extends Thread {

    private AdminPojo   pojo;
    private long        pingFrequency = 10000;
    private AdminRepository repository = inject(AdminRepository.class);

    public AdminClient(Properties properties) {
        setDaemon(true);
        setName("AdminClient");

        //set everything up and persist to the cache
        pojo = new AdminPojo();
        pojo.setId( UniqueId.generate() );
        pojo.setApplicationName(properties.getProperty("application_name"));
        pojo.setDescription(properties.getProperty("application_description"));
        Just.doIt(() -> pojo.setHostname(InetAddress.getLocalHost().getHostName()));
        pojo.setLastHeartbeatTime(0);
        repository.persist(pojo);

        start();
    }

    public void run() {
        long lastPingTime = 0;

        while(true) {

            //send ping if time expired
            boolean dirty = false;
            pojo = repository.retrieve(pojo.getId());
            if ((System.currentTimeMillis() - lastPingTime)>pingFrequency) {
                pojo.setLastHeartbeatTime(System.currentTimeMillis());
                dirty = true;
            }

            //check for commands
            //incoming commands need to be relayed to the listener


            //persist if needed
            if (dirty) repository.persist( pojo );

            //sleep for a little time
            try{Thread.sleep(100); } catch(Exception e){}
        }
    }

    public static void main(String[] args) {
        new AdminClient(null);
    }
}
