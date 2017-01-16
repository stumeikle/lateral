package transgenic.lauterbrunnen.lateral.domain;

/**
 * Created by stumeikle on 14/01/17.
 */
public class OptimisticLockingException extends PersistenceException {

    private Class clazz;
    private Object id;
    private OptimisticLockingException next;

    public OptimisticLockingException(Class clazz, Object id) {
        this.clazz = clazz;
        this.id = id;
    }

    public void add(OptimisticLockingException optimisticLockingException) {
        next = optimisticLockingException;
    }
}
