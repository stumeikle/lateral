package transgenic.lauterbrunnen.lateral.dicontext.onecontextmultipledefaultimpls;

import org.checkerframework.framework.qual.DefaultFor;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;

/**
 * Created by stumeikle on 28/07/19.
 */
@DefaultImpl
public class InterfaceImpl2 implements MyInterface{
    @Override
    public String getMessage() {
        return "InterfaceImpl2";
    }
}
