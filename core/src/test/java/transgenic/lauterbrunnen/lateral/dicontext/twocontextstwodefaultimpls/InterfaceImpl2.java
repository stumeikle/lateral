package transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls;

import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

/**
 * Created by stumeikle on 28/07/19.
 */
@DefaultImpl
@DIContext(MyOtherContext.class)
public class InterfaceImpl2 implements MyInterface {
    @Override
    public String getMessage() {
        return "InterfaceImpl2";
    }
}
