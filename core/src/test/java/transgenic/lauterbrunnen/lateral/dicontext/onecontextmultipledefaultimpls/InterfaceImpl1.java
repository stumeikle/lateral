package transgenic.lauterbrunnen.lateral.dicontext.onecontextmultipledefaultimpls;

import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

/**
 * Created by stumeikle on 28/07/19.
 */
@DefaultImpl
public class InterfaceImpl1 implements MyInterface {
    @Override
    public String getMessage() {
        return "InterfaceImpl1";
    }
}
