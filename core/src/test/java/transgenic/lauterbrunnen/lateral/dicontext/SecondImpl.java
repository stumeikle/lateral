package transgenic.lauterbrunnen.lateral.dicontext;

import transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls.MyInterface;

/**
 * Created by stumeikle on 29/07/19.
 */
public class SecondImpl implements MyInterface {
    @Override
    public String getMessage() {
        return "a new hope";
    }
}
