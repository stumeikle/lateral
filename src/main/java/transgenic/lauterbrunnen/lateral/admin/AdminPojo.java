package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

/**
 * Created by stumeikle on 17/06/16.
 */
public class AdminPojo {

    private UniqueId id;
    private String applicationName;
    private String configuration;  // which config was selected when starting?
    private String description;
    private String hostname;
    private String currentState;
    private String targetState;
    private String command;
    private String commandSender;
    private String commandStatus; //being handled, etc, used also if not a state change command
    private long   lastHeartbeatTime;

    //perhaps
    //resource, partition
    //perhaps: history ( which states this instance has been in )


    public UniqueId getId() {
        return id;
    }

    public void setId(UniqueId id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandSender() {
        return commandSender;
    }

    public void setCommandSender(String commandSender) {
        this.commandSender = commandSender;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(long lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }
}
