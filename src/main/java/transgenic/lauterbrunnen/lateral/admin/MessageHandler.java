package transgenic.lauterbrunnen.lateral.admin;

/**
 * Created by stumeikle on 04/12/16.
 */
public interface MessageHandler<T> {

    void handle(T t);
    Object blockUntilResponse(Object messageSent);
}
