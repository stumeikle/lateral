package transgenic.lauterbrunnen.lateral.util;

/**
 * Created by stumeikle on 17/06/16.
 */
public class Just {

    public interface ExceptionThrower {
        void run() throws Exception;
    }

    public static void doIt(ExceptionThrower function) {
        try {
            function.run();
        } catch(Exception e) {}
    }

    public static void main(String[] args) {

        Just.doIt(() -> Thread.sleep(1000));
    }
}
