package transgenic.lauterbrunnen.lateral.admin;

import transgenic.lauterbrunnen.lateral.domain.UniqueId;

import java.io.Serializable;

/**
 * Created by stumeikle on 02/12/16.
 */
public class CommandResponse implements Serializable{
    private UniqueId commandId;
    private boolean success;
    private Object result;

    public UniqueId getCommandId() {
        return commandId;
    }

    public void setCommandId(UniqueId commandId) {
        this.commandId = commandId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
