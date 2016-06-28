package transgenic.lauterbrunnen.lateral.persist;

/**
 * Created by stumeikle on 13/05/16.
 */
public class EventWrapper {
    private EventType type;
    private Object entity;
    private Persister persister;

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Persister getPersister() {
        return persister;
    }

    public void setPersister(Persister persister) {
        this.persister = persister;
    }
}
