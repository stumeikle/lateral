package transgenic.lauterbrunnen.lateral.admin;

/**
 * Created by stumeikle on 04/12/16.
 */
public interface IncomingMessageQueue<T> {

    void setHandler(MessageHandler<T> handler);
    MessageHandler<T> getHandler();
}
