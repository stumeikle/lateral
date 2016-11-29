package transgenic.lauterbrunnen.lateral.admin;

/**
 * Created by stumeikle on 27/11/16.
 */
public interface CommandFilter {

    //boolean matches(String commandName);
    boolean matches(String commandName, String[] params);
}
