package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.io.Serializable;

/**
 * Created by stumeikle on 23/11/16.
 */
public class Command implements Serializable, Cloneable {

    private String command;
    private Object[] parameter;

    //did have destination but i don't want this now
    //client shouldn't need to know about server

    private String status; //? unset, created, claimed, done
    private String owner;
    private UniqueId lockVersion; //used to ensure a single owner, optimistic lockign
    private UniqueId commandId;
    private long timeCreated;
    private Object result;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public UniqueId getCommandId() {
        return commandId;
    }

    public void setCommandId(UniqueId commandId) {
        this.commandId = commandId;
    }

    public UniqueId getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(UniqueId lockVersion) {
        this.lockVersion = lockVersion;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object[] getParameter() {
        return parameter;
    }

    public void setParameter(Object[] parameter) {
        this.parameter = parameter;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
