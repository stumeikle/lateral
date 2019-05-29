package transgenic.lauterbrunnen.lateral.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by stumeikle on 04/12/16.
 */
public class CommandHandler implements MessageHandler<Command> {

    private Map<String, Map<String, Function<Command, Object>>> handlerMap = new HashMap<>();
    private OutgoingMessageQueue<CommandResponse> outgoingMessageQueue; //This is for the response messages going back

    public CommandHandler(OutgoingMessageQueue<CommandResponse> outgoingMessageQueue) {
        this.outgoingMessageQueue =outgoingMessageQueue;
    }

    @Override
    public void handle(Command command) {
        //command comes in from a client.
        //handle it and send a response back
        Map<String, Function<Command, Object>>      map = handlerMap.get(command.getTopic());
        if (map!=null) {
            Function<Command,Object> function = map.get(command.getCommand());
            if (function!=null) {
                Object result = function.apply(command);
                CommandResponse response = new CommandResponse();
                response.setCommandId(command.getId());
                response.setSuccess(true);
                response.setResult(result);
                outgoingMessageQueue.send(response);
            }
        }
    }

    @Override
    public Object blockUntilResponse(Object messageSent) {
        //no relevance in this context
        return null;
    }

    public void registerHandler(String command, String topic, Function<Command,Object> function) {
        Map<String, Function<Command, Object>>      map = handlerMap.get(topic);
        if (map==null) {
            map = new HashMap<>();
            handlerMap.put(topic, map);
        }
        map.put(command, function);
    }
}
