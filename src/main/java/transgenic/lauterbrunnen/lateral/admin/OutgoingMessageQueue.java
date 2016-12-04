package transgenic.lauterbrunnen.lateral.admin;

/**
 * Created by stumeikle on 04/12/16.
 */
public interface OutgoingMessageQueue<T> {

    void send(T message);
}
